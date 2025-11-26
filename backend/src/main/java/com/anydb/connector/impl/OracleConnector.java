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
 * Oracle数据库连接器实现
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class OracleConnector implements DatabaseConnector {
    
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.ORACLE;
    }
    
    @Override
    public boolean testConnection(DatabaseConfig config) {
        Connection conn = null;
        try {
            conn = getConnection(config);
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            log.warn("Oracle连接测试失败: {}", e.getMessage());
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
            throw new RuntimeException("Oracle SQL执行失败: " + e.getMessage(), e);
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
            result.setMessage("Oracle操作成功");
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("Oracle SQL执行失败: " + e.getMessage(), e);
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
            
            // Oracle中，没有传统意义上的数据库概念
            // 这里是返回用户表空间信息或schemas
            rs = stmt.executeQuery("SELECT DISTINCT TABLESPACE_NAME FROM USER_TABLES ORDER BY TABLESPACE_NAME");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
            
            // 如果没有找到表空间，返回默认的表空间
            if (databases.isEmpty()) {
                rs = stmt.executeQuery("SELECT DEFAULT_TABLESPACE FROM USER_USERS");
                if (rs.next()) {
                    databases.add(rs.getString(1));
                }
            }
            
            return databases;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取Oracle表空间列表失败: " + e.getMessage(), e);
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
            
            StringBuilder sql = new StringBuilder("SELECT TABLE_NAME, COMMENTS, NUM_ROWS, LAST_ANALYZED FROM USER_TABLES");
            
            if (database != null && !database.isEmpty()) {
                // Oracle中的表空间查询
                sql.append(" WHERE TABLESPACE_NAME = '").append(database).append("'");
            }
            
            sql.append(" ORDER BY TABLE_NAME");
            
            rs = stmt.executeQuery(sql.toString());
            
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("TABLE_NAME"));
                table.setComment(rs.getString("COMMENTS"));
                
                // Oracle中的行数统计需要统计，不是直接存储的
                String numRows = rs.getString("NUM_ROWS");
                if (numRows != null && !numRows.isEmpty()) {
                    try {
                        table.setRowCount(Integer.parseInt(numRows));
                    } catch (NumberFormatException e) {
                        table.setRowCount(0);
                    }
                } else {
                    table.setRowCount(0);
                }
                
                Timestamp lastAnalyzed = rs.getTimestamp("LAST_ANALYZED");
                if (lastAnalyzed != null) {
                    table.setUpdateTime(lastAnalyzed.getTime());
                }
                
                tables.add(table);
            }
            
            return tables;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取Oracle表列表失败: " + e.getMessage(), e);
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
            
            // 获取表基本信息
            StringBuilder sql = new StringBuilder("SELECT TABLE_NAME, COMMENTS, NUM_ROWS, LAST_ANALYZED FROM USER_TABLES");
            sql.append(" WHERE TABLE_NAME = '").append(tableName.toUpperCase()).append("'");
            
            rs = stmt.executeQuery(sql.toString());
            
            if (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("TABLE_NAME"));
                table.setComment(rs.getString("COMMENTS"));
                
                String numRows = rs.getString("NUM_ROWS");
                if (numRows != null && !numRows.isEmpty()) {
                    try {
                        table.setRowCount(Integer.parseInt(numRows));
                    } catch (NumberFormatException e) {
                        table.setRowCount(getTableRowCount(conn, tableName));
                    }
                } else {
                    table.setRowCount(getTableRowCount(conn, tableName));
                }
                
                Timestamp lastAnalyzed = rs.getTimestamp("LAST_ANALYZED");
                if (lastAnalyzed != null) {
                    table.setUpdateTime(lastAnalyzed.getTime());
                }
                
                // 获取列信息
                List<ColumnInfo> columns = getColumnInfo(conn, tableName);
                table.setColumns(columns);
                
                return table;
            }
            
            return null;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取Oracle表信息失败: " + e.getMessage(), e);
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
            
            // Oracle分页查询使用ROWNUM
            StringBuilder sql = new StringBuilder("SELECT * FROM (");
            sql.append("SELECT a.*, ROWNUM r FROM (");
            
            // 主查询
            sql.append("SELECT * FROM ").append(tableName);
            
            if (orderBy != null && !orderBy.isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
                if (orderDirection != null && !orderDirection.isEmpty()) {
                    sql.append(" ").append(orderDirection.toUpperCase());
                }
            }
            
            sql.append(") a WHERE ROWNUM <= ").append(page * size);
            sql.append(") WHERE r > ").append((page - 1) * size);
            
            rs = stmt.executeQuery(sql.toString());
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                // Oracle中ROWNUM会作为额外的列返回，跳过它
                if (!"R".equals(columnName)) {
                    columns.add(columnName);
                }
            }
            
            List<List<Object>> rows = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    // Oracle中ROWNUM会作为额外的列返回，跳过它
                    if (!"R".equals(columnName)) {
                        row.add(rs.getObject(i));
                    }
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
            throw new RuntimeException("获取Oracle表数据失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public Object createConnectionPool(DatabaseConfig config) {
        try {
            if (testConnection(config)) {
                return new OracleConnectionPool(config);
            } else {
                throw new RuntimeException("创建Oracle连接池失败：连接测试失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("创建Oracle连接池失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void close() {
        // 清理资源
    }
    
    /**
     * 获取数据库连接
     */
    private Connection getConnection(DatabaseConfig config) throws SQLException {
        String url = config.buildUrl();
        Properties props = new Properties();
        props.setProperty("user", config.getUsername());
        props.setProperty("password", config.getPassword());
        props.setProperty("oracle.jdbc.defaultReadOnly", "false");
        props.setProperty("oracle.jdbc.defaultAutoCommit", "false");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        
        return DriverManager.getConnection(url, props);
    }
    
    /**
     * 关闭连接
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 关闭多个资源
     */
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
    
    /**
     * 获取列信息
     */
    private List<ColumnInfo> getColumnInfo(Connection conn, String tableName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("""
                SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, 
                       DATA_SCALE, NULLABLE, DATA_DEFAULT, COMMENTS
                FROM USER_TAB_COLUMNS 
                WHERE TABLE_NAME = ? 
                ORDER BY COLUMN_ID
                """.replace("?", "'" + tableName.toUpperCase() + "'"));
            
            List<ColumnInfo> columns = new ArrayList<>();
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("COLUMN_NAME"));
                column.setType(rs.getString("DATA_TYPE"));
                column.setNullable("Y".equals(rs.getString("NULLABLE")));
                column.setDefaultValue(rs.getString("DATA_DEFAULT"));
                column.setComment(rs.getString("COMMENTS"));
                
                // 判断是否为主键
                Boolean isPrimaryKey = isPrimaryKeyColumn(conn, tableName, column.getName());
                column.setPrimaryKey(isPrimaryKey);
                
                columns.add(column);
            }
            
            return columns;
            
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    /**
     * 判断列是否为主键
     */
    private boolean isPrimaryKeyColumn(Connection conn, String tableName, String columnName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(String.format("""
                SELECT COUNT(*) FROM USER_CONS_COLUMNS 
                WHERE TABLE_NAME = '%s' 
                AND COLUMN_NAME = '%s' 
                AND CONSTRAINT_NAME IN (
                    SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS 
                    WHERE CONSTRAINT_TYPE = 'P' AND TABLE_NAME = '%s'
                )
                """, tableName.toUpperCase(), columnName, tableName.toUpperCase()));
            
            return rs.next() && rs.getInt(1) > 0;
            
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    /**
     * 获取表行数
     */
    private int getTableRowCount(Connection conn, String tableName) {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            
            if (rs.next()) {
                return (int) rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            log.warn("获取Oracle表 {} 行数失败: {}", tableName, e.getMessage());
            return 0;
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    /**
     * Oracle连接池实现
     */
    private static class OracleConnectionPool {
        private final DatabaseConfig config;
        private volatile boolean active = true;
        
        public OracleConnectionPool(DatabaseConfig config) {
            this.config = config;
        }
        
        public Connection getConnection() throws SQLException {
            if (!active) {
                throw new SQLException("Oracle连接池已关闭");
            }
            
            String url = config.buildUrl();
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("oracle.jdbc.defaultReadOnly", "false");
            props.setProperty("oracle.jdbc.defaultAutoCommit", "false");
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
