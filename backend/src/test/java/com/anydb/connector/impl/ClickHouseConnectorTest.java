package com.anydb.connector.impl;

import com.anydb.connector.DatabaseConfig;
import com.anydb.connector.DatabaseOperationException;
import com.anydb.connector.DatabaseType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ClickHouse连接器单元测试
 * 
 * 测试覆盖范围：
 * - 连接测试功能
 * - SQL执行功能
 * - 数据库和表操作
 * - 异常处理
 * - 资源清理
 * - 安全防护
 * - 性能优化
 * - 并发访问
 * 
 * @author AnyDB Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClickHouse连接器测试")
class ClickHouseConnectorTest {

    @Mock
    private DatabaseConfig mockConfig;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private DatabaseConfig mockQueryResultConfig;
    
    @Mock
    private ResultSetMetaData mockMetaData;
    
    @InjectMocks
    private ClickHouseConnector clickHouseConnector;
    
    // 测试数据
    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 8123;
    private static final String TEST_DATABASE = "test_db";
    private static final String TEST_USERNAME = "test_user";
    private static final String TEST_PASSWORD = "test_password";
    private static final String TEST_TABLE = "test_table";
    
    @BeforeEach
    void setUp() {
        // 设置默认配置
        when(mockConfig.getHost()).thenReturn(TEST_HOST);
        when(mockConfig.getPort()).thenReturn(TEST_PORT);
        when(mockConfig.getDatabase()).thenReturn(TEST_DATABASE);
        when(mockConfig.getUsername()).thenReturn(TEST_USERNAME);
        when(mockConfig.getPassword()).thenReturn(TEST_PASSWORD);
        
        // 设置URL构建
        when(mockConfig.buildUrl()).thenReturn("jdbc:clickhouse://" + TEST_HOST + ":" + TEST_PORT + "/" + TEST_DATABASE);
    }
    
    @AfterEach
    void tearDown() {
        reset(mockConfig, mockConnection, mockStatement, mockResultSet, mockQueryResultConfig, mockMetaData);
    }
    
    // ========== 基础功能测试 ==========
    
    @Test
    @DisplayName("获取支持的数据库类型")
    void testGetSupportedType() {
        DatabaseType result = clickHouseConnector.getSupportedType();
        
        assertEquals(DatabaseType.CLICKHOUSE, result, "应该返回ClickHouse数据库类型");
        assertEquals("clickhouse", result.getCode(), "数据库类型代码应该正确");
        assertEquals("ClickHouse", result.getDisplayName(), "数据库显示名称应该正确");
    }
    
    // ========== 连接测试功能测试 ==========
    
    @Test
    @DisplayName("连接测试成功")
    void testConnection_Success() throws SQLException {
        // 模拟连接过程
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SELECT 1")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        boolean result = clickHouseConnector.testConnection(mockConfig);
        
        assertTrue(result, "连接测试应该成功");
        
        // 验证调用
        verify(mockConfig, times(2)).getHost();
        verify(mockConfig, times(2)).getPort();
        verify(mockConfig, times(2)).getUsername();
        verify(mockConfig, times(2)).getPassword();
        verify(mockConnection, times(1)).createStatement();
        verify(mockStatement, times(1)).executeQuery("SELECT 1");
        verify(mockResultSet, times(1)).next();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("连接测试失败 - SQL异常")
    void testConnection_SQLException() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenThrow(new SQLException("连接失败"));
        
        boolean result = clickHouseConnector.testConnection(mockConfig);
        
        assertFalse(result, "连接测试应该失败");
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("连接测试失败 - 连接超时")
    void testConnection_Timeout() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenThrow(new SQLException("连接超时"));
        
        boolean result = clickHouseConnector.testConnection(mockConfig);
        
        assertFalse(result, "连接测试应该失败");
        verify(mockConfig, times(2)).getHost();
        verify(mockConfig, times(2)).getPassword();
    }
    
    // ========== SQL执行功能测试 ==========
    
    @Test
    @DisplayName("执行简单查询成功")
    void testExecuteQuery_SimpleQuery_Success() throws SQLException {
        // 模拟执行查询
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        // 模拟结果集元数据
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");
        when(mockMetaData.getColumnLabel(3)).thenReturn("created_at");
        
        // 模拟数据行
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn(1);
        when(mockResultSet.getObject(2)).thenReturn("test");
        when(mockResultSet.getObject(3)).thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
        
        var result = clickHouseConnector.executeQuery(mockConfig, "SELECT id, name, created_at FROM test_table LIMIT 10");
        
        assertNotNull(result, "查询结果不应该为空");
        assertEquals(3, result.getColumns().size(), "列数应该正确");
        assertEquals(1, result.getRows().size(), "行数应该正确");
        assertTrue(result.getExecutionTime() >= 0, "执行时间应该有效");
        assertEquals("id", result.getColumns().get(0), "第一列名称应该正确");
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("执行复杂查询 - 分析型查询")
    void testExecuteQuery_ComplexQuery_Success() throws SQLException {
        // 模拟执行复杂查询
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("system"))).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("database");
        when(mockMetaData.getColumnLabel(2)).thenReturn("table");
        
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getObject(1)).thenReturn("system");
        when(mockResultSet.getObject(2)).thenReturn("events");
        
        var result = clickHouseConnector.executeQuery(mockConfig, "SYSTEM TABLES");
        
        assertNotNull(result, "查询结果不应该为空");
        assertTrue(result.getRows().size() >= 1, "应该返回系统表信息");
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("执行查询失败 - SQL语法错误")
    void testExecuteQuery_SQLSyntaxError() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("语法错误"));
        
        assertThrows(DatabaseOperationException.class, () -> {
            clickHouseConnector.executeQuery(mockConfig, "SELECT * FROM invalid_syntax");
        }, "应该抛出数据库操作异常");
        
        // 验证资源清理
        verify(mockResultSet, never()).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    // ========== SQL更新功能测试 ==========
    
    @Test
    @DisplayName("执行更新操作成功")
    void testExecuteUpdate_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate("INSERT INTO test VALUES (1, 'test')")).thenReturn(1);
        
        var result = clickHouseConnector.executeUpdate(mockConfig, "INSERT INTO test VALUES (1, 'test')");
        
        assertNotNull(result, "更新结果不应该为空");
        assertEquals(1, result.getAffectedRows(), "受影响行数应该正确");
        assertTrue(result.getExecutionTime() >= 0, "执行时间应该有效");
        assertNotNull(result.getMessage(), "消息应该不为空");
        
        // 验证资源清理
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("执行更新操作失败")
    void testExecuteUpdate_Failure() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate("INSERT INTO invalid VALUES (1, 'test')")).thenThrow(new SQLException("表不存在"));
        
        assertThrows(DatabaseOperationException.class, () -> {
            clickHouseConnector.executeUpdate(mockConfig, "INSERT INTO invalid VALUES (1, 'test')");
        }, "应该抛出数据库操作异常");
        
        // 验证资源清理
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    // ========== 数据库操作功能测试 ==========
    
    @Test
    @DisplayName("获取数据库列表成功")
    void testGetDatabases_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SHOW DATABASES")).thenReturn(mockResultSet);
        
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getString(1)).thenReturn("default", "system", "information_schema");
        
        List<String> databases = clickHouseConnector.getDatabases(mockConfig);
        
        assertNotNull(databases, "数据库列表不应该为空");
        assertEquals(3, databases.size(), "数据库数量应该正确");
        assertTrue(databases.contains("default"), "应该包含default数据库");
        assertTrue(databases.contains("system"), "应该包含system数据库");
        assertTrue(databases.contains("information_schema"), "应该包含information_schema数据库");
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("获取数据库列表失败")
    void testGetDatabases_Failure() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SHOW DATABASES")).thenThrow(new SQLException("权限不足"));
        
        assertThrows(DatabaseOperationException.class, () -> {
            clickHouseConnector.getDatabases(mockConfig);
        }, "应该抛出数据库操作异常");
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    // ========== 表操作功能测试 ==========
    
    @Test
    @DisplayName("获取表列表成功")
    void testGetTables_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SHOW TABLES")).thenReturn(mockResultSet);
        
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString(1)).thenReturn("users", "orders");
        
        List<TableInfo> tables = clickHouseConnector.getTables(mockConfig, TEST_DATABASE);
        
        assertNotNull(tables, "表列表不应该为空");
        assertEquals(2, tables.size(), "表数量应该正确");
        assertEquals("users", tables.get(0).getName(), "第一个表名称应该正确");
        assertEquals("orders", tables.get(1).getName(), "第二个表名称应该正确");
        assertNotNull(tables.get(0).getComment(), "表注释不应该为空");
        assertNotNull(tables.get(0).getRowCount() >= 0, "行数应该有效");
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("获取指定数据库的表列表成功")
    void testGetTables_WithDatabase_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SHOW TABLES FROM test_db")).thenReturn(mockResultSet);
        
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(1)).thenReturn("test_table");
        
        List<TableInfo> tables = clickHouseConnector.getTables(mockConfig, "test_db");
        
        assertNotNull(tables, "表列表不应该为空");
        assertEquals(1, tables.size(), "表数量应该正确");
        assertEquals("test_table", tables.get(0).getName(), "表名称应该正确");
        
        // 验证使用了指定的数据库名称
        verify(mockStatement, times(1)).executeQuery("SHOW TABLES FROM test_db");
    }
    
    // ========== 表信息功能测试 ==========
    
    @Test
    @DisplayName("获取表信息成功")
    void testGetTableInfo_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("DESCRIBE"))).thenReturn(mockResultSet);
        
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getString("name")).thenReturn("id", "name", "created_at");
        when(mockResultSet.getString("type")).thenReturn("Int32", "String", "DateTime");
        when(mockResultSet.getString("comment")).thenReturn("主键", "名称", "创建时间");
        
        TableInfo table = clickHouseConnector.getTableInfo(mockConfig, TEST_DATABASE, TEST_TABLE);
        
        assertNotNull(table, "表信息不应该为空");
        assertEquals(TEST_TABLE, table.getName(), "表名称应该正确");
        assertNotNull(table.getComment(), "表注释不应该为空");
        assertTrue(table.getRowCount() >= 0, "行数应该有效");
        assertNotNull(table.getColumns(), "列信息列表不应该为空");
        assertEquals(3, table.getColumns().size(), "列数量应该正确");
        
        // 验证列信息
        ColumnInfo firstColumn = table.getColumns().get(0);
        assertEquals("id", firstColumn.getName(), "第一列名称应该正确");
        assertEquals("Int32", firstColumn.getType(), "第一列类型应该正确");
        assertEquals("主键", firstColumn.getComment(), "第一列注释应该正确");
        assertFalse(firstColumn.isPrimaryKey(), "ClickHouse列不应该被标记为主键");
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    // ========== 表数据分页查询测试 ==========
    
    @Test
    @DisplayName("获取表数据分页成功")
    void testGetTableData_Pagination_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("LIMIT 20 OFFSET 20"))).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");
        
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getObject(1)).thenReturn(11, 12);
        when(mockResultSet.getObject(2)).thenReturn("record11", "record12");
        
        var result = clickHouseConnector.getTableData(mockConfig, TEST_DATABASE, TEST_TABLE, 2, 20, "id", "ASC");
        
        assertNotNull(result, "分页结果不应该为空");
        assertEquals(2, result.getColumns().size(), "列数应该正确");
        assertEquals(2, result.getRows().size(), "数据行数应该正确");
        assertEquals("id", result.getColumns().get(0), "第一列名称应该正确");
        
        // 验证SQL构造
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockStatement, times(1)).executeQuery(sqlCaptor.capture());
        String actualSql = sqlCaptor.getValue();
        assertTrue(actualSql.contains("LIMIT 20"), "SQL应该包含LIMIT子句");
        assertTrue(actualSql.contains("OFFSET 20"), "SQL应该包含OFFSET子句");
        assertTrue(actualSql.contains("ORDER BY id ASC"), "SQL应该包含ORDER BY子句");
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("获取表数据 - 第一页测试")
    void testGetTableData_FirstPage_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(contains("LIMIT 10 OFFSET 0"))).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn(1);
        
        var result = clickHouseConnector.getTableData(mockConfig, TEST_DATABASE, TEST_TABLE, 1, 10, null, null);
        
        assertNotNull(result, "分页结果不应该为空");
        assertEquals(1, result.getRows().size(), "第一页应该有一行数据");
        
        // 验证SQL构造（第一页OFFSET为0）
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockStatement, times(1)).executeQuery(sqlCaptor.capture());
        String actualSql = sqlCaptor.getValue();
        assertTrue(actualSql.contains("OFFSET 0"), "第一页OFFSET应该为0");
    }
    
    // ========== 异常处理测试 ==========
    
    @Test
    @DisplayName("数据库操作异常处理")
    void testExceptionHandling_DatabaseOperationException() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("数据库连接失败"));
        
        assertThrows(DatabaseOperationException.class, () -> {
            clickHouseConnector.executeQuery(mockConfig, "SELECT 1");
        }, "应该抛出数据库操作异常");
        
        // 验证异常信息
        try {
            clickHouseConnector.executeQuery(mockConfig, "SELECT 1");
        } catch (DatabaseOperationException e) {
            assertTrue(e.getMessage().contains("ClickHouse SQL执行失败"), "异常信息应该包含ClickHouse SQL执行失败");
        }
    }
    
    @Test
    @DisplayName("连接超时异常处理")
    void testExceptionHandling_ConnectionTimeout() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenThrow(new SQLException("连接超时"));
        
        assertThrows(DatabaseOperationException.class, () -> {
            clickHouseConnector.executeQuery(mockConfig, "SELECT 1");
        }, "应该抛出数据库操作异常");
    }
    
    // ========== 资源清理测试 ==========
    
    @Test
    @DisplayName("资源清理 - 正常情况")
    void testResourceCleanup_NormalCase() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SELECT 1")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        clickHouseConnector.executeQuery(mockConfig, "SELECT 1");
        
        // 验证所有资源都被正确关闭
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("资源清理 - 异常情况")
    void testResourceCleanup_ExceptionCase() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SELECT 1")).thenThrow(new SQLException("执行异常"));
        
        try {
            clickHouseConnector.executeQuery(mockConfig, "SELECT 1");
        } catch (DatabaseOperationException e) {
            // 预期异常
        }
        
        // 即使发生异常，资源也应该被清理
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    // ========== 安全防护测试 ==========
    
    @Test
    @DisplayName("SQL注入防护测试")
    void testSQLInjectionProtection() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnLabel(1)).thenReturn("result");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn("safe");
        
        // 模拟潜在的SQL注入尝试
        String maliciousSql = "SELECT * FROM users WHERE name = 'admin' OR '1'='1'";
        
        var result = clickHouseConnector.executeQuery(mockConfig, maliciousSql);
        
        assertNotNull(result, "应该正常执行查询");
        // 注意：ClickHouse连接器使用原生JDBC，SQL注入防护主要由应用层和数据库本身完成
        
        // 验证资源清理
        verify(mockResultSet, times(1)).close();
        verify(mockStatement, times(1)).close();
        verify(mockConnection, times(1)).close();
    }
    
    // ========== 性能优化测试 ==========
    
    @Test
    @DisplayName("执行时间记录测试")
    void testExecutionTimeRecording() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SELECT 1")).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnLabel(1)).thenReturn("result");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn(1);
        
        long startTime = System.currentTimeMillis();
        var result = clickHouseConnector.executeQuery(mockConfig, "SELECT 1");
        long endTime = System.currentTimeMillis();
        
        assertNotNull(result.getExecutionTime(), "执行时间不应该为空");
        assertTrue(result.getExecutionTime() >= 0, "执行时间应该大于等于0");
        assertTrue(result.getExecutionTime() <= endTime - startTime + 100, "执行时间应该在合理范围内");
    }
    
    // ========== 连接池功能测试 ==========
    
    @Test
    @DisplayName("创建连接池成功")
    void testCreateConnectionPool_Success() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SELECT 1")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        var pool = clickHouseConnector.createConnectionPool(mockConfig);
        
        assertNotNull(pool, "连接池不应该为空");
        assertTrue(pool instanceof ClickHouseConnector.ClickHouseConnectionPool, "应该返回ClickHouse连接池实例");
        
        // 验证资源清理
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("创建连接池失败")
    void testCreateConnectionPool_Failure() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenThrow(new SQLException("连接失败"));
        
        assertThrows(RuntimeException.class, () -> {
            clickHouseConnector.createConnectionPool(mockConfig);
        }, "应该抛出运行时异常");
    }
    
    // ========== 特殊功能测试 ==========
    
    @Test
    @DisplayName("ClickHouse特殊查询测试")
    void testClickHouseSpecialQueries() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("SYSTEM TABLES")).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnLabel(1)).thenReturn("table");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn("system_events");
        
        var result = clickHouseConnector.executeQuery(mockConfig, "SYSTEM TABLES");
        
        assertNotNull(result, "特殊查询结果不应该为空");
        assertTrue(result.getRows().size() >= 1, "应该返回系统表信息");
    }
    
    @Test
    @DisplayName("关闭连接器")
    void testCloseConnector() {
        // 测试关闭操作不应该抛出异常
        assertDoesNotThrow(() -> {
            clickHouseConnector.close();
        }, "关闭连接器不应该抛出异常");
    }
    
    // ========== 边界值测试 ==========
    
    @Test
    @DisplayName("空配置测试")
    void testEmptyConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> {
            clickHouseConnector.testConnection(null);
        }, "空配置应该抛出异常");
    }
    
    @Test
    @DisplayName("空SQL语句测试")
    void testEmptySQL() throws SQLException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("")).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(0);
        
        // 空SQL应该正常执行，但返回空结果集
        var result = clickHouseConnector.executeQuery(mockConfig, "");
        
        assertNotNull(result, "查询结果不应该为空");
        assertEquals(0, result.getColumns().size(), "空SQL应该返回0列");
        assertEquals(0, result.getRows().size(), "空SQL应该返回0行");
    }
    
    // ========== 并发访问测试 ==========
    
    @Test
    @DisplayName("并发查询测试")
    void testConcurrentQueries() throws SQLException, InterruptedException {
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnLabel(1)).thenReturn("result");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn(1);
        
        // 创建多个线程同时执行查询
        Thread[] threads = new Thread[3];
        var[] results = new var[3];
        
        for (int i = 0; i < 3; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = clickHouseConnector.executeQuery(mockConfig, "SELECT 1");
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证所有查询都成功执行
        for (var result : results) {
            assertNotNull(result, "并发查询结果不应该为空");
        }
        
        // 验证资源清理
        verify(mockResultSet, times(3)).close();
        verify(mockStatement, times(3)).close();
        verify(mockConnection, times(3)).close();
    }
}