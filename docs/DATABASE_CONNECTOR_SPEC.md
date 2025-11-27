# 数据库连接器开发规范

## 1. 架构设计原则

### 1.1 接口设计
- 所有连接器必须实现 `DatabaseConnector` 接口
- 每个数据库类型对应一个独立的连接器类
- 使用Spring容器管理连接器实例
- 遵循单一职责原则

### 1.2 核心接口
```java
public interface DatabaseConnector {
    DatabaseType getSupportedType();
    boolean testConnection(DatabaseConfig config);
    QueryResult executeQuery(DatabaseConfig config, String sql);
    UpdateResult executeUpdate(DatabaseConfig config, String sql);
    List<String> getDatabases(DatabaseConfig config);
    List<TableInfo> getTables(DatabaseConfig config, String database);
    TableInfo getTableInfo(DatabaseConfig config, String database, String tableName);
    QueryResult getTableData(DatabaseConfig config, String database, String tableName, 
                            int page, int size, String orderBy, String orderDirection);
    Object createConnectionPool(DatabaseConfig config);
    void close();
}
```

## 2. 编码规范

### 2.1 类命名规范
- 连接器命名：`{DatabaseType}Connector`
- 文件位置：`backend/src/main/java/com/anydb/connector/impl/`
- 包名：`com.anydb.connector.impl`

### 2.2 方法规范
- 所有公开方法必须有完整的JavaDoc注释
- 私有方法名称使用小写字母和下划线
- 参数验证：输入参数不能为null
- 异常处理：统一抛出 `DatabaseOperationException`

### 2.3 日志规范
```java
// 初始化日志
log.info("创建{}连接池，配置: {}:{}", getSupportedType().getDisplayName(), config.getHost(), config.getPort());

// 操作日志  
log.info("执行查询: {}", sql.substring(0, Math.min(100, sql.length())));

// 错误日志
log.error("查询执行失败", e);
throw new DatabaseOperationException("查询执行失败: " + e.getMessage(), e);
```

## 3. 单元测试规范

### 3.1 测试结构
- 测试类命名：`{DatabaseType}ConnectorTest`
- 测试包：`backend/src/test/java/com/anydb/connector/impl/`
- 测试方法命名：`test{methodName}When{description}`

### 3.2 测试覆盖率要求
- 公共方法覆盖率：100%
- 私有方法覆盖率：≥90%
- 异常分支覆盖率：100%
- 边界条件测试：必测

### 3.3 测试夹具（Test Fixtures）
```java
public abstract class DatabaseConnectorTest {
    protected DatabaseConfig config;
    protected DatabaseConnector connector;
    
    @BeforeEach
    void setUp() {
        // 设置测试配置
        config = createTestConfig();
        connector = createConnector();
    }
}
```

## 4. 数据库操作规范

### 4.1 连接管理
- 连接使用try-with-resources确保自动关闭
- 连接池配置参数统一管理
- 连接超时和重试机制

### 4.2 SQL执行
- 参数化查询防止SQL注入
- 执行时间监控和记录
- 结果集处理优化

### 4.3 错误处理
- 统一错误码定义
- 详细错误信息记录
- 优雅的降级处理

## 5. 配置文件规范

### 5.1 连接参数
```java
public DatabaseConfig createTestConfig() {
    DatabaseConfig config = new DatabaseConfig();
    config.setType(DatabaseType.MYSQL);
    config.setHost("localhost");
    config.setPort(3306);
    config.setDatabase("test");
    config.setUsername("test");
    config.setPassword("password");
    return config;
}
```

### 5.2 默认配置
- 连接池大小：10-50
- 连接超时：30秒
- 查询超时：60秒
- 最大重试次数：3

## 6. 性能规范

### 6.1 连接池
- 使用 HikariCP 或 Druid 连接池
- 配置适当的池大小
- 启用连接有效性检查

### 6.2 内存管理
- 结果集分页加载
- 大结果集分块处理
- 及时释放资源

## 7. 安全规范

### 7.1 认证
- 支持用户名密码认证
- 支持SSL/TLS加密
- 支持Kerberos等高级认证（可选）

### 7.2 授权
- 连接级别的权限控制
- SQL语句执行权限验证
- 敏感操作审计日志

## 8. 监控规范

### 8.1 性能指标
- 连接池状态
- 查询执行时间
- 成功率统计
- 错误率监控

### 8.2 健康检查
- ping连接测试
- 连接池状态检查
- 资源使用监控

## 9. 文档要求

### 9.1 API文档
- 每个类和方法必须有JavaDoc
- 包含使用示例
- 参数说明完整
- 异常说明详细

### 9.2 测试文档
- 测试覆盖率报告
- 性能测试结果
- 兼容性测试矩阵

## 10. 部署规范

### 10.1 依赖管理
- 使用Maven依赖管理
- 版本兼容性检查
- 冲突解决机制

### 10.2 配置外部化
- 数据库配置外部化
- 环境变量支持
- 配置文件热更新（可选）

## 11. 持续集成

### 11.1 自动测试
- 单元测试自动化
- 集成测试自动化
- 覆盖率检查自动化

### 11.2 代码质量
- 代码规范检查（CheckStyle）
- 安全漏洞扫描
- 静态代码分析

---

## 检查清单

### 开发前
- [ ] 分析数据库特性
- [ ] 设计接口方案
- [ ] 编写技术设计文档
- [ ] 配置开发环境

### 开发中
- [ ] 编写单元测试
- [ ] 实现核心功能
- [ ] 编写集成测试
- [ ] 性能优化
- [ ] 安全性检查

### 开发后
- [ ] 代码审查
- [ ] 文档完善
- [ ] 测试报告
- [ ] 性能测试
- [ ] 兼容性测试

### 部署前
- [ ] 最终测试
- [ ] 生产环境配置
- [ ] 监控配置
- [ ] 回滚计划