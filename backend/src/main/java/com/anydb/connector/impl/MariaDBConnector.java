package com.anydb.connector.impl;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * MariaDB数据库连接器实现 (与MySQL兼容)
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class MariaDBConnector implements DatabaseConnector {
    
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.MARIADB;
    }
    
    @Override
    public boolean testConnection(DatabaseConfig config) {
        Connection conn = null;
        try {
            conn = getConnection(config);
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
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
            throw new RuntimeException("MariaDB SQL执行失败: " + e.getMessage(), e);
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
            result.setMessage("操作成功");
            
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
            rs = stmt.executeQuery("SHOW DATABASES");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
            
            return databases;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库列表失败: " + e.getMessage(), e);
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
            if (conn == null) {
                throw new RuntimeException("无法获取MariaDB连接");
            }
            stmt = conn.createStatement();
            
            StringBuilder sql = new StringBuilder("SHOW TABLE STATUS");
            if (database != null && !database.isEmpty()) {
                String safeDatabase = database.replaceAll("[^a-zA-Z0-9_]", "");
                if (!safeDatabase.isEmpty()) {
                    sql.append(" FROM ").append(safeDatabase);
                }
            }
            
            rs = stmt.executeQuery(sql.toString());
            
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("Name"));
                table.setComment(rs.getString("Comment"));
                table.setRowCount(rs.getInt("Rows"));
                
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
            
            return tables;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
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
            
            StringBuilder sql = new StringBuilder("SHOW TABLE STATUS");
            if (database != null && !database.isEmpty()) {
                sql.append(" FROM ").append(database);
            }
            sql.append(" WHERE Name = '").append(tableName).append("'");
            
            rs = stmt.executeQuery(sql.toString());
            
            if (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("Name"));
                table.setComment(rs.getString("Comment"));
                table.setRowCount(rs.getInt("Rows"));
                
                Timestamp createTime = rs.getTimestamp("Create_time");
                if (createTime != null) {
                    table.setCreateTime(createTime.getTime());
                }
                
                Timestamp updateTime = rs.getTimestamp("Update_time");
                if (updateTime != null) {
                    table.setUpdateTime(updateTime.getTime());
                }
                
                List<ColumnInfo> columns = getColumnInfo(conn, database, tableName);
                table.setColumns(columns);
                
                return table;
            }
            
            return null;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取表信息失败: " + e.getMessage(), e);
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
            throw new RuntimeException("获取表数据失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public Object createConnectionPool(DatabaseConfig config) {
        log.info("为MariaDB创建连接池，配置: {}:{}", config.getHost(), config.getPort());
        
        if (testConnection(config)) {
            return new MariaDBConnectionPool(config);
        } else {
            throw new RuntimeException("创建MariaDB连接池失败：连接测试失败");
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
    
    private List<ColumnInfo> getColumnInfo(Connection conn, String database, String tableName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            StringBuilder sql = new StringBuilder("SHOW FULL COLUMNS FROM ");
            if (database != null && !database.isEmpty()) {
                sql.append(database).append(".");
            }
            sql.append(tableName);
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());
            
            List<ColumnInfo> columns = new ArrayList<>();
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("Field"));
                column.setType(rs.getString("Type"));
                column.setComment(rs.getString("Comment"));
                column.setNullable("YES".equals(rs.getString("Null")));
                column.setDefaultValue(rs.getString("Default"));
                
                String key = rs.getString("Key");
                column.setPrimaryKey("PRI".equals(key));
                
                columns.add(column);
            }
            
            return columns;
            
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    private static class MariaDBConnectionPool {
        private final DatabaseConfig config;
        private volatile boolean active = true;
        
        public MariaDBConnectionPool(DatabaseConfig config) {
            this.config = config;
        }
        
        public Connection getConnection() throws SQLException {
            if (!active) {
                throw new SQLException("连接池已关闭");
            }
            
            String url = config.buildUrl();
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "Asia/Shanghai");
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            
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
