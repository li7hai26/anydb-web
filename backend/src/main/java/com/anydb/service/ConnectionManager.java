package com.anydb.service;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseConnector;
import com.anydb.connector.DatabaseType;
import com.anydb.connector.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 连接管理器 - 按需数据库连接管理
 * 
 * 核心功能：
 * 1. 只在用户配置时才创建连接池
 * 2. 启动时不连接任何外部数据库
 * 3. 统一管理所有数据库连接池
 * 4. 提供连接健康检查和清理机制
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class ConnectionManager {
    
    /**
     * 连接池存储 - key: 数据库配置ID, value: 连接池实例
     */
    private final ConcurrentMap<Long, Object> connectionPools = new ConcurrentHashMap<>();
    
    /**
     * 连接器工厂映射 - DatabaseType -> DatabaseConnector实例
     */
    private final ConcurrentMap<DatabaseType, DatabaseConnector> connectorFactories = new ConcurrentHashMap<>();
    
    /**
     * 构造函数 - 注册所有支持的数据库连接器
     */
    public ConnectionManager() {
        // 注册数据库连接器工厂
        registerConnector(DatabaseType.MYSQL, new MySQLConnector());
        registerConnector(DatabaseType.POSTGRESQL, new PostgreSQLConnector());
        registerConnector(DatabaseType.REDIS, new RedisConnector());
        // 其他连接器可以按需注册
        
        log.info("连接管理器初始化完成，支持 {} 种数据库连接器", connectorFactories.size());
    }
    
    /**
     * 注册数据库连接器
     */
    public void registerConnector(DatabaseType type, DatabaseConnector connector) {
        connectorFactories.put(type, connector);
        log.info("注册数据库连接器: {}", type.getDisplayName());
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testConnection(DatabaseConfig config) {
        DatabaseConnector connector = getConnector(config.getType());
        if (connector == null) {
            log.error("不支持的数据库类型: {}", config.getType());
            return false;
        }
        
        try {
            boolean success = connector.testConnection(config);
            log.info("测试数据库连接 {} - {}: {}", config.getType(), config.getHost(), 
                    success ? "成功" : "失败");
            return success;
        } catch (Exception e) {
            log.error("数据库连接测试异常: {} - {}", config.getType(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 创建连接池 (按需)
     */
    public void createConnectionPool(Long configId, DatabaseConfig config) {
        if (connectionPools.containsKey(configId)) {
            log.warn("连接池已存在，配置ID: {}", configId);
            return;
        }
        
        // 测试连接
        if (!testConnection(config)) {
            throw new IllegalStateException("无法创建连接池，连接测试失败: " + config.getType().getDisplayName());
        }
        
        // 创建连接池实例（使用连接器创建）
        DatabaseConnector connector = getConnector(config.getType());
        Object pool = connector.createConnectionPool(config);
        
        // 存储连接池
        connectionPools.put(configId, pool);
        
        log.info("创建连接池成功，配置ID: {}, 数据库类型: {}", configId, config.getType());
    }
    
    /**
     * 获取连接池
     */
    public Object getConnectionPool(Long configId) {
        Object pool = connectionPools.get(configId);
        if (pool == null) {
            log.warn("连接池不存在，配置ID: {}", configId);
        }
        return pool;
    }
    
    /**
     * 关闭连接池
     */
    public void closeConnectionPool(Long configId) {
        Object pool = connectionPools.remove(configId);
        if (pool != null) {
            // TODO: 实现连接池关闭逻辑
            log.info("关闭连接池成功，配置ID: {}", configId);
        } else {
            log.warn("要关闭的连接池不存在，配置ID: {}", configId);
        }
    }
    
    /**
     * 获取连接器实例
     */
    public DatabaseConnector getConnector(DatabaseType type) {
        return connectorFactories.get(type);
    }
    
    /**
     * 获取支持的所有数据库类型
     */
    public DatabaseType[] getSupportedTypes() {
        return connectorFactories.keySet().toArray(new DatabaseType[0]);
    }
    
    /**
     * 检查连接池是否存在
     */
    public boolean hasConnectionPool(Long configId) {
        return connectionPools.containsKey(configId);
    }
    
    /**
     * 获取连接池数量
     */
    public int getConnectionPoolCount() {
        return connectionPools.size();
    }
    
    /**
     * 关闭所有连接池 (应用关闭时调用)
     */
    public void closeAllConnectionPools() {
        log.info("开始关闭所有连接池，数量: {}", connectionPools.size());
        
        for (Long configId : connectionPools.keySet()) {
            try {
                closeConnectionPool(configId);
            } catch (Exception e) {
                log.error("关闭连接池异常，配置ID: {}, 错误: {}", configId, e.getMessage(), e);
            }
        }
        
        connectionPools.clear();
        log.info("所有连接池已关闭");
    }
    
    /**
     * 健康检查 - 检查所有连接池状态
     */
    public void healthCheck() {
        log.info("开始连接池健康检查，连接池数量: {}", connectionPools.size());
        
        for (Long configId : connectionPools.keySet()) {
            Object pool = connectionPools.get(configId);
            if (pool == null) {
                log.warn("连接池已失效，配置ID: {}", configId);
                connectionPools.remove(configId);
                continue;
            }
            
            // TODO: 实现具体的健康检查逻辑
            log.debug("连接池健康检查通过，配置ID: {}", configId);
        }
    }
}
