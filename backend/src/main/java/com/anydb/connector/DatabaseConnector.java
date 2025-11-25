package com.anydb.connector;

import java.util.List;
import java.util.Map;

/**
 * 数据库连接器接口
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public interface DatabaseConnector {
    
    /**
     * 测试数据库连接
     */
    boolean testConnection(DatabaseConfig config);
    
    /**
     * 执行SQL查询
     */
    QueryResult executeQuery(DatabaseConfig config, String sql);
    
    /**
     * 执行SQL更新（INSERT, UPDATE, DELETE等）
     */
    UpdateResult executeUpdate(DatabaseConfig config, String sql);
    
    /**
     * 获取数据库列表
     */
    List<String> getDatabases(DatabaseConfig config);
    
    /**
     * 获取表列表
     */
    List<TableInfo> getTables(DatabaseConfig config, String database);
    
    /**
     * 获取表结构信息
     */
    TableInfo getTableInfo(DatabaseConfig config, String database, String tableName);
    
    /**
     * 获取表数据
     */
    QueryResult getTableData(DatabaseConfig config, String database, String tableName, 
                           int page, int size, String orderBy, String orderDirection);
    
    /**
     * 获取支持的数据库类型
     */
    DatabaseType getSupportedType();
    
    /**
     * 关闭连接
     */
    void close();
    
    /**
     * 查询结果
     */
    class QueryResult {
        private List<String> columns;
        private List<List<Object>> rows;
        private Integer total;
        private Long executionTime;
        
        public List<String> getColumns() {
            return columns;
        }
        
        public void setColumns(List<String> columns) {
            this.columns = columns;
        }
        
        public List<List<Object>> getRows() {
            return rows;
        }
        
        public void setRows(List<List<Object>> rows) {
            this.rows = rows;
        }
        
        public Integer getTotal() {
            return total;
        }
        
        public void setTotal(Integer total) {
            this.total = total;
        }
        
        public Long getExecutionTime() {
            return executionTime;
        }
        
        public void setExecutionTime(Long executionTime) {
            this.executionTime = executionTime;
        }
    }
    
    /**
     * 更新结果
     */
    class UpdateResult {
        private Integer affectedRows;
        private Long executionTime;
        private String message;
        
        public Integer getAffectedRows() {
            return affectedRows;
        }
        
        public void setAffectedRows(Integer affectedRows) {
            this.affectedRows = affectedRows;
        }
        
        public Long getExecutionTime() {
            return executionTime;
        }
        
        public void setExecutionTime(Long executionTime) {
            this.executionTime = executionTime;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    /**
     * 表信息
     */
    class TableInfo {
        private String name;
        private String comment;
        private List<ColumnInfo> columns;
        private Integer rowCount;
        private Long createTime;
        private Long updateTime;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String comment) {
            this.comment = comment;
        }
        
        public List<ColumnInfo> getColumns() {
            return columns;
        }
        
        public void setColumns(List<ColumnInfo> columns) {
            this.columns = columns;
        }
        
        public Integer getRowCount() {
            return rowCount;
        }
        
        public void setRowCount(Integer rowCount) {
            this.rowCount = rowCount;
        }
        
        public Long getCreateTime() {
            return createTime;
        }
        
        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }
        
        public Long getUpdateTime() {
            return updateTime;
        }
        
        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }
    }
    
    /**
     * 列信息
     */
    class ColumnInfo {
        private String name;
        private String type;
        private String comment;
        private Boolean nullable;
        private Boolean primaryKey;
        private Integer maxLength;
        private Integer precision;
        private Integer scale;
        private String defaultValue;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String comment) {
            this.comment = comment;
        }
        
        public Boolean getNullable() {
            return nullable;
        }
        
        public void setNullable(Boolean nullable) {
            this.nullable = nullable;
        }
        
        public Boolean getPrimaryKey() {
            return primaryKey;
        }
        
        public void setPrimaryKey(Boolean primaryKey) {
            this.primaryKey = primaryKey;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }
        
        public Integer getPrecision() {
            return precision;
        }
        
        public void setPrecision(Integer precision) {
            this.precision = precision;
        }
        
        public Integer getScale() {
            return scale;
        }
        
        public void setScale(Integer scale) {
            this.scale = scale;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
        
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}