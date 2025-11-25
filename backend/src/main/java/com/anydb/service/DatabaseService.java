package com.anydb.service;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据库服务 - 使用ConnectionManager实现按需连接
 * 
 * 核心改进：
 * 1. 不在启动时连接任何外部数据库
 * 2. 使用ConnectionManager统一管理连接
 * 3. 按需创建连接池
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class DatabaseService {
    
    @Autowired
    private ConnectionManager connectionManager;
    
    /**
     * 获取支持的数据库类型
     */
    public List<DatabaseType> getSupportedTypes() {
        return Arrays.asList(connectionManager.getSupportedTypes());
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testConnection(DatabaseConfig config) {
        try {
            boolean result = connectionManager.testConnection(config);
            log.info("数据库连接测试结果: {} - {}: {}", config.getType(), config.getHost(), result ? "成功" : "失败");
            return result;
        } catch (Exception e) {
            log.error("数据库连接测试异常: {} - {}", config.getType(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 创建数据库连接池（按需）
     */
    public void createConnectionPool(Long configId, DatabaseConfig config) {
        try {
            connectionManager.createConnectionPool(configId, config);
            log.info("创建数据库连接池成功，配置ID: {}, 类型: {}", configId, config.getType());
        } catch (Exception e) {
            log.error("创建数据库连接池失败，配置ID: {}, 错误: {}", configId, e.getMessage(), e);
            throw new RuntimeException("创建连接池失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 关闭数据库连接池
     */
    public void closeConnectionPool(Long configId) {
        try {
            connectionManager.closeConnectionPool(configId);
            log.info("关闭数据库连接池成功，配置ID: {}", configId);
        } catch (Exception e) {
            log.error("关闭数据库连接池失败，配置ID: {}, 错误: {}", configId, e.getMessage(), e);
        }
    }
    
    /**
     * 检查连接池是否存在
     */
    public boolean hasConnectionPool(Long configId) {
        return connectionManager.hasConnectionPool(configId);
    }
    
    /**
     * 获取连接池数量
     */
    public int getConnectionPoolCount() {
        return connectionManager.getConnectionPoolCount();
    }
    
    /**
     * 执行SQL查询
     */
    public DatabaseConnector.QueryResult executeQuery(Long configId, String sql) {
        DatabaseConfig config = getDatabaseConfig(configId);
        DatabaseConnector connector = connectionManager.getConnector(config.getType());
        
        if (connector == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + config.getType());
        }
        
        log.info("执行SQL查询: {}", sql);
        return connector.executeQuery(config, sql);
    }
    
    /**
     * 执行SQL更新
     */
    public DatabaseConnector.UpdateResult executeUpdate(Long configId, String sql) {
        DatabaseConfig config = getDatabaseConfig(configId);
        DatabaseConnector connector = connectionManager.getConnector(config.getType());
        
        if (connector == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + config.getType());
        }
        
        log.info("执行SQL更新: {}", sql);
        return connector.executeUpdate(config, sql);
    }
    
    /**
     * 获取数据库列表
     */
    public List<String> getDatabases(Long configId) {
        DatabaseConfig config = getDatabaseConfig(configId);
        DatabaseConnector connector = connectionManager.getConnector(config.getType());
        
        if (connector == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + config.getType());
        }
        
        return connector.getDatabases(config);
    }
    
    /**
     * 获取表列表
     */
    public List<DatabaseConnector.TableInfo> getTables(Long configId, String database) {
        DatabaseConfig config = getDatabaseConfig(configId);
        DatabaseConnector connector = connectionManager.getConnector(config.getType());
        
        if (connector == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + config.getType());
        }
        
        return connector.getTables(config, database);
    }
    
    /**
     * 获取表结构信息
     */
    public DatabaseConnector.TableInfo getTableInfo(Long configId, String database, String tableName) {
        DatabaseConfig config = getDatabaseConfig(configId);
        DatabaseConnector connector = connectionManager.getConnector(config.getType());
        
        if (connector == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + config.getType());
        }
        
        return connector.getTableInfo(config, database, tableName);
    }
    
    /**
     * 获取表数据
     */
    public DatabaseConnector.QueryResult getTableData(Long configId, String database, String tableName, 
                                                     int page, int size, String orderBy, String orderDirection) {
        DatabaseConfig config = getDatabaseConfig(configId);
        DatabaseConnector connector = connectionManager.getConnector(config.getType());
        
        if (connector == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + config.getType());
        }
        
        return connector.getTableData(config, database, tableName, page, size, orderBy, orderDirection);
    }
    
    /**
     * 获取数据库配置（TODO: 实际应该从MySQL持久化存储中获取）
     */
    private DatabaseConfig getDatabaseConfig(Long configId) {
        // TODO: 从MySQL数据库中获取实际的数据库配置
        // 这里返回临时配置，实际项目中需要实现持久化存储
        
        // 临时模拟数据库配置
        DatabaseConfig config = new DatabaseConfig();
        config.setId(configId);
        config.setType(DatabaseType.MYSQL);
        config.setHost("localhost");
        config.setPort(3306);
        config.setDatabase("test");
        config.setUsername("root");
        config.setPassword("password");
        
        log.warn("使用临时数据库配置，实际项目中应该从MySQL持久化存储中获取，配置ID: {}", configId);
        return config;
    }
}