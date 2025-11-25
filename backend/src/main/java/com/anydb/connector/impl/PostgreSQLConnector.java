package com.anydb.connector.impl;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseType;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL数据库连接器实现
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Component
public class PostgreSQLConnector implements DatabaseConnector {
    
    @Override
    public DatabaseType getSupportedType() {
        return DatabaseType.POSTGRESQL;
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
                    Object value = rs.getObject(i);
                    row.add(value);
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
            throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
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
            rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname");
            
            List<String> databases = new ArrayList<>();
            while (rs.next()) {
                databases.add(rs.getString("datname"));
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
            stmt = conn.createStatement();
            
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT schemaname, tablename, tableowner, hasindexes, hasrules, hastriggers ");
            sql.append("FROM pg_tables ");
            if (database != null && !database.isEmpty()) {
                sql.append("WHERE schemaname = '").append(database).append("' ");
            }
            sql.append("ORDER BY schemaname, tablename");
            
            rs = stmt.executeQuery(sql.toString());
            
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                TableInfo table = new TableInfo();
                table.setName(rs.getString("tablename"));
                table.setComment(rs.getString("tableowner"));
                
                // 获取表行数（仅在必要时）
                table.setRowCount(0); // 可以通过count查询获取
                
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
            
            // 获取表基本信息
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT t.tablename, c.column_name, c.data_type, c.is_nullable, ");
            sql.append("c.column_default, c.character_maximum_length, c.numeric_precision, ");
            sql.append("c.numeric_scale, pgd.description ");
            sql.append("FROM pg_tables t ");
            sql.append("JOIN information_schema.columns c ON c.table_name = t.tablename AND c.table_schema = t.schemaname ");
            sql.append("LEFT JOIN pg_class pc ON pc.relname = t.tablename ");
            sql.append("LEFT JOIN pg_description pgd ON pgd.objoid = pc.oid AND pgd.objsubid = c.ordinal_position ");
            sql.append("WHERE t.schemaname = '").append(database).append("' AND t.tablename = '").append(tableName).append("'");
            
            rs = stmt.executeQuery(sql.toString());
            
            TableInfo table = null;
            List<ColumnInfo> columns = new ArrayList<>();
            
            while (rs.next()) {
                if (table == null) {
                    table = new TableInfo();
                    table.setName(rs.getString("tablename"));
                    table.setColumns(columns);
                }
                
                ColumnInfo column = new ColumnInfo();
                column.setName(rs.getString("column_name"));
                column.setType(rs.getString("data_type"));
                column.setComment(rs.getString("description"));
                column.setNullable("YES".equals(rs.getString("is_nullable")));
                column.setDefaultValue(rs.getString("column_default"));
                column.setMaxLength(rs.getObject("character_maximum_length") != null ? 
                    ((Number) rs.getObject("character_maximum_length")).intValue() : null);
                column.setPrecision(rs.getObject("numeric_precision") != null ? 
                    ((Number) rs.getObject("numeric_precision")).intValue() : null);
                column.setScale(rs.getObject("numeric_scale") != null ? 
                    ((Number) rs.getObject("numeric_scale")).intValue() : null);
                
                columns.add(column);
            }
            
            return table;
            
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
            
            // 构建分页查询SQL
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
                    Object value = rs.getObject(i);
                    row.add(value);
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
    public void close() {
        // 清理资源
    }
    
    /**
     * 获取数据库连接
     */
    private Connection getConnection(DatabaseConfig config) throws SQLException {
        String url = config.buildUrl();
        return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
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
}