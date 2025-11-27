package com.anydb.connector;

/**
 * 更新结果类
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public class UpdateResult {
    
    /**
     * 受影响的行数
     */
    private int affectedRows;
    
    /**
     * 执行时间（毫秒）
     */
    private long executionTime;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    public UpdateResult() {
        this.success = true;
    }
    
    // Getters and Setters
    public int getAffectedRows() {
        return affectedRows;
    }
    
    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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