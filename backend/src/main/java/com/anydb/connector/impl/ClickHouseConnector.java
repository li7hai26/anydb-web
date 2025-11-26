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
 * ClickHouse数据库连接器实现 (列式数据库)
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class ClickHouseConnector implements DatabaseConnector {
    
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.CLICKHOUSE;
    }
    
    @Override
    public boolean testConnection(DatabaseConfig config) {
        Connection conn = null;
        try {
            conn = getConnection(config);
            // ClickHouse特殊查询测试
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            return rs.next();
        } catch (Exception e) {
            log.warn("ClickHouse连接测试失败: {}", e.getMessage());
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
            
            // ClickHouse支持特殊的分析型查询
            if (isClickHouseSpecialQuery(sql)) {
                return executeClickHouseSpecialQuery(stmt, sql);
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
            throw new RuntimeException("ClickHouse SQL执行失败: " + e.getMessage(), e);
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
            result.setMessage("ClickHouse操作成功");
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("ClickHouse SQL执行失败: " + e.getMessage(), e);
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
            
            // ClickHouse获取数据库列表
            rs = stmt.executeQuery("SHOW DATABASES");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
            
            return databases;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取ClickHouse数据库列表失败: " + e.getMessage(), e);
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
                table.setName(rs.getString(1));
                table.setComment("ClickHouse表");
                
                // ClickHouse表行数统计 - 简化实现
                table.setRowCount(getTableRowCount(conn, database, table.getName()));
                
                tables.add(table);
            }
            
            return tables;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取ClickHouse表列表失败: " + e.getMessage(), e);
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
            
            TableInfo table = new TableInfo();
            table.setName(tableName);
            table.setComment("ClickHouse列式表");
            table.setRowCount(getTableRowCount(conn, database, tableName));
            
            // 获取列信息
            List<ColumnInfo> columns = getColumnInfo(conn, database, tableName);
            table.setColumns(columns);
            
            return table;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取ClickHouse表信息失败: " + e.getMessage(), e);
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
            
            // ClickHouse分页：使用LIMIT和OFFSET
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
            throw new RuntimeException("获取ClickHouse表数据失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public Object createConnectionPool(DatabaseConfig config) {
        log.info("为ClickHouse创建连接池，配置: {}:{}", config.getHost(), config.getPort());
        
        if (testConnection(config)) {
            return new ClickHouseConnectionPool(config);
        } else {
            throw new RuntimeException("创建ClickHouse连接池失败：连接测试失败");
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
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("serverTimezone", "Asia/Shanghai");
        props.setProperty("socketTimeout", "30000");
        
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
    
    private boolean isClickHouseSpecialQuery(String sql) {
        String lowerSql = sql.trim().toLowerCase();
        return lowerSql.startsWith("system") || 
               lowerSql.startsWith("grant") || 
               lowerSql.startsWith("revoke") ||
               lowerSql.contains("clickhouse") ||
               lowerSql.contains("replicated");
    }
    
    private QueryResult executeClickHouseSpecialQuery(Statement stmt, String sql) throws SQLException {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行ClickHouse特殊查询
            ResultSet rs = stmt.executeQuery(sql);
            
            List<String> columns = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columns.add(metaData.getColumnLabel(i));
            }
            
            List<List<Object>> rows = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
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
            throw new RuntimeException("执行ClickHouse特殊查询失败: " + e.getMessage(), e);
        }
    }
    
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
            log.warn("获取ClickHouse表 {} 行数失败: {}", tableName, e.getMessage());
            return 0;
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
            
            // ClickHouse获取列信息
            rs = stmt.executeQuery("DESCRIBE " + dbPrefix + tableName);
            
            List<ColumnInfo> columns = new ArrayList<>();
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("name"));
                column.setType(rs.getString("type"));
                column.setNullable(false); // ClickHouse列默认非空
                column.setComment(rs.getString("comment"));
                
                // ClickHouse没有传统的主键概念
                column.setPrimaryKey(false);
                
                columns.add(column);
            }
            
            return columns;
            
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    private static class ClickHouseConnectionPool {
        private final DatabaseConfig config;
        private volatile boolean active = true;
        
        public ClickHouseConnectionPool(DatabaseConfig config) {
            this.config = config;
        }
        
        public Connection getConnection() throws SQLException {
            if (!active) {
                throw new SQLException("ClickHouse连接池已关闭");
            }
            
            String url = config.buildUrl();
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("serverTimezone", "Asia/Shanghai");
            props.setProperty("socketTimeout", "30000");
            
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
