package com.anydb.connector;

import java.util.List;

/**
 * 查询结果类
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public class QueryResult {
    
    /**
     * 列名列表
     */
    private List<String> columns;
    
    /**
     * 数据行列表
     */
    private List<List<Object>> rows;
    
    /**
     * 总行数
     */
    private int total;
    
    /**
     * 执行时间（毫秒）
     */
    private long executionTime;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    public QueryResult() {
        this.success = true;
    }
    
    // Getters and Setters
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
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}