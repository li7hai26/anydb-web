package com.anydb.connector;

/**
 * 数据库操作异常
 * 
 * 用于包装所有数据库操作相关的异常，提供统一的错误处理机制。
 * 
 * 特点：
 * - 继承RuntimeException，无需强制声明
 * - 包含原始异常信息，便于问题定位
 * - 支持错误代码和错误消息的分离
 * - 提供多种构造函数以适应不同场景
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public class DatabaseOperationException extends RuntimeException {
    
    /**
     * 错误代码
     */
    private final String errorCode;
    
    /**
     * 错误消息
     */
    private final String errorMessage;
    
    /**
     * 异常发生时的操作类型
     */
    private final String operationType;
    
    /**
     * 构造函数 - 仅错误消息
     * 
     * @param message 错误消息
     */
    public DatabaseOperationException(String message) {
        super(message);
        this.errorCode = "DB_OP_ERROR";
        this.errorMessage = message;
        this.operationType = "UNKNOWN";
    }
    
    /**
     * 构造函数 - 错误消息和原因
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DB_OP_ERROR";
        this.errorMessage = message;
        this.operationType = "UNKNOWN";
    }
    
    /**
     * 构造函数 - 错误代码和消息
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     */
    public DatabaseOperationException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.operationType = "UNKNOWN";
    }
    
    /**
     * 构造函数 - 完整参数
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @param operationType 操作类型
     * @param cause 原始异常
     */
    public DatabaseOperationException(String errorCode, String errorMessage, 
                                    String operationType, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.operationType = operationType;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 获取操作类型
     * 
     * @return 操作类型
     */
    public String getOperationType() {
        return operationType;
    }
    
    /**
     * 获取完整的错误信息
     * 
     * @return 完整的错误信息
     */
    public String getFullMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("错误代码: ").append(errorCode)
          .append(" | 操作类型: ").append(operationType)
          .append(" | 错误信息: ").append(errorMessage);
        
        if (getCause() != null) {
            sb.append(" | 原始异常: ").append(getCause().getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * 检查是否为连接异常
     * 
     * @return 是否为连接异常
     */
    public boolean isConnectionError() {
        return "CONNECTION_ERROR".equals(errorCode) || 
               errorMessage != null && errorMessage.toLowerCase().contains("connection");
    }
    
    /**
     * 检查是否为SQL语法异常
     * 
     * @return 是否为SQL语法异常
     */
    public boolean isSQLSyntaxError() {
        return "SQL_SYNTAX_ERROR".equals(errorCode) ||
               errorMessage != null && errorMessage.toLowerCase().contains("syntax");
    }
    
    /**
     * 检查是否为权限异常
     * 
     * @return 是否为权限异常
     */
    public boolean isPermissionError() {
        return "PERMISSION_ERROR".equals(errorCode) ||
               errorMessage != null && errorMessage.toLowerCase().contains("permission");
    }
    
    /**
     * 检查是否为超时异常
     * 
     * @return 是否为超时异常
     */
    public boolean isTimeoutError() {
        return "TIMEOUT_ERROR".equals(errorCode) ||
               errorMessage != null && errorMessage.toLowerCase().contains("timeout");
    }
    
    @Override
    public String toString() {
        return getFullMessage();
    }
    
    /**
     * 预设的错误代码常量
     */
    public static class ErrorCodes {
        
        /** 连接错误 */
        public static final String CONNECTION_ERROR = "DB_CONN_001";
        
        /** SQL语法错误 */
        public static final String SQL_SYNTAX_ERROR = "DB_SQL_001";
        
        /** 权限不足错误 */
        public static final String PERMISSION_ERROR = "DB_AUTH_001";
        
        /** 超时错误 */
        public static final String TIMEOUT_ERROR = "DB_TIMEOUT_001";
        
        /** 数据库不存在错误 */
        public static final String DATABASE_NOT_FOUND = "DB_DB_001";
        
        /** 表不存在错误 */
        public static final String TABLE_NOT_FOUND = "DB_TABLE_001";
        
        /** 字段不存在错误 */
        public static final String COLUMN_NOT_FOUND = "DB_COLUMN_001";
        
        /** 数据类型错误 */
        public static final String DATA_TYPE_ERROR = "DB_TYPE_001";
        
        /** 唯一键冲突错误 */
        public static final String UNIQUE_CONSTRAINT_ERROR = "DB_CONSTRAINT_001";
        
        /** 外键约束错误 */
        public static final String FOREIGN_KEY_ERROR = "DB_FOREIGN_001";
        
        /** 内存不足错误 */
        public static final String MEMORY_ERROR = "DB_MEM_001";
        
        /** 网络错误 */
        public static final String NETWORK_ERROR = "DB_NET_001";
        
        /** 配置错误 */
        public static final String CONFIG_ERROR = "DB_CONFIG_001";
        
        /** 未知错误 */
        public static final String UNKNOWN_ERROR = "DB_UNKNOWN_001";
    }
    
    /**
     * 预设的操作类型常量
     */
    public static class OperationTypes {
        
        /** 连接测试 */
        public static final String CONNECTION_TEST = "CONNECTION_TEST";
        
        /** 查询执行 */
        public static final String QUERY_EXECUTION = "QUERY_EXECUTION";
        
        /** 更新操作 */
        public static final String UPDATE_OPERATION = "UPDATE_OPERATION";
        
        /** 删除操作 */
        public static final String DELETE_OPERATION = "DELETE_OPERATION";
        
        /** 插入操作 */
        public static final String INSERT_OPERATION = "INSERT_OPERATION";
        
        /** 获取数据库列表 */
        public static final String GET_DATABASES = "GET_DATABASES";
        
        /** 获取表列表 */
        public static final String GET_TABLES = "GET_TABLES";
        
        /** 获取表信息 */
        public static final String GET_TABLE_INFO = "GET_TABLE_INFO";
        
        /** 获取表数据 */
        public static final String GET_TABLE_DATA = "GET_TABLE_DATA";
        
        /** 获取列信息 */
        public static final String GET_COLUMN_INFO = "GET_COLUMN_INFO";
        
        /** 创建连接池 */
        public static final String CREATE_CONNECTION_POOL = "CREATE_CONNECTION_POOL";
        
        /** 关闭连接 */
        public static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";
        
        /** 未知操作 */
        public static final String UNKNOWN = "UNKNOWN";
    }
    
    /**
     * 创建连接错误异常的便捷方法
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * @return 数据库操作异常实例
     */
    public static DatabaseOperationException connectionError(String message, Throwable cause) {
        return new DatabaseOperationException(ErrorCodes.CONNECTION_ERROR, message, 
                                             OperationTypes.CONNECTION_TEST, cause);
    }
    
    /**
     * 创建SQL语法错误异常的便捷方法
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * @return 数据库操作异常实例
     */
    public static DatabaseOperationException sqlSyntaxError(String message, Throwable cause) {
        return new DatabaseOperationException(ErrorCodes.SQL_SYNTAX_ERROR, message, 
                                             OperationTypes.QUERY_EXECUTION, cause);
    }
    
    /**
     * 创建权限错误异常的便捷方法
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * @return 数据库操作异常实例
     */
    public static DatabaseOperationException permissionError(String message, Throwable cause) {
        return new DatabaseOperationException(ErrorCodes.PERMISSION_ERROR, message, 
                                             OperationTypes.UNKNOWN, cause);
    }
    
    /**
     * 创建超时错误异常的便捷方法
     * 
     * @param message 错误消息
     * @param cause 原始异常
     * @return 数据库操作异常实例
     */
    public static DatabaseOperationException timeoutError(String message, Throwable cause) {
        return new DatabaseOperationException(ErrorCodes.TIMEOUT_ERROR, message, 
                                             OperationTypes.UNKNOWN, cause);
    }
}