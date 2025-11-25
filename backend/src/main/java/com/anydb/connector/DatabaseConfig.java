package com.anydb.connector;

import java.util.Map;

/**
 * 数据库连接配置
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
public class DatabaseConfig {
    
    private Long id;
    
    /**
     * 连接名称
     */
    private String name;
    
    /**
     * 数据库类型
     */
    private DatabaseType type;
    
    /**
     * 主机地址
     */
    private String host;
    
    /**
     * 端口号
     */
    private Integer port;
    
    /**
     * 数据库名
     */
    private String database;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 参数配置
     */
    private Map<String, Object> parameters;
    
    /**
     * 连接选项
     */
    private Map<String, String> options;
    
    // 构造函数
    public DatabaseConfig() {
    }
    
    public DatabaseConfig(Long id, DatabaseType type, String host, Integer port) {
        this.id = id;
        this.type = type;
        this.host = host;
        this.port = port;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public DatabaseType getType() {
        return type;
    }
    
    public void setType(DatabaseType type) {
        this.type = type;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public Map<String, String> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
    
    // 添加parameters方法
    public Object getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }
    
    public void setParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }
    
    public String getOption(String key) {
        return options != null ? options.get(key) : null;
    }
    
    public void setOption(String key, String value) {
        if (options == null) {
            options = new java.util.HashMap<>();
        }
        options.put(key, value);
    }
    
    /**
     * 连接超时时间（毫秒）
     */
    private Integer timeout;
    
    /**
     * 连接池大小
     */
    private Integer poolSize;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 是否启用
     */
    private Boolean enabled = true;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 构建JDBC URL
     */
    public String buildUrl() {
        StringBuilder url = new StringBuilder();
        url.append(type.getUrlPrefix());
        
        // 根据不同数据库类型构建URL
        switch (type) {
            case MYSQL:
            case MARIADB:
            case TIDB:
            case OCEANBASE:
                url.append(host).append(":").append(port);
                if (database != null && !database.isEmpty()) {
                    url.append("/").append(database);
                }
                break;
                
            case POSTGRESQL:
                url.append(host).append(":").append(port);
                if (database != null && !database.isEmpty()) {
                    url.append("/").append(database);
                }
                break;
                
            case ORACLE:
                if (database != null && !database.isEmpty()) {
                    url.append(host).append(":").append(port).append(":").append(database);
                } else {
                    url.append(host).append(":").append(port);
                }
                break;
                
            case SQLSERVER:
                url.append(host).append(":").append(port);
                if (database != null && !database.isEmpty()) {
                    url.append(";databaseName=").append(database);
                }
                break;
                
            case REDIS:
                url.append(host).append(":").append(port);
                break;
                
            case ELASTICSEARCH:
                url.append(host).append(":").append(port);
                break;
                
            default:
                url.append(host).append(":").append(port);
                if (database != null && !database.isEmpty()) {
                    url.append("/").append(database);
                }
        }
        
        // 添加额外参数
        if (parameters != null && !parameters.isEmpty()) {
            url.append("?").append(parameters);
        }
        
        return url.toString();
    }
    
    /**
     * 获取连接描述信息
     */
    public String getDescription() {
        return String.format("%s://%s:%d%s", 
            type.getDisplayName(), 
            host, 
            port, 
            database != null ? "/" + database : ""
        );
    }
}