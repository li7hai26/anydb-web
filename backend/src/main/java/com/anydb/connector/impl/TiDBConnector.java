package com.anydb.connector.impl;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * TiDB数据库连接器实现 (与MySQL兼容)
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class TiDBConnector implements DatabaseConnector {
    
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.TIDB;
    }
    
    @Override
    public boolean testConnection(DatabaseConfig config) {
        Connection conn = null;
        try {
            conn = getConnection(config);
            // 测试TiDB特有的查询
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT tidb_version()");
            return rs.next();
        } catch (Exception e) {
            log.warn("TiDB连接测试失败: {}", e.getMessage());
            return false;
        } finally {
            closeConnection(conn);
        }
    }
    
    @Override
    public QueryResult executeQuery(DatabaseConfig config, String sql) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            // 检查是否是TiDB特有的SQL
            if (sql.trim().toLowerCase().contains("explain")) {
                return executeExplainQuery(stmt, sql);
            }
            
            rs = stmt.executeQuery(sql);
            
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
            
            QueryResult result = new QueryResult();
            result.setColumns(columns);
            result.setRows(rows);
            result.setTotal(rows.size());
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("TiDB SQL执行失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public UpdateResult executeUpdate(DatabaseConfig config, String sql) {
        Connection conn = null;
        Statement stmt = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            conn = getConnection(config);
            stmt = conn.createStatement();
            int affectedRows = stmt.executeUpdate(sql);
            
            UpdateResult result = new UpdateResult();
            result.setAffectedRows(affectedRows);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            result.setMessage("TiDB操作成功");
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    @Override
    public List<String> getDatabases(DatabaseConfig config) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            // TiDB支持SHOW DATABASES
            rs = stmt.executeQuery("SHOW DATABASES");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
            
            return databases;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取TiDB数据库列表失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public List<TableInfo> getTables(DatabaseConfig config, String database) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            StringBuilder sql = new StringBuilder("SHOW TABLES");
            if (database != null && !database.isEmpty()) {
                sql.append(" FROM ").append(database);
            }
            
            rs = stmt.executeQuery(sql.toString());
            
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                TableInfo table = new TableInfo();
                String tableName = rs.getString(1);
                table.setName(tableName);
                table.setComment("TiDB分布式表");
                
                // 获取表的行数和创建时间
                List<Object> tableInfo = getTableDetailedInfo(conn, database, tableName);
                if (tableInfo.size() >= 2) {
                    table.setRowCount((Integer) tableInfo.get(0));
                    table.setCreateTime((Long) tableInfo.get(1));
                } else {
                    table.setRowCount(0);
                }
                
                tables.add(table);
            }
            
            return tables;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取TiDB表列表失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public TableInfo getTableInfo(DatabaseConfig config, String database, String tableName) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            // 获取表详细信息
            List<Object> tableInfo = getTableDetailedInfo(conn, database, tableName);
            if (tableInfo.isEmpty()) {
                return null;
            }
            
            TableInfo table = new TableInfo();
            table.setName(tableName);
            table.setComment("TiDB分布式表");
            table.setRowCount((Integer) tableInfo.get(0));
            table.setCreateTime((Long) tableInfo.get(1));
            
            // 获取列信息
            List<ColumnInfo> columns = getColumnInfo(conn, database, tableName);
            table.setColumns(columns);
            
            return table;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取TiDB表信息失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public QueryResult getTableData(DatabaseConfig config, String database, String tableName, 
                                  int page, int size, String orderBy, String orderDirection) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            conn = getConnection(config);
            stmt = conn.createStatement();
            
            StringBuilder sql = new StringBuilder("SELECT * FROM ");
            if (database != null && !database.isEmpty()) {
                sql.append(database).append(".");
            }
            sql.append(tableName);
            
            if (orderBy != null && !orderBy.isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
                if (orderDirection != null && !orderDirection.isEmpty()) {
                    sql.append(" ").append(orderDirection.toUpperCase());
                }
            }
            
            // TiDB分页：支持LIMIT OFFSET，也支持OFFSET LIMIT
            sql.append(" LIMIT ").append(size).append(" OFFSET ").append((page - 1) * size);
            
            rs = stmt.executeQuery(sql.toString());
            
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
            
            QueryResult result = new QueryResult();
            result.setColumns(columns);
            result.setRows(rows);
            result.setTotal(rows.size());
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取TiDB表数据失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public Object createConnectionPool(DatabaseConfig config) {
        log.info("为TiDB创建连接池，配置: {}:{}", config.getHost(), config.getPort());
        
        if (testConnection(config)) {
            return new TiDBConnectionPool(config);
        } else {
            throw new RuntimeException("创建TiDB连接池失败：连接测试失败");
        }
    }
    
    @Override
    public void close() {
        // 清理资源
    }
    
    private Connection getConnection(DatabaseConfig config) throws SQLException {
        String url = config.buildUrl();
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword());
        props.setProperty("useSSL", "false");
        props.setProperty("serverTimezone", "Asia/Shanghai");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("allowMultiQueries", "true"); // TiDB支持多查询
        props.setProperty("useAffectedRows", "false");
        
        return DriverManager.getConnection(url, props);
    }
    
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // 忽略关闭异常
            }
        }
    }
    
    private void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    
    private QueryResult executeExplainQuery(Statement stmt, String sql) throws SQLException {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行EXPLAIN查询获取执行计划
            ResultSet rs = stmt.executeQuery("EXPLAIN " + sql.replaceAll("(?i)^EXPLAIN\\s+", ""));
            
            List<String> columns = new ArrayList<>();
            columns.add("execution_plan");
            
            List<List<Object>> rows = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                row.add(rs.getString(1)); // 执行计划
                rows.add(row);
            }
            
            QueryResult result = new QueryResult();
            result.setColumns(columns);
            result.setRows(rows);
            result.setTotal(rows.size());
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("执行TiDB EXPLAIN查询失败: " + e.getMessage(), e);
        }
    }
    
    private List<Object> getTableDetailedInfo(Connection conn, String database, String tableName) {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            String dbPrefix = database != null && !database.isEmpty() ? database + "." : "";
            
            // 获取表统计信息
            String countSql = "SELECT COUNT(*) FROM " + dbPrefix + tableName;
            rs = stmt.executeQuery(countSql);
            
            int rowCount = 0;
            if (rs.next()) {
                rowCount = (int) rs.getLong(1);
            }
            
            // 获取表创建时间
            String descSql = "SHOW CREATE TABLE " + dbPrefix + tableName;
            rs = stmt.executeQuery(descSql);
            
            long createTime = System.currentTimeMillis(); // 简化实现
            
            List<Object> result = new ArrayList<>();
            result.add(rowCount);
            result.add(createTime);
            
            return result;
            
        } catch (SQLException e) {
            log.warn("获取表详细信息失败: {}", e.getMessage());
            return new ArrayList<>();
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    private List<ColumnInfo> getColumnInfo(Connection conn, String database, String tableName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            String dbPrefix = database != null && !database.isEmpty() ? database + "." : "";
            
            rs = stmt.executeQuery("DESC " + dbPrefix + tableName);
            
            List<ColumnInfo> columns = new ArrayList<>();
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("Field"));
                column.setType(rs.getString("Type"));
                column.setComment(rs.getString("Comment"));
                column.setNullable("YES".equals(rs.getString("Null")));
                column.setDefaultValue(rs.getString("Default"));
                
                // TiDB列信息简化
                column.setPrimaryKey(false); // 需要额外查询判断主键
                
                columns.add(column);
            }
            
            return columns;
            
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    private static class TiDBConnectionPool {
        private final DatabaseConfig config;
        private volatile boolean active = true;
        
        public TiDBConnectionPool(DatabaseConfig config) {
            this.config = config;
        }
        
        public Connection getConnection() throws SQLException {
            if (!active) {
                throw new SQLException("TiDB连接池已关闭");
            }
            
            String url = config.buildUrl();
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "Asia/Shanghai");
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("allowMultiQueries", "true");
            props.setProperty("useAffectedRows", "false");
            
            return DriverManager.getConnection(url, props);
        }
        
        public void close() {
            active = false;
        }
        
        public boolean isActive() {
            return active;
        }
    }
}
