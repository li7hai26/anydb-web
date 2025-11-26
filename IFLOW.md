# AnyDB Web 项目开发指南

## 项目概述

**AnyDB Web** 是一个功能强大的开源数据库管理平台，采用现代化的前后端分离架构，支持多种数据库类型的连接、管理和监控。平台采用按需连接设计，启动快速，配置持久化，支持连接池管理。

### 核心特性
- 🔗 **多数据库支持**: MySQL, PostgreSQL, Oracle, MariaDB, SQL Server, DB2, TiDB, OceanBase, Redis, Elasticsearch, MongoDB, Kafka, ClickHouse 等 18+ 种数据库
- 📝 **SQL编辑器**: 基于 Monaco Editor，支持语法高亮、自动补全、SQL格式化和执行历史
- 📊 **实时监控**: 数据库连接状态、性能指标、慢查询分析、资源监控
- 🔐 **安全认证**: JWT身份认证、权限控制、操作审计、配置加密
- 🎨 **现代化UI**: 基于 Ant Design 5.x，支持深色主题、响应式设计
- ⚡ **高性能**: Druid连接池、Redis缓存、异步处理、按需连接
- 🔄 **连接管理**: 连接池管理、健康检查、异常恢复、自动清理

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2.1
- **语言**: Java 17+
- **数据访问**: Spring Data JPA + MyBatis Plus 3.5.5
- **连接池**: Alibaba Druid 1.2.20
- **缓存**: Redis + Spring Cache
- **认证**: Spring Security + JWT (jjwt 0.12.3)
- **消息推送**: WebSocket
- **构建**: Maven 3.8+
- **数据库驱动**: MySQL 8.0.33, PostgreSQL 42.7.1, Oracle, DB2等

### 前端技术栈
- **框架**: React 18.2.0 + TypeScript 5.3.3
- **构建工具**: Vite 5.0.11
- **UI组件**: Ant Design 5.12.8 + Ant Design Charts 2.0.3
- **状态管理**: React Query 5.17.19 + React Hooks
- **路由**: React Router 6.21.1
- **代码编辑**: Monaco Editor 4.6.0
- **图表**: Ant Design Charts
- **工具库**: lodash, dayjs, sql-formatter, date-fns

### 部署架构
- **容器化**: Docker + Docker Compose 3.8
- **反向代理**: Nginx
- **数据库集群**: MySQL, PostgreSQL, Redis, Elasticsearch, MongoDB, Kafka, ClickHouse
- **健康检查**: 自动容器健康监控
- **数据备份**: 自动化备份服务
- **网络**: Bridge Network 隔离

## 项目结构

```
anydb-web/
├── backend/                     # Spring Boot 后端项目
│   ├── src/main/java/com/anydb/
│   │   ├── AnyDBBackendApplication.java    # 启动类
│   │   ├── config/                   # 配置类
│   │   │   └── SecurityConfig.java   # 安全配置
│   │   ├── connector/                # 数据库连接器模块
│   │   │   ├── DatabaseConfig.java   # 数据库配置
│   │   │   ├── DatabaseConnector.java # 连接器接口
│   │   │   ├── DatabaseType.java     # 数据库类型枚举
│   │   │   └── impl/                 # 具体实现
│   │   │       ├── MySQLConnector.java        # MySQL连接器
│   │   │       ├── PostgreSQLConnector.java   # PostgreSQL连接器
│   │   │       └── RedisConnector.java        # Redis连接器
│   │   ├── controller/               # REST API控制器
│   │   │   ├── DatabaseController.java       # 数据库管理API
│   │   │   └── HealthController.java         # 健康检查API
│   │   └── service/                  # 业务逻辑服务
│   │       ├── ConnectionManager.java # 连接管理器
│   │       └── DatabaseService.java  # 数据库服务
│   ├── src/main/resources/
│   │   └── application.yml           # 主配置文件
│   ├── pom.xml                       # Maven依赖配置
│   └── Dockerfile                    # 后端Docker镜像
├── frontend/                        # React 前端项目
│   ├── src/
│   │   ├── App.tsx                   # 应用根组件
│   │   ├── main.tsx                  # 应用入口
│   │   ├── components/layout/        # 布局组件
│   │   │   └── MainLayout.tsx        # 主布局
│   │   ├── pages/                    # 页面组件
│   │   │   ├── Home.tsx              # 首页Dashboard
│   │   │   ├── DatabaseManager.tsx   # 数据库管理
│   │   │   ├── SQLEditor.tsx         # SQL编辑器
│   │   │   ├── Monitor.tsx           # 监控页面
│   │   │   └── Settings.tsx          # 设置页面
│   │   ├── services/                 # API服务
│   │   │   ├── api.ts                # HTTP客户端
│   │   │   └── database.ts           # 数据库相关API
│   │   ├── hooks/                    # React Hooks
│   │   ├── types/                    # TypeScript类型定义
│   │   ├── styles/                   # 样式文件
│   │   └── utils/                    # 工具函数
│   ├── public/                       # 静态资源
│   ├── package.json                  # 前端依赖配置
│   ├── vite.config.ts                # Vite配置
│   ├── tsconfig.json                 # TypeScript配置
│   └── Dockerfile                    # 前端Docker镜像
├── docs/                            # 项目文档
│   └── ARCHITECTURE.md               # 架构设计文档
├── docker-compose.yml               # Docker Compose配置
├── start.sh                         # 一键启动脚本
├── verify.sh                        # 服务验证脚本
└── README.md                        # 项目说明文档
```

## 快速启动

### 1. 前提条件
- Java 17+
- Node.js 18+
- Docker & Docker Compose（推荐）
- Maven 3.8+
- npm 9+

### 2. Docker 部署（推荐）

#### 一键启动所有服务
```bash
# 启动所有数据库和应用程序
./start.sh

# 或者手动启动
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f backend
```

#### 启动验证
```bash
# 验证所有服务状态
./verify.sh

# 检查健康状态
curl http://localhost:8080/api/actuator/health
```

**服务地址**:
- 前端界面: http://localhost
- 后端API: http://localhost:8080/api
- 数据库服务:
  - MySQL: localhost:3306 (root/password, anydb/anydb123)
  - PostgreSQL: localhost:5432 (anydb/anydb123)
  - Redis: localhost:6379 (password: anydb123)
  - Elasticsearch: localhost:9200
  - MongoDB: localhost:27017 (admin/anydb123)
  - Kafka: localhost:9092
  - ClickHouse: localhost:8123 (anydb/anydb123)

### 3. 手动开发部署

#### 后端启动
```bash
cd backend
mvn spring-boot:run

# 或者构建后运行
mvn clean package -DskipTests
java -jar target/anydb-backend-1.0.0.jar
```

#### 前端启动
```bash
cd frontend
npm install
npm run dev
```

**前端开发服务器**: http://localhost:3000

## 开发指南

### 前端开发

#### 主要页面路由
- `/` - 首页Dashboard（连接概览、状态监控）
- `/databases` - 数据库管理（添加、编辑、删除连接）
- `/sql-editor` - SQL编辑器（语法高亮、执行历史）
- `/monitor` - 性能监控（连接池状态、查询性能）
- `/settings` - 系统设置（用户配置、系统参数）

#### API调用
前端通过 `frontend/src/services/api.ts` 进行HTTP请求：
- 自动处理JWT token认证
- 统一的错误处理和重试机制
- 请求/响应拦截器
- 类型安全的TypeScript接口

#### 主要依赖和工具
```json
{
  "react": "^18.2.0",
  "antd": "^5.12.8",
  "@tanstack/react-query": "^5.17.19",
  "@monaco-editor/react": "^4.6.0",
  "axios": "^1.6.7",
  "sql-formatter": "^15.0.2",
  "@ant-design/charts": "^2.0.3"
}
```

### 后端开发

#### REST API设计
- **路径前缀**: `/api`
- **主要端点**:
  - `GET /actuator/health` - 健康检查
  - `GET /databases/types` - 获取支持的数据库类型
  - `POST /databases/test` - 测试数据库连接
  - `GET /databases/configs` - 获取数据库配置列表
  - `POST /databases/configs` - 保存数据库配置
  - `DELETE /databases/configs/{id}` - 删除配置
  - `POST /databases/{configId}/query` - 执行SQL查询
  - `GET /databases/{configId}/databases` - 获取数据库列表
  - `GET /databases/{configId}/tables` - 获取表列表

#### 数据库连接器模式
项目采用连接器模式支持多种数据库：
```java
public interface DatabaseConnector {
    boolean testConnection(DatabaseConfig config);
    List<String> getDatabases(Long configId);
    List<TableInfo> getTables(Long configId, String database);
    QueryResult executeQuery(Long configId, String sql);
    void closeConnection(Long configId);
}
```

#### 配置管理
主要配置文件 `backend/src/main/resources/application.yml` 包含：
- Spring Boot 3.2.1 配置
- Druid连接池详细配置
- Redis按需连接配置
- JWT安全配置
- Actuator健康检查
- 数据库驱动配置

#### 连接管理机制
- **按需连接**: 应用启动时不连接外部数据库
- **配置持久化**: 用户配置存储在MySQL中
- **连接池管理**: 使用Druid连接池，支持动态配置
- **健康检查**: 定期检查连接池状态和可用性
- **异常处理**: 连接失败重试、自动恢复、熔断机制

## 常用开发命令

### 前端命令
```bash
npm run dev          # 开发模式启动 (Vite)
npm run build        # 生产构建 (TypeScript + Vite)
npm run preview      # 预览构建结果
npm run lint         # ESLint代码检查
npm run type-check   # TypeScript类型检查
```

### 后端命令
```bash
mvn spring-boot:run      # 开发模式启动
mvn clean package        # 构建jar包
mvn test                 # 运行单元测试
mvn verify               # 验证构建
mvn dependency:tree      # 查看依赖树
```

### Docker命令
```bash
docker-compose up -d          # 后台启动所有服务
docker-compose down           # 停止所有服务
docker-compose logs -f        # 查看实时日志
docker-compose ps             # 查看服务状态
docker-compose restart [service] # 重启指定服务
```

### 项目管理命令
```bash
./start.sh           # 一键启动所有服务
./verify.sh          # 验证服务状态
```

## 数据库支持

### 关系型数据库
- **MySQL** 8.0+ (支持SSL、读写分离)
- **PostgreSQL** 15+ (支持JSON、数组类型)
- **Oracle Database** (支持PL/SQL)
- **Microsoft SQL Server** (支持T-SQL)
- **MariaDB** (MySQL兼容)
- **DB2** (IBM企业数据库)
- **达梦数据库** (国产数据库)
- **OceanBase** (分布式数据库)
- **TiDB** (分布式SQL数据库)

### NoSQL数据库
- **Redis** (缓存和键值存储)
- **MongoDB** 6.0+ (文档数据库)
- **Elasticsearch** 8.11+ (搜索引擎)
- **etcd** (分布式键值存储)

### 时序数据库
- **TDEngine** (时序数据处理)
- **InfluxDB** (时序数据库)

### 消息队列和大数据
- **Apache Kafka** (消息队列)
- **Apache Zookeeper** (分布式协调)
- **ClickHouse** (列式数据库)
- **Presto/Trino** (分布式查询引擎)

### 连接特性
- **连接池管理**: 每个数据库独立的连接池
- **SSL/TLS支持**: 安全连接加密
- **连接超时**: 可配置连接和查询超时
- **批量操作**: 支持批量SQL执行
- **事务支持**: 支持事务管理（关系型数据库）

## 监控和维护

### 健康检查
- **后端健康检查**: http://localhost:8080/api/actuator/health
- **Docker容器健康检查**: 自动监控服务状态
- **数据库连接监控**: 实时连接池状态
- **应用性能监控**: JVM指标、内存使用、GC情况

### 日志管理
- **后端日志**: `logs/anydb.log` (结构化日志)
- **前端日志**: 浏览器控制台 (开发模式)
- **容器日志**: `docker-compose logs -f [service-name]`
- **审计日志**: 操作记录和SQL执行历史

### 数据备份
项目中包含自动备份服务：
- **MySQL**: 每日自动备份到 `/backups/mysql/`
- **PostgreSQL**: 每日自动备份到 `/backups/postgresql/`
- **Redis**: 自动持久化 (RDB + AOF)
- **MongoDB**: 每日自动备份到 `/backups/mongodb/`

### 性能监控指标
- **连接池状态**: 活跃连接数、空闲连接数、最大连接数
- **SQL执行性能**: 执行时间、结果行数、错误率
- **系统资源**: CPU、内存、磁盘I/O使用情况
- **数据库特定指标**: 查询吞吐量、缓存命中率、索引使用情况

## 安全考虑

### 认证机制
- **JWT Token**: 24小时有效期，可配置
- **自动刷新**: 7天刷新令牌机制
- **权限控制**: 基于角色的访问控制(RBAC)
- **会话管理**: 安全的会话超时和清理

### 连接安全
- **数据库密码**: 加密存储（AES加密）
- **连接加密**: 支持SSL/TLS加密连接
- **IP白名单**: 可配置的访问控制列表
- **连接隔离**: 不同用户连接完全隔离

### 审计日志
- **操作记录**: 所有数据库操作完整记录
- **用户追踪**: 用户身份、操作时间、IP地址
- **SQL注入防护**: 参数化查询和SQL注入检测
- **敏感信息保护**: 密码和密钥自动脱敏

### 数据保护
- **配置加密**: 敏感配置信息AES加密存储
- **数据传输**: HTTPS/TLS加密传输
- **访问控制**: 细粒度权限控制
- **数据脱敏**: 敏感数据自动脱敏显示

## 开发规范

### 代码风格
- **后端**: 遵循阿里巴巴Java开发手册
- **前端**: ESLint + Prettier统一代码格式
- **TypeScript**: 严格类型检查，noImplicitAny启用
- **API设计**: RESTful设计原则，统一的响应格式

### 提交规范
- **feat**: 新功能开发
- **fix**: 修复bug
- **docs**: 文档更新
- **style**: 代码格式调整（不影响功能）
- **refactor**: 代码重构（不影响功能）
- **test**: 测试相关（单元测试、集成测试）
- **chore**: 构建或辅助工具变动

### 测试策略
- **单元测试**: JUnit 5 + Mockito (后端)，Jest (前端)
- **集成测试**: Spring Boot Test，Cypress (前端E2E)
- **代码覆盖率**: Jacoco代码覆盖率分析
- **性能测试**: 压力测试和性能基准测试

## 部署指南

### 开发环境
```bash
# 启动完整开发环境
./start.sh

# 验证所有服务
./verify.sh

# 查看服务状态
docker-compose ps
```

### 生产环境
```bash
# 生产模式构建
docker-compose -f docker-compose.prod.yml build

# 生产环境部署
docker-compose -f docker-compose.prod.yml up -d
```

### 环境变量配置
```bash
# 数据库配置
DB_HOST=mysql
DB_USERNAME=anydb
DB_PASSWORD=anydb123
DB_DATABASE=anydb

# Redis配置
REDIS_HOST=redis
REDIS_PASSWORD=anydb123
REDIS_PORT=6379

# JWT安全配置
JWT_SECRET=YourProductionSecretKey
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800

# 应用程序配置
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

### Docker优化配置
```yaml
# 生产环境Docker Compose
version: '3.8'
services:
  backend:
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
```

## 故障排除

### 常见问题

1. **端口冲突**
   ```bash
   # 检查端口占用
   lsof -i :8080
   lsof -i :3000
   # 修改docker-compose.yml中的端口映射
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库容器状态
   docker-compose ps mysql
   docker-compose ps redis
   # 查看数据库日志
   docker-compose logs mysql
   docker-compose logs redis
   # 测试数据库连接
   docker exec anydb-mysql mysql -u anydb -panydb123
   ```

3. **前端无法访问后端**
   - 检查后端服务是否正常运行
   - 验证CORS配置是否正确
   - 检查API地址配置
   - 查看浏览器开发者工具网络请求

4. **内存不足**
   ```bash
   # 检查系统资源使用
   docker stats
   # 增加Docker内存限制
   # 调整JVM参数
   java -Xmx2g -jar target/anydb-backend-1.0.0.jar
   ```

5. **连接池耗尽**
   - 检查数据库连接数限制
   - 调整连接池配置参数
   - 优化SQL查询性能
   - 增加连接池大小

### 调试技巧

1. **后端调试**
   ```bash
   # 开启调试模式
   mvn spring-boot:run -Dspring-boot.run.profiles=debug
   
   # 查看详细日志
   tail -f logs/anydb.log
   
   # 启用SQL日志
   # 在application.yml中设置: logging.level.com.anydb=DEBUG
   ```

2. **前端调试**
   ```bash
   # 开启详细日志
   npm run dev -- --debug
   
   # 检查构建结果
   npm run build && npm run preview
   
   # TypeScript类型检查
   npm run type-check
   ```

3. **Docker调试**
   ```bash
   # 进入容器调试
   docker exec -it anydb-backend bash
   docker exec -it anydb-frontend sh
   
   # 查看容器资源使用
   docker stats
   
   # 查看容器网络
   docker network ls
   docker network inspect anydb-network
   ```

### 性能调优

1. **数据库连接调优**
   - 调整连接池大小 (`spring.datasource.druid.max-active`)
   - 设置合理的连接超时时间
   - 启用连接预热 (`initial-size`)
   - 配置连接验证查询

2. **JVM调优**
   ```bash
   # 典型生产环境JVM参数
   java -Xms2g -Xmx4g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -XX:+UnlockExperimentalVMOptions \
        -XX:+UseCGroupMemoryLimitForHeap \
        -jar anydb-backend-1.0.0.jar
   ```

3. **缓存优化**
   - 配置Redis连接池
   - 合理设置缓存过期时间
   - 使用Redis集群提升可用性
   - 监控缓存命中率

## 贡献指南

1. **Fork项目**: 在GitHub上Fork仓库
2. **创建分支**: `git checkout -b feature/AmazingFeature`
3. **开发规范**: 
   - 遵循代码风格规范
   - 添加单元测试
   - 更新相关文档
   - 确保所有测试通过
4. **提交代码**: `git commit -m 'Add some AmazingFeature'`
5. **推送分支**: `git push origin feature/AmazingFeature`
6. **创建PR**: 提供清晰的PR描述和测试用例

### 开发环境要求
- **Java**: 17+
- **Node.js**: 18+
- **Docker**: 20.0+
- **Docker Compose**: 2.0+
- **Maven**: 3.8+
- **npm**: 9+
- **IDE**: IntelliJ IDEA (推荐)，VS Code (前端)

### 代码审查流程
1. 创建Pull Request
2. 自动化测试检查
3. 代码审查和质量检查
4. 性能和安全审查
5. 合并到主分支

## 路线图

### v1.1.0 (计划中)
- [ ] 支持更多数据库类型 (Cassandra, Neo4j, InfluxDB)
- [ ] 添加数据导入导出功能 (Excel, CSV, JSON)
- [ ] 支持SQL脚本批量执行和事务管理
- [ ] 优化大数据量查询性能和虚拟滚动
- [ ] 增加数据比较和同步功能

### v1.2.0 (规划中)
- [ ] 移动端响应式优化和PWA支持
- [ ] 多语言国际化支持 (中文、英文、日文)
- [ ] 插件系统和自定义连接器API
- [ ] 更强大的权限管理和审计功能
- [ ] 数据库迁移向导和版本管理

### v2.0.0 (展望)
- [ ] 微服务架构重构和容器编排
- [ ] 集群管理和负载均衡功能
- [ ] AI辅助SQL优化建议和智能分析
- [ ] 云原生部署支持和Kubernetes集成
- [ ] 实时协作和版本控制功能

---

**最后更新**: 2025-11-26  
**项目版本**: v1.0.0  
**维护团队**: AnyDB Team  

> 💡 **提示**: 本文档会根据项目发展持续更新，建议定期查看最新版本以获取最新功能和使用方法。