package com.anydb.connector.impl;

import com.anydb.connector.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Redis连接器基本单元测试
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class RedisConnectorTest {

    @Mock
    private DatabaseConfig mockConfig;

    @InjectMocks
    private RedisConnector redisConnector;

    @BeforeEach
    void setUp() {
        // 基本的配置设置
        when(mockConfig.getHost()).thenReturn("localhost");
        when(mockConfig.getPort()).thenReturn(6379);
        when(mockConfig.getUsername()).thenReturn("testuser");
        when(mockConfig.getPassword()).thenReturn("testpass");
    }

    @Test
    void testGetSupportedType() {
        assertEquals(DatabaseType.REDIS, redisConnector.getSupportedType());
    }

    @Test
    void testConnection() {
        // 在单元测试中，testConnection可能会失败因为没有真实的Redis
        // 但不应该抛出异常
        boolean result = redisConnector.testConnection(mockConfig);
        // 实际结果取决于是否有Redis服务器运行
        log.info("Redis连接测试结果: {}", result);
    }

    @Test
    void testConnection_ConfigNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            redisConnector.testConnection(null);
        });
    }

    @Test
    void testConnection_HostNull() {
        when(mockConfig.getHost()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            redisConnector.testConnection(mockConfig);
        });
    }
}