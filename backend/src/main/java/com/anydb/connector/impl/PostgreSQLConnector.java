package com.anydb.connector.impl;

import com.anydb.connector.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PostgreSQL数据库连接器实现
 * 
 * 特性：
 * - 标准SQL支持
 * - 事务管理
 * - 存储过程支持
 * - JSON/JSONB数据类型
 * - 数组类型支持
 * - 全文检索
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class PostgreSQLConnector implements DatabaseConnector {
    
    /**
     * 获取支持的数据库类型
     */
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.POSTGRESQL;
    }
    
    /**
     * 测试数据库连接
     */
    @Override
    public boolean testConnection(DatabaseConfig config) {
        validateConfig(config);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            log.debug("测试PostgreSQL连接: {}:{}", config.getHost(), config.getPort());
            
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            // PostgreSQL特定连接测试
            rs = stmt.executeQuery("SELECT 1");
            boolean result = rs.next();
            
            log.debug("PostgreSQL连接测试成功");
            return result;
            
        } catch (SQLException e) {
            log.warn("PostgreSQL连接测试失败: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("PostgreSQL连接测试异常: {}", e.getMessage());
            return false;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * 执行SQL查询
     */
    @Override
    public QueryResult executeQuery(DatabaseConfig config, String sql) {
        validateConfig(config);
        validateSql(sql);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("执行PostgreSQL SQL查询: {}", sql);
            
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            // PostgreSQL特殊查询处理
            if (isPostgreSQLSpecialQuery(sql)) {
                return executePostgreSQLSpecialQuery(stmt, sql);
            }
            
            rs = stmt.executeQuery(sql);
            
            return processResultSet(rs, startTime);
            
        } catch (SQLException e) {
            log.error("PostgreSQL SQL执行失败: {}", e.getMessage());
            throw new DatabaseOperationException(
                "SQL_EXECUTION_FAILED",
                "PostgreSQL SQL执行失败: " + e.getMessage(),
                "EXECUTE_QUERY",
                e
            );
        } catch (Exception e) {
            log.error("PostgreSQL SQL执行异常: {}", e.getMessage());
            throw new DatabaseOperationException(
                "INTERNAL_ERROR",
                "PostgreSQL SQL执行异常: " + e.getMessage(),
                "EXECUTE_QUERY",
                e
            );
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * 执行SQL更新
     */
    @Override
    public UpdateResult executeUpdate(DatabaseConfig config, String sql) {
        validateConfig(config);
        validateSql(sql);
        
        Connection conn = null;
        Statement stmt = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("执行PostgreSQL SQL更新: {}", sql);
            
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            int affectedRows = stmt.executeUpdate(sql);
            
            UpdateResult result = new UpdateResult();
            result.setAffectedRows(affectedRows);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            result.setMessage("PostgreSQL操作成功");
            
            log.debug("PostgreSQL更新操作完成，影响行数: {}", affectedRows);
            return result;
            
        } catch (SQLException e) {
            log.error("PostgreSQL SQL更新失败: {}", e.getMessage());
            throw new DatabaseOperationException(
                "SQL_EXECUTION_FAILED",
                "PostgreSQL SQL更新失败: " + e.getMessage(),
                "EXECUTE_UPDATE",
                e
            );
        } catch (Exception e) {
            log.error("PostgreSQL SQL更新异常: {}", e.getMessage());
            throw new DatabaseOperationException(
                "INTERNAL_ERROR",
                "PostgreSQL SQL更新异常: " + e.getMessage(),
                "EXECUTE_UPDATE",
                e
            );
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    /**
     * 获取数据库列表
     */
    @Override
    public List<String> getDatabases(DatabaseConfig config) {
        validateConfig(config);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            log.debug("获取PostgreSQL数据库列表");
            
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            // PostgreSQL特定语法
            rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
            
            log.debug("获取到 {} 个数据库", databases.size());
            return databases;
            
        } catch (SQLException e) {
            log.error("获取PostgreSQL数据库列表失败: {}", e.getMessage());
            throw new DatabaseOperationException(
                "QUERY_FAILED",
                "PostgreSQL获取数据库列表失败: " + e.getMessage(),
                "GET_DATABASES",
                e
            );
        } catch (Exception e) {
            log.error("获取PostgreSQL数据库列表异常: {}", e.getMessage());
            throw new DatabaseOperationException(
                "INTERNAL_ERROR",
                "PostgreSQL获取数据库列表异常: " + e.getMessage(),
                "GET_DATABASES",
                e
            );
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * 获取表列表
     */
    @Override
    public List<TableInfo> getTables(DatabaseConfig config, String database) {
        validateConfig(config);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            log.debug("获取PostgreSQL表列表，数据库: {}", database);
            
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            StringBuilder sql = new StringBuilder("SELECT tablename FROM pg_tables WHERE schemaname = 'public'");
            if (database != null && !database.trim().isEmpty()) {
                sql.append(" AND schemaname = '").append(database).append("'");
            }
            sql.append(" ORDER BY tablename");
            
            rs = stmt.executeQuery(sql.toString());
            
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString(1));
                table.setComment("PostgreSQL表");
                
                // PostgreSQL表行数统计
                table.setRowCount(getTableRowCount(conn, database, table.getName()));
                
                tables.add(table);
            }
            
            log.debug("获取到 {} 个表", tables.size());
            return tables;
            
        } catch (SQLException e) {
            log.error("获取PostgreSQL表列表失败: {}", e.getMessage());
            throw new DatabaseOperationException(
                "QUERY_FAILED",
                "PostgreSQL获取表列表失败: " + e.getMessage(),
                "GET_TABLES",
                e
            );
        } catch (Exception e) {
            log.error("获取PostgreSQL表列表异常: {}", e.getMessage());
            throw new DatabaseOperationException(
                "INTERNAL_ERROR",
                "PostgreSQL获取表列表异常: " + e.getMessage(),
                "GET_TABLES",
                e
            );
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * 获取表信息
     */
    @Override
    public TableInfo getTableInfo(DatabaseConfig config, String database, String tableName) {
        validateConfig(config);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            log.debug("获取PostgreSQL表信息，数据库: {}, 表: {}", database, tableName);
            
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            TableInfo table = new TableInfo();
            table.setName(tableName);
            table.setComment("PostgreSQL表");
            table.setRowCount(getTableRowCount(conn, database, tableName));
            
            // 获取列信息
            List<ColumnInfo> columns = getColumnInfo(conn, database, tableName);
            table.setColumns(columns);
            
            log.debug("获取到 {} 个列信息", columns.size());
            return table;
            
        } catch (SQLException e) {
            log.error("获取PostgreSQL表信息失败: {}", e.getMessage());
            throw new DatabaseOperationException(
                "QUERY_FAILED",
                "PostgreSQL获取表信息失败: " + e.getMessage(),
                "GET_TABLE_INFO",
                e
            );
        } catch (Exception e) {
            log.error("获取PostgreSQL表信息异常: {}", e.getMessage());
            throw new DatabaseOperationException(
                "INTERNAL_ERROR",
                "PostgreSQL获取表信息异常: " + e.getMessage(),
                "GET_TABLE_INFO",
                e
            );
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    /**
     * 获取表数据（分页）
     */
    @Override
    public QueryResult getTableData(DatabaseConfig config, String database, String tableName, 
                                  int page, int size, String orderBy, String orderDirection) {
        validateConfig(config);
        
        if (page <= 0) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("页面大小必须大于0");
        }
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空");
        }
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("获取PostgreSQL表数据，数据库: {}, 表: {}, 页码: {}, 每页: {}", 
                     database, tableName, page, size);
            
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            StringBuilder sql = new StringBuilder("SELECT * FROM ");
            if (database != null && !database.trim().isEmpty()) {
                sql.append(database).append(".");
            }
            sql.append(tableName);
            
            if (orderBy != null && !orderBy.trim().isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
                if (orderDirection != null && !orderDirection.trim().isEmpty()) {
                    sql.append(" ").append(orderDirection.toUpperCase());
                }
            }
            
            // PostgreSQL分页：使用LIMIT和OFFSET
            sql.append(" LIMIT ").append(size).append(" OFFSET ").append((page - 1) * size);
            
            log.debug("执行SQL: {}", sql.toString());
            rs = stmt.executeQuery(sql.toString());
            
            QueryResult result = processResultSet(rs, startTime);
            
            log.debug("获取到 {} 行数据，耗时: {}ms", result.getRows().size(), result.getExecutionTime());
            return result;
            
        } catch (SQLException e) {
            log.error("获取PostgreSQL表数据失败: {}", e.getMessage());
            throw new DatabaseOperationException(
                "QUERY_FAILED",
                "PostgreSQL获取表数据失败: " + e.getMessage(),
                "GET_TABLE_DATA",
                e
            );
        } catch (Exception e) {
            log.error("获取PostgreSQL表数据异常: {}", e.getMessage());
            throw new DatabaseOperationException(
                "INTERNAL_ERROR",
                "PostgreSQL获取表数据异常: " + e.getMessage(),
                "GET_TABLE_DATA",
                e
            );
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    
    
    // ========== 验证和工具方法 ==========
    
    /**
     * 验证数据库配置
     */
    private void validateConfig(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("数据库配置不能为空");
        }
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("数据库主机地址不能为空");
        }
        if (config.getPort() <= 0 || config.getPort() > 65535) {
            throw new IllegalArgumentException("数据库端口无效: " + config.getPort());
        }
        if (config.getUsername() == null || config.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("数据库用户名不能为空");
        }
    }
    
    /**
     * 验证SQL语句
     */
    private void validateSql(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }
        String trimmedSql = sql.trim();
        if (trimmedSql.isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }
    }
    
    /**
     * 处理结果集
     */
    private QueryResult processResultSet(ResultSet rs, long startTime) throws SQLException {
        QueryResult result = new QueryResult();
        
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }
        
        List<List<Object>> rows = new ArrayList<>();
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
     * 获取数据库连接
     */
    private Connection getConnection(DatabaseConfig config) throws SQLException {
        String url = buildConnectionUrl(config);
        Properties props = buildConnectionProperties(config);
        
        return DriverManager.getConnection(url, props);
    }
    
    /**
     * 构建连接URL
     */
    private String buildConnectionUrl(DatabaseConfig config) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:postgresql://");
        url.append(config.getHost());
        url.append(":");
        url.append(config.getPort());
        
        if (config.getDatabase() != null && !config.getDatabase().trim().isEmpty()) {
            url.append("/").append(config.getDatabase());
        }
        
        return url.toString();
    }
    
    /**
     * 构建连接属性
     */
    private Properties buildConnectionProperties(DatabaseConfig config) {
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword());
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("loginTimeout", "30");
        props.setProperty("connectTimeout", "30");
        return props;
    }
    
    /**
     * 关闭资源
     */
    private void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    log.debug("关闭资源失败: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * 检查是否为PostgreSQL特殊查询
     */
    private boolean isPostgreSQLSpecialQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        String lowerSql = sql.trim().toLowerCase();
        return lowerSql.startsWith("select version()") ||
               lowerSql.startsWith("show") ||
               lowerSql.startsWith("pg_") ||
               lowerSql.contains("information_schema") ||
               lowerSql.contains("pg_catalog");
    }
    
    /**
     * 执行PostgreSQL特殊查询
     */
    private QueryResult executePostgreSQLSpecialQuery(Statement stmt, String sql) throws SQLException {
        long startTime = System.currentTimeMillis();
        
        try {
            ResultSet rs = stmt.executeQuery(sql);
            return processResultSet(rs, startTime);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                "SQL_EXECUTION_FAILED",
                "PostgreSQL特殊查询执行失败: " + e.getMessage(),
                "SPECIAL_QUERY",
                e
            );
        }
    }
    
    
    
    /**
     * 获取表行数
     */
    private int getTableRowCount(Connection conn, String database, String tableName) {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            String dbPrefix = database != null && !database.isEmpty() ? database + "." : "";
            
            String countSql = "SELECT COUNT(*) FROM " + dbPrefix + tableName;
            rs = stmt.executeQuery(countSql);
            
            if (rs.next()) {
                return (int) rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            log.warn("获取PostgreSQL表 {} 行数失败: {}", tableName, e.getMessage());
            return 0;
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    /**
     * 获取列信息
     */
    private List<ColumnInfo> getColumnInfo(Connection conn, String database, String tableName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            String dbPrefix = database != null && !database.isEmpty() ? database + "." : "";
            
            // PostgreSQL获取列信息
            rs = stmt.executeQuery(
                "SELECT column_name, data_type, is_nullable, column_default, column_comment " +
                "FROM information_schema.columns " +
                "WHERE table_name = '" + tableName + "' AND table_schema = 'public' " +
                "ORDER BY ordinal_position"
            );
            
            List<ColumnInfo> columns = new ArrayList<>();
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("column_name"));
                column.setType(rs.getString("data_type"));
                column.setNullable("YES".equals(rs.getString("is_nullable")));
                column.setComment(rs.getString("column_comment"));
                column.setPrimaryKey(false); // 简化实现，实际应查询pg_constraint
                
                columns.add(column);
            }
            
            return columns;
            
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    /**
     * 创建连接池（按需）
     */
    @Override
    public Object createConnectionPool(DatabaseConfig config) {
        validateConfig(config);
        
        try {
            log.info("创建PostgreSQL连接池，配置: {}:{}", config.getHost(), config.getPort());
            
            if (testConnection(config)) {
                log.info("PostgreSQL连接池创建成功");
                return new PostgreSQLConnectionPool(config);
            } else {
                throw new DatabaseOperationException(
                    "CONNECTION_FAILED",
                    "PostgreSQL连接池创建失败：连接测试失败"
                );
            }
        } catch (Exception e) {
            log.error("创建PostgreSQL连接池失败: {}", e.getMessage());
            throw new DatabaseOperationException(
                "CONNECTION_FAILED",
                "PostgreSQL连接池创建失败: " + e.getMessage(),
                "CREATE_CONNECTION_POOL",
                e
            );
        }
    }
    
    /**
     * 关闭连接器
     */
    @Override
    public void close() {
        log.debug("关闭PostgreSQL连接器");
        // PostgreSQL连接器在Spring容器关闭时会自动清理资源
        // 这里可以添加额外的清理逻辑（如连接池关闭等）
    }
    
    /**
     * PostgreSQL连接池实现
     */
    private static class PostgreSQLConnectionPool {
        private final DatabaseConfig config;
        private volatile boolean active = true;
        
        public PostgreSQLConnectionPool(DatabaseConfig config) {
            this.config = config;
        }
        
        public Connection getConnection() throws SQLException {
            if (!active) {
                throw new SQLException("PostgreSQL连接池已关闭");
            }
            return getDirectConnection();
        }
        
        public void close() {
            active = false;
        }
        
        public boolean isActive() {
            return active;
        }
        
        private Connection getDirectConnection() throws SQLException {
            String url = "jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("useSSL", "false");
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("loginTimeout", "30");
            props.setProperty("connectTimeout", "30");
            
            return DriverManager.getConnection(url, props);
        }
    }
}