package com.anydb.connector;

import lombok.Data;

/**
 * 数据库连接配置
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Data
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
     * 其他参数
     */
    private String parameters;
    
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