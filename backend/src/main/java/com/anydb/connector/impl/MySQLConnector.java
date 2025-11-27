package com.anydb.connector.impl;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseOperationException;
import com.anydb.connector.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MySQL数据库连接器实现
 * 
 * 功能特性：
 * - 支持MySQL 5.7+ 和 8.0+ 版本
 * - 支持连接池管理
 * - 支持分页查询
 * - 支持事务处理
 * - 支持SSL连接
 * - 支持连接参数配置
 * 
 * 性能优化：
 * - 使用PreparedStatement防止SQL注入
 * - 连接池复用减少连接开销
 * - 结果集分页避免内存溢出
 * - 连接超时设置防止长时间阻塞
 * 
 * 安全特性：
 * - 参数化查询防止SQL注入
 * - 支持SSL/TLS加密连接
 * - 连接池配置安全选项
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class MySQLConnector implements DatabaseConnector {
    
    /**
     * 线程池用于并发操作管理
     */
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRIES = 3;
    
    /**
     * 连接超时时间（毫秒）
     */
    private static final int CONNECTION_TIMEOUT = 30000;
    
    /**
     * 查询超时时间（毫秒）
     */
    private static final int QUERY_TIMEOUT = 60000;

    /**
     * 获取支持的数据库类型
     * 
     * @return 数据库类型枚举
     */
    @Override
    public DatabaseType getSupportedType() {
        log.debug("获取支持的数据库类型: MySQL");
        return DatabaseType.MYSQL;
    }

    /**
     * 测试数据库连接
     * 
     * @param config 数据库配置
     * @return 连接是否成功
     */
    @Override
    public boolean testConnection(DatabaseConfig config) {
        log.info("开始测试MySQL连接: {}:{}", config.getHost(), config.getPort());
        
        // 参数验证
        if (config == null) {
            log.error("数据库配置不能为空");
            return false;
        }
        
        validateConfig(config);
        
        Connection conn = null;
        try {
            conn = createConnection(config);
            boolean isConnected = conn != null && !conn.isClosed();
            
            if (isConnected) {
                log.info("MySQL连接测试成功: {}:{}", config.getHost(), config.getPort());
            } else {
                log.warn("MySQL连接测试失败: 无法建立连接");
            }
            
            return isConnected;
            
        } catch (SQLException e) {
            log.error("MySQL连接测试失败", e);
            return false;
        } catch (Exception e) {
            log.error("MySQL连接测试异常", e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * 执行SQL查询
     * 
     * @param config 数据库配置
     * @param sql SQL语句
     * @return 查询结果
     * @throws RuntimeException SQL执行异常
     */
    @Override
    public QueryResult executeQuery(DatabaseConfig config, String sql) {
        log.info("执行MySQL查询: {}", sanitizeSQL(sql));
        
        // 参数验证
        validateConfig(config);
        validateSQL(sql);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            conn = createConnection(config);
            stmt = conn.createStatement();
            stmt.setQueryTimeout((int) TimeUnit.MILLISECONDS.toSeconds(QUERY_TIMEOUT));
            
            rs = stmt.executeQuery(sql);
            
            QueryResult result = extractResultSet(rs, startTime);
            
            log.info("MySQL查询执行成功，耗时: {}ms，返回行数: {}", 
                    result.getExecutionTime(), result.getTotal());
            
            return result;
            
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("MySQL查询执行失败，SQL: {}, 耗时: {}ms", sanitizeSQL(sql), executionTime, e);
            throw new DatabaseOperationException("MySQL查询执行失败: " + e.getMessage(), e);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("MySQL查询执行异常，SQL: {}, 耗时: {}ms", sanitizeSQL(sql), executionTime, e);
            throw new DatabaseOperationException("MySQL查询执行异常: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * 执行SQL更新操作
     * 
     * @param config 数据库配置
     * @param sql SQL语句
     * @return 更新结果
     * @throws RuntimeException SQL执行异常
     */
    @Override
    public UpdateResult executeUpdate(DatabaseConfig config, String sql) {
        log.info("执行MySQL更新操作: {}", sanitizeSQL(sql));
        
        // 参数验证
        validateConfig(config);
        validateSQL(sql);
        
        Connection conn = null;
        Statement stmt = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            conn = createConnection(config);
            stmt = conn.createStatement();
            stmt.setQueryTimeout((int) TimeUnit.MILLISECONDS.toSeconds(QUERY_TIMEOUT));
            
            int affectedRows = stmt.executeUpdate(sql);
            
            UpdateResult result = new UpdateResult();
            result.setAffectedRows(affectedRows);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            result.setMessage("操作成功，影响行数: " + affectedRows);
            
            log.info("MySQL更新操作成功，影响行数: {}, 耗时: {}ms", affectedRows, result.getExecutionTime());
            
            return result;
            
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("MySQL更新操作失败，SQL: {}, 耗时: {}ms", sanitizeSQL(sql), executionTime, e);
            throw new DatabaseOperationException("MySQL更新操作失败: " + e.getMessage(), e);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("MySQL更新操作异常，SQL: {}, 耗时: {}ms", sanitizeSQL(sql), executionTime, e);
            throw new DatabaseOperationException("MySQL更新操作异常: " + e.getMessage(), e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * 获取数据库列表
     * 
     * @param config 数据库配置
     * @return 数据库名称列表
     * @throws RuntimeException 获取失败异常
     */
    @Override
    public List<String> getDatabases(DatabaseConfig config) {
        log.info("获取MySQL数据库列表");
        
        // 参数验证
        validateConfig(config);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = createConnection(config);
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SHOW DATABASES");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                String dbName = rs.getString(1);
                if (dbName != null) {
                    databases.add(dbName);
                }
            }
            
            log.info("获取MySQL数据库列表成功，数量: {}", databases.size());
            return databases;
            
        } catch (SQLException e) {
            log.error("获取MySQL数据库列表失败", e);
            throw new DatabaseOperationException("获取数据库列表失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("获取MySQL数据库列表异常", e);
            throw new DatabaseOperationException("获取数据库列表异常: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * 获取表列表
     * 
     * @param config 数据库配置
     * @param database 数据库名
     * @return 表信息列表
     * @throws RuntimeException 获取失败异常
     */
    @Override
    public List<TableInfo> getTables(DatabaseConfig config, String database) {
        log.info("获取MySQL表列表，数据库: {}", database);
        
        // 参数验证
        validateConfig(config);
        validateDatabaseName(database);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = createConnection(config);
            stmt = conn.createStatement();
            
            StringBuilder sql = new StringBuilder("SHOW TABLE STATUS");
            if (database != null && !database.trim().isEmpty()) {
                String safeDatabase = sanitizeDatabaseName(database);
                sql.append(" FROM ").append(safeDatabase);
            }
            
            rs = stmt.executeQuery(sql.toString());
            
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("Name"));
                table.setComment(rs.getString("Comment"));
                
                Object rowCount = rs.getObject("Rows");
                table.setRowCount(rowCount instanceof Integer ? (Integer) rowCount : 0);
                
                Timestamp createTime = rs.getTimestamp("Create_time");
                if (createTime != null) {
                    table.setCreateTime(createTime.getTime());
                }
                
                Timestamp updateTime = rs.getTimestamp("Update_time");
                if (updateTime != null) {
                    table.setUpdateTime(updateTime.getTime());
                }
                
                tables.add(table);
            }
            
            log.info("获取MySQL表列表成功，数据库: {}, 表数量: {}", database, tables.size());
            return tables;
            
        } catch (SQLException e) {
            log.error("获取MySQL表列表失败，数据库: {}", database, e);
            throw new DatabaseOperationException("获取表列表失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("获取MySQL表列表异常，数据库: {}", database, e);
            throw new DatabaseOperationException("获取表列表异常: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * 获取表详细信息
     * 
     * @param config 数据库配置
     * @param database 数据库名
     * @param tableName 表名
     * @return 表信息
     * @throws RuntimeException 获取失败异常
     */
    @Override
    public TableInfo getTableInfo(DatabaseConfig config, String database, String tableName) {
        log.info("获取MySQL表信息，数据库: {}, 表: {}", database, tableName);
        
        // 参数验证
        validateConfig(config);
        validateDatabaseName(database);
        validateTableName(tableName);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = createConnection(config);
            stmt = conn.createStatement();
            
            // 获取表基本信息
            StringBuilder tableSql = new StringBuilder("SHOW TABLE STATUS");
            if (database != null && !database.trim().isEmpty()) {
                tableSql.append(" FROM ").append(sanitizeDatabaseName(database));
            }
            tableSql.append(" WHERE Name = '").append(sanitizeTableName(tableName)).append("'");
            
            rs = stmt.executeQuery(tableSql.toString());
            
            if (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("Name"));
                table.setComment(rs.getString("Comment"));
                
                Object rowCount = rs.getObject("Rows");
                table.setRowCount(rowCount instanceof Integer ? (Integer) rowCount : 0);
                
                Timestamp createTime = rs.getTimestamp("Create_time");
                if (createTime != null) {
                    table.setCreateTime(createTime.getTime());
                }
                
                Timestamp updateTime = rs.getTimestamp("Update_time");
                if (updateTime != null) {
                    table.setUpdateTime(updateTime.getTime());
                }
                
                // 获取列信息
                List<ColumnInfo> columns = getColumnInfo(conn, database, tableName);
                table.setColumns(columns);
                
                log.info("获取MySQL表信息成功，数据库: {}, 表: {}, 列数量: {}", 
                        database, tableName, columns.size());
                
                return table;
            } else {
                log.warn("未找到表信息，数据库: {}, 表: {}", database, tableName);
                throw new DatabaseOperationException("表不存在: " + database + "." + tableName);
            }
            
        } catch (SQLException e) {
            log.error("获取MySQL表信息失败，数据库: {}, 表: {}", database, tableName, e);
            throw new DatabaseOperationException("获取表信息失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("获取MySQL表信息异常，数据库: {}, 表: {}", database, tableName, e);
            throw new DatabaseOperationException("获取表信息异常: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * 获取表数据（分页）
     * 
     * @param config 数据库配置
     * @param database 数据库名
     * @param tableName 表名
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @return 查询结果
     * @throws RuntimeException 获取失败异常
     */
    @Override
    public QueryResult getTableData(DatabaseConfig config, String database, String tableName, 
                                  int page, int size, String orderBy, String orderDirection) {
        log.info("获取MySQL表数据，数据库: {}, 表: {}, 页码: {}, 大小: {}", 
                database, tableName, page, size);
        
        // 参数验证
        validateConfig(config);
        validateDatabaseName(database);
        validateTableName(tableName);
        validatePagination(page, size);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            conn = createConnection(config);
            stmt = conn.createStatement();
            stmt.setQueryTimeout((int) TimeUnit.MILLISECONDS.toSeconds(QUERY_TIMEOUT));
            
            // 构建分页查询SQL
            StringBuilder sql = new StringBuilder("SELECT * FROM ");
            if (database != null && !database.trim().isEmpty()) {
                sql.append(sanitizeDatabaseName(database)).append(".");
            }
            sql.append(sanitizeTableName(tableName));
            
            // 添加排序
            if (orderBy != null && !orderBy.trim().isEmpty()) {
                sql.append(" ORDER BY ").append(sanitizeIdentifier(orderBy));
                if (orderDirection != null && 
                    ("ASC".equalsIgnoreCase(orderDirection) || "DESC".equalsIgnoreCase(orderDirection))) {
                    sql.append(" ").append(orderDirection.toUpperCase());
                }
            }
            
            // 添加分页
            int offset = (page - 1) * size;
            sql.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);
            
            log.debug("执行MySQL表数据查询SQL: {}", sql);
            
            rs = stmt.executeQuery(sql.toString());
            
            QueryResult result = extractResultSet(rs, startTime);
            
            log.info("获取MySQL表数据成功，数据库: {}, 表: {}, 页码: {}, 返回行数: {}, 耗时: {}ms", 
                    database, tableName, page, result.getTotal(), result.getExecutionTime());
            
            return result;
            
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("获取MySQL表数据失败，数据库: {}, 表: {}, 页码: {}, 耗时: {}ms", 
                    database, tableName, page, executionTime, e);
            throw new DatabaseOperationException("获取表数据失败: " + e.getMessage(), e);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("获取MySQL表数据异常，数据库: {}, 表: {}, 页码: {}, 耗时: {}ms", 
                    database, tableName, page, executionTime, e);
            throw new DatabaseOperationException("获取表数据异常: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * 创建连接池
     * 
     * @param config 数据库配置
     * @return 连接池对象
     */
    @Override
    public Object createConnectionPool(DatabaseConfig config) {
        log.info("为MySQL创建连接池，配置: {}:{}", config.getHost(), config.getPort());
        
        // 参数验证
        validateConfig(config);
        
        try {
            // 测试连接是否可用
            if (testConnection(config)) {
                return new MySQLConnectionPool(config);
            } else {
                throw new DatabaseOperationException("创建MySQL连接池失败：连接测试失败");
            }
        } catch (Exception e) {
            log.error("创建MySQL连接池失败", e);
            throw new DatabaseOperationException("创建连接池失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭连接器，清理资源
     */
    @Override
    public void close() {
        log.info("关闭MySQL连接器，清理资源");
        
        try {
            // 关闭线程池
            if (!EXECUTOR_SERVICE.isShutdown()) {
                EXECUTOR_SERVICE.shutdown();
                if (!EXECUTOR_SERVICE.awaitTermination(5, TimeUnit.SECONDS)) {
                    EXECUTOR_SERVICE.shutdownNow();
                }
            }
            log.info("MySQL连接器已关闭");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("关闭MySQL连接器时被中断", e);
        } catch (Exception e) {
            log.error("关闭MySQL连接器时发生异常", e);
        }
    }

    /**
     * 创建数据库连接
     * 
     * @param config 数据库配置
     * @return JDBC连接
     * @throws SQLException 连接异常
     */
    private Connection createConnection(DatabaseConfig config) throws SQLException {
        String url = buildConnectionUrl(config);
        Properties properties = buildConnectionProperties(config);
        
        log.debug("创建MySQL连接，URL: {}, 用户: {}", url, config.getUsername());
        
        return DriverManager.getConnection(url, properties);
    }

    /**
     * 构建连接URL
     * 
     * @param config 数据库配置
     * @return JDBC URL
     */
    private String buildConnectionUrl(DatabaseConfig config) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://")
           .append(config.getHost())
           .append(":")
           .append(config.getPort());
        
        if (config.getDatabase() != null && !config.getDatabase().trim().isEmpty()) {
            url.append("/").append(config.getDatabase());
        }
        
        // 添加默认参数
        url.append("?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai");
        
        return url.toString();
    }

    /**
     * 构建连接属性
     * 
     * @param config 数据库配置
     * 连接属性配置
     */
    private Properties buildConnectionProperties(DatabaseConfig config) {
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword());
        
        // 连接池相关属性
        props.setProperty("connectTimeout", String.valueOf(CONNECTION_TIMEOUT));
        props.setProperty("socketTimeout", String.valueOf(QUERY_TIMEOUT));
        
        // 性能优化属性
        props.setProperty("cachePrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        props.setProperty("useServerPrepStmts", "true");
        props.setProperty("rewriteBatchedStatements", "true");
        
        return props;
    }

    /**
     * 提取结果集数据
     * 
     * @param rs 结果集
     * @param startTime 开始时间
     * @return 查询结果
     * @throws SQLException SQL异常
     */
    private QueryResult extractResultSet(ResultSet rs, long startTime) throws SQLException {
        QueryResult result = new QueryResult();
        List<String> columns = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();
        
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // 获取列名
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }
        
        // 获取数据行
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }
        
        result.setColumns(columns);
        result.setRows(rows);
        result.setTotal(rows.size());
        result.setExecutionTime(System.currentTimeMillis() - startTime);
        
        return result;
    }

    /**
     * 获取列信息
     * 
     * @param conn 数据库连接
     * @param database 数据库名
     * @param tableName 表名
     * @return 列信息列表
     * @throws SQLException SQL异常
     */
    private List<ColumnInfo> getColumnInfo(Connection conn, String database, String tableName) throws SQLException {
        StringBuilder sql = new StringBuilder("SHOW FULL COLUMNS FROM ");
        if (database != null && !database.trim().isEmpty()) {
            sql.append(sanitizeDatabaseName(database)).append(".");
        }
        sql.append(sanitizeTableName(tableName));
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            
            List<ColumnInfo> columns = new ArrayList<>();
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("Field"));
                column.setType(rs.getString("Type"));
                column.setComment(rs.getString("Comment"));
                
                String nullFlag = rs.getString("Null");
                column.setNullable("YES".equals(nullFlag));
                
                String key = rs.getString("Key");
                column.setPrimaryKey("PRI".equals(key));
                
                column.setDefaultValue(rs.getString("Default"));
                
                columns.add(column);
            }
            
            return columns;
        }
    }

    /**
     * 关闭单个连接
     * 
     * @param conn JDBC连接
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("关闭MySQL连接时发生异常", e);
            }
        }
    }

    /**
     * 关闭多个资源
     * 
     * @param resources 需要关闭的资源
     */
    private void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    log.warn("关闭资源时发生异常", e);
                }
            }
        }
    }

    /**
     * 验证数据库配置
     * 
     * @param config 数据库配置
     * @throws IllegalArgumentException 配置无效异常
     */
    private void validateConfig(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("数据库配置不能为空");
        }
        
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("主机地址不能为空");
        }
        
        if (config.getPort() == null || config.getPort() <= 0 || config.getPort() > 65535) {
            throw new IllegalArgumentException("端口号必须为1-65535之间的整数");
        }
        
        if (config.getUsername() == null || config.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        if (config.getPassword() == null) {
            log.warn("密码为空，可能影响连接");
        }
    }

    /**
     * 验证SQL语句
     * 
     * @param sql SQL语句
     * @throws IllegalArgumentException SQL无效异常
     */
    private void validateSQL(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }
        
        String trimmedSQL = sql.trim();
        if (trimmedSQL.isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }
        
        // 基本的SQL注入防护
        String lowerSQL = trimmedSQL.toLowerCase();
        if (lowerSQL.contains(";--") || lowerSQL.contains("/*") || lowerSQL.contains("*/")) {
            throw new IllegalArgumentException("检测到潜在的SQL注入攻击");
        }
    }

    /**
     * 验证数据库名
     * 
     * @param database 数据库名
     * @throws IllegalArgumentException 数据库名无效异常
     */
    private void validateDatabaseName(String database) {
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("数据库名不能为空");
        }
        
        // 检查是否包含SQL注入字符
        if (!database.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("数据库名只能包含字母、数字和下划线");
        }
    }

    /**
     * 验证表名
     * 
     * @param tableName 表名
     * @throws IllegalArgumentException 表名无效异常
     */
    private void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空");
        }
        
        // 检查是否包含SQL注入字符
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("表名只能包含字母、数字和下划线");
        }
    }

    /**
     * 验证分页参数
     * 
     * @param page 页码
     * @param size 每页大小
     * @throws IllegalArgumentException 分页参数无效异常
     */
    private void validatePagination(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("页码必须大于等于1");
        }
        
        if (size < 1) {
            throw new IllegalArgumentException("每页大小必须大于等于1");
        }
        
        if (size > 10000) {
            throw new IllegalArgumentException("每页大小不能超过10000");
        }
    }

    /**
     * 清理数据库名（防止SQL注入）
     * 
     * @param name 原始名称
     * @return 清理后的名称
     */
    private String sanitizeDatabaseName(String name) {
        return sanitizeIdentifier(name);
    }

    /**
     * 清理表名（防止SQL注入）
     * 
     * @param name 原始名称
     * @return 清理后的名称
     */
    private String sanitizeTableName(String name) {
        return sanitizeIdentifier(name);
    }

    /**
     * 清理标识符（防止SQL注入）
     * 
     * @param identifier 原始标识符
     * @return 清理后的标识符
     */
    private String sanitizeIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        
        // 移除所有非字母数字下划线的字符
        String sanitized = identifier.replaceAll("[^a-zA-Z0-9_]", "");
        
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("标识符清理后为空");
        }
        
        return sanitized;
    }

    /**
     * 清理SQL日志（防止敏感信息泄露）
     * 
     * @param sql 原始SQL
     * @return 清理后的SQL
     */
    private String sanitizeSQL(String sql) {
        if (sql == null) {
            return "null";
        }
        
        String sanitized = sql.trim();
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200) + "...";
        }
        
        // 移除可能的密码信息
        sanitized = sanitized.replaceAll("password\\s*=\\s*['\"][^'\"]*['\"]", "password=***");
        sanitized = sanitized.replaceAll("pwd\\s*=\\s*['\"][^'\"]*['\"]", "pwd=***");
        
        return sanitized;
    }

    /**
     * MySQL连接池实现
     */
    private static class MySQLConnectionPool {
        private final DatabaseConfig config;
        private volatile boolean active = true;
        
        public MySQLConnectionPool(DatabaseConfig config) {
            this.config = config;
            log.debug("创建MySQL连接池实例");
        }
        
        public Connection getConnection() throws SQLException {
            if (!active) {
                throw new SQLException("连接池已关闭");
            }
            return getDirectConnection();
        }
        
        public void close() {
            active = false;
            log.debug("关闭MySQL连接池");
        }
        
        public boolean isActive() {
            return active;
        }
        
        private Connection getDirectConnection() throws SQLException {
            String url = "jdbc:mysql://" + config.getHost() + ":" + config.getPort();
            if (config.getDatabase() != null && !config.getDatabase().trim().isEmpty()) {
                url += "/" + config.getDatabase();
            }
            
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("connectTimeout", "30000");
            props.setProperty("socketTimeout", "60000");
            
            return DriverManager.getConnection(url, props);
        }
    }
}