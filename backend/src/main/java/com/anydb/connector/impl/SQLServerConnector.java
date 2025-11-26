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
 * SQL Server数据库连接器实现
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class SQLServerConnector implements DatabaseConnector {
    
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.SQLSERVER;
    }
    
    @Override
    public boolean testConnection(DatabaseConfig config) {
        Connection conn = null;
        try {
            conn = getConnection(config);
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            log.warn("SQL Server连接测试失败: {}", e.getMessage());
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
            throw new RuntimeException("SQL Server SQL执行失败: " + e.getMessage(), e);
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
            result.setMessage("SQL Server操作成功");
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("SQL Server SQL执行失败: " + e.getMessage(), e);
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
            rs = stmt.executeQuery("SELECT name FROM sys.databases ORDER BY name");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString("name"));
            }
            
            return databases;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取SQL Server数据库列表失败: " + e.getMessage(), e);
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
            
            StringBuilder sql = new StringBuilder("""
                SELECT TABLE_NAME, TABLE_COMMENT, 
                       CASE WHEN t.is_ms_shipped = 1 THEN 'System Table' ELSE 'User Table' END as TableType,
                       SUM(p.rows) as RowCounts
                FROM INFORMATION_SCHEMA.TABLES t
                LEFT JOIN sys.partitions p ON t.TABLE_NAME = OBJECT_NAME(p.object_id)
                WHERE TABLE_TYPE = 'BASE TABLE'
                """);
            
            if (database != null && !database.isEmpty()) {
                // SQL Server中，INFORMATION_SCHEMA.TABLES没有直接的database参数
                // 需要使用USE database;语句
                String useDbSql = "USE " + database;
                Statement useStmt = conn.createStatement();
                useStmt.execute(useDbSql);
                useStmt.close();
            }
            
            sql.append(" GROUP BY TABLE_NAME, TABLE_COMMENT, t.is_ms_shipped");
            sql.append(" ORDER BY TABLE_NAME");
            
            rs = stmt.executeQuery(sql.toString());
            
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("TABLE_NAME"));
                table.setComment(rs.getString("TABLE_COMMENT"));
                
                int rowCount = rs.getInt("RowCounts");
                if (!rs.wasNull()) {
                    table.setRowCount(rowCount);
                } else {
                    table.setRowCount(0);
                }
                
                tables.add(table);
            }
            
            return tables;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取SQL Server表列表失败: " + e.getMessage(), e);
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
            
            // 确保使用正确的数据库
            if (database != null && !database.isEmpty()) {
                String useDbSql = "USE " + database;
                Statement useStmt = conn.createStatement();
                useStmt.execute(useDbSql);
                useStmt.close();
            }
            
            // 获取表基本信息
            StringBuilder sql = new StringBuilder("""
                SELECT TABLE_NAME, TABLE_COMMENT,
                       CASE WHEN t.is_ms_shipped = 1 THEN 'System Table' ELSE 'User Table' END as TableType,
                       SUM(p.rows) as RowCounts
                FROM INFORMATION_SCHEMA.TABLES t
                LEFT JOIN sys.partitions p ON t.TABLE_NAME = OBJECT_NAME(p.object_id)
                WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_NAME = ?
                GROUP BY TABLE_NAME, TABLE_COMMENT, t.is_ms_shipped
                """);
            
            // SQL Server不支持参数化查询，所以手动替换
            sql = new StringBuilder(sql.toString().replace("TABLE_NAME = ?", "TABLE_NAME = '" + tableName + "'"));
            
            rs = stmt.executeQuery(sql.toString());
            
            if (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("TABLE_NAME"));
                table.setComment(rs.getString("TABLE_COMMENT"));
                
                int rowCount = rs.getInt("RowCounts");
                if (!rs.wasNull()) {
                    table.setRowCount(rowCount);
                } else {
                    table.setRowCount(getTableRowCount(conn, tableName));
                }
                
                // 获取列信息
                List<ColumnInfo> columns = getColumnInfo(conn, tableName);
                table.setColumns(columns);
                
                return table;
            }
            
            return null;
            
        } catch (SQLException e) {
            throw new RuntimeException("获取SQL Server表信息失败: " + e.getMessage(), e);
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
            
            // 确保使用正确的数据库
            if (database != null && !database.isEmpty()) {
                String useDbSql = "USE " + database;
                Statement useStmt = conn.createStatement();
                useStmt.execute(useDbSql);
                useStmt.close();
            }
            
            // SQL Server分页查询使用OFFSET FETCH
            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
            
            if (orderBy != null && !orderBy.isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
                if (orderDirection != null && !orderDirection.isEmpty()) {
                    sql.append(" ").append(orderDirection.toUpperCase());
                }
            }
            
            sql.append(" OFFSET ").append((page - 1) * size).append(" ROWS");
            sql.append(" FETCH NEXT ").append(size).append(" ROWS ONLY");
            
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
            throw new RuntimeException("获取SQL Server表数据失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    @Override
    public Object createConnectionPool(DatabaseConfig config) {
        try {
            if (testConnection(config)) {
                return new SQLServerConnectionPool(config);
            } else {
                throw new RuntimeException("创建SQL Server连接池失败：连接测试失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("创建SQL Server连接池失败: " + e.getMessage(), e);
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
        props.setProperty("encrypt", "false");
        props.setProperty("loginTimeout", "30");
        props.setProperty("socketTimeout", "300000");
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
            rs = stmt.executeQuery(String.format("""
                SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, 
                       IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = '%s' 
                ORDER BY ORDINAL_POSITION
                """, tableName));
            
            List<ColumnInfo> columns = new ArrayList<>();
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("COLUMN_NAME"));
                column.setType(rs.getString("DATA_TYPE"));
                column.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
                column.setComment(rs.getString("COLUMN_COMMENT"));
                
                int maxLength = rs.getInt("CHARACTER_MAXIMUM_LENGTH");
                if (!rs.wasNull()) {
                    column.setMaxLength(maxLength);
                }
                
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
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                WHERE TABLE_NAME = '%s' AND COLUMN_NAME = '%s'
                """, tableName, columnName));
            
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
            log.warn("获取SQL Server表 {} 行数失败: {}", tableName, e.getMessage());
            return 0;
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    /**
     * SQL Server连接池实现
     */
    private static class SQLServerConnectionPool {
        private final DatabaseConfig config;
        private volatile boolean active = true;
        
        public SQLServerConnectionPool(DatabaseConfig config) {
            this.config = config;
        }
        
        public Connection getConnection() throws SQLException {
            if (!active) {
                throw new SQLException("SQL Server连接池已关闭");
            }
            
            String url = config.buildUrl();
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            props.setProperty("encrypt", "false");
            props.setProperty("loginTimeout", "30");
            props.setProperty("socketTimeout", "300000");
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
