package com.anydb.connector;

import com.anydb.connector.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库连接器工厂
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class DatabaseConnectorFactory {
    
    private final Map<DatabaseType, DatabaseConnector> connectorMap = new ConcurrentHashMap<>();
    
    @Autowired
    private MySQLConnector mysqlConnector;
    
    @Autowired
    private PostgreSQLConnector postgresqlConnector;
    
    @Autowired
    private RedisConnector redisConnector;
    
    @Autowired
    private MongoDBConnector mongodbConnector;
    
    @Autowired
    private ElasticsearchConnector elasticsearchConnector;
    
    @Autowired
    private OracleConnector oracleConnector;
    
    @Autowired
    private SQLServerConnector sqlserverConnector;
    
    @Autowired
    private MariaDBConnector mariadbConnector;
    
    @Autowired
    private TiDBConnector tidbConnector;
    
    @Autowired
    private ClickHouseConnector clickhouseConnector;
    
    public DatabaseConnectorFactory() {
        initializeConnectors();
    }
    
    /**
     * 初始化连接器映射
     */
    private void initializeConnectors() {
        try {
            // 高优先级连接器
            registerConnector(DatabaseType.MYSQL, mysqlConnector);
            registerConnector(DatabaseType.POSTGRESQL, postgresqlConnector);
            registerConnector(DatabaseType.REDIS, redisConnector);
            registerConnector(DatabaseType.MONGODB, mongodbConnector);
            registerConnector(DatabaseType.ELASTICSEARCH, elasticsearchConnector);
            registerConnector(DatabaseType.ORACLE, oracleConnector);
            registerConnector(DatabaseType.SQLSERVER, sqlserverConnector);
            registerConnector(DatabaseType.MARIADB, mariadbConnector);
            
            // 中优先级连接器
            registerConnector(DatabaseType.TIDB, tidbConnector);
            registerConnector(DatabaseType.CLICKHOUSE, clickhouseConnector);
            
            log.info("数据库连接器工厂初始化完成，支持的数据库类型: {}", connectorMap.keySet());
            
        } catch (Exception e) {
            log.error("连接器工厂初始化失败", e);
        }
    }
    
    /**
     * 注册连接器
     */
    private void registerConnector(DatabaseType type, DatabaseConnector connector) {
        if (connector != null) {
            connectorMap.put(type, connector);
            log.debug("注册 {} 连接器成功", type.getDisplayName());
        } else {
            log.warn("{} 连接器未找到，无法注册", type.getDisplayName());
        }
    }
    
    /**
     * 根据数据库类型获取连接器
     */
    public DatabaseConnector getConnector(DatabaseType type) {
        if (type == null) {
            throw new IllegalArgumentException("数据库类型不能为null");
        }
        
        DatabaseConnector connector = connectorMap.get(type);
        if (connector == null) {
            String supportedTypes = connectorMap.keySet().stream()
                .map(DatabaseType::getDisplayName)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("无");
            
            throw new UnsupportedOperationException(
                String.format("不支持的数据库类型: %s (当前支持的类型: %s)", 
                    type.getDisplayName(), supportedTypes));
        }
        
        return connector;
    }
    
    /**
     * 根据数据库类型代码获取连接器
     */
    public DatabaseConnector getConnector(String typeCode) {
        try {
            DatabaseType type = DatabaseType.fromCode(typeCode);
            return getConnector(type);
        } catch (IllegalArgumentException e) {
            String supportedTypes = connectorMap.keySet().stream()
                .map(DatabaseType::getCode)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("无");
            
            throw new IllegalArgumentException(
                String.format("不支持的数据库类型代码: %s (当前支持的类型代码: %s)", 
                    typeCode, supportedTypes));
        }
    }
    
    /**
     * 获取所有支持的数据库类型
     */
    public DatabaseType[] getSupportedTypes() {
        return connectorMap.keySet().toArray(new DatabaseType[0]);
    }
    
    /**
     * 检查是否支持指定数据库类型
     */
    public boolean isSupported(DatabaseType type) {
        return connectorMap.containsKey(type);
    }
    
    /**
     * 检查是否支持指定数据库类型代码
     */
    public boolean isSupported(String typeCode) {
        try {
            DatabaseType type = DatabaseType.fromCode(typeCode);
            return isSupported(type);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取连接器统计信息
     */
    public Map<String, Object> getConnectorStats() {
        return Map.of(
            "totalConnectors", connectorMap.size(),
            "supportedTypes", connectorMap.keySet().stream()
                .map(DatabaseType::getDisplayName)
                .sorted()
                .toList(),
            "unimplementedTypes", DatabaseType.values() != null ? 
                java.util.Arrays.stream(DatabaseType.values())
                    .filter(type -> !connectorMap.containsKey(type))
                    .map(DatabaseType::getDisplayName)
                    .sorted()
                    .toList() : java.util.Collections.emptyList()
        );
    }
    
    /**
     * 测试所有连接器
     */
    public Map<String, Boolean> testAllConnectors() {
        Map<String, Boolean> results = new ConcurrentHashMap<>();
        
        connectorMap.forEach((type, connector) -> {
            try {
                // 使用默认配置进行测试
                DatabaseConfig testConfig = createTestConfig(type);
                boolean success = connector.testConnection(testConfig);
                results.put(type.getDisplayName(), success);
                
                if (success) {
                    log.info("{} 连接器测试成功", type.getDisplayName());
                } else {
                    log.warn("{} 连接器测试失败", type.getDisplayName());
                }
            } catch (Exception e) {
                results.put(type.getDisplayName(), false);
                log.error("{} 连接器测试异常", type.getDisplayName(), e);
            }
        });
        
        return results;
    }
    
    /**
     * 创建测试配置
     */
    private DatabaseConfig createTestConfig(DatabaseType type) {
        DatabaseConfig config = new DatabaseConfig();
        config.setType(type);
        config.setHost("localhost");
        config.setPort(getDefaultPort(type));
        config.setDatabase("test");
        config.setUsername("test");
        config.setPassword("test");
        return config;
    }
    
    /**
     * 获取数据库默认端口
     */
    private Integer getDefaultPort(DatabaseType type) {
        switch (type) {
            case MYSQL: return 3306;
            case POSTGRESQL: return 5432;
            case REDIS: return 6379;
            case MONGODB: return 27017;
            case ELASTICSEARCH: return 9200;
            case ORACLE: return 1521;
            case SQLSERVER: return 1433;
            case MARIADB: return 3306;
            case TIDB: return 4000;
            case CLICKHOUSE: return 8123;
            default: return 5432;
        }
    }
    
    /**
     * 关闭所有连接器资源
     */
    public void closeAll() {
        log.info("关闭所有数据库连接器");
        connectorMap.forEach((type, connector) -> {
            try {
                connector.close();
                log.debug("{} 连接器已关闭", type.getDisplayName());
            } catch (Exception e) {
                log.error("关闭 {} 连接器时发生异常", type.getDisplayName(), e);
            }
        });
        connectorMap.clear();
    }
}
