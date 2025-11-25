# AnyDB Web 项目开发指南

## 项目概述

**AnyDB Web** 是一个功能强大的开源数据库管理平台，采用现代化的前后端分离架构，支持多种数据库类型的连接、管理和监控。

### 核心特性
- 🔗 **多数据库支持**: MySQL, PostgreSQL, Redis, Elasticsearch, MongoDB, Kafka, ClickHouse 等 15+ 种数据库
- 📝 **SQL编辑器**: 基于 Monaco Editor，支持语法高亮、自动补全、SQL格式化
- 📊 **实时监控**: 数据库连接状态、性能指标、慢查询分析
- 🔐 **安全认证**: JWT身份认证、权限控制、操作审计
- 🎨 **现代化UI**: 基于 Ant Design，支持深色主题、响应式设计
- ⚡ **高性能**: Druid连接池、Redis缓存、异步处理

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2.1
- **语言**: Java 17+
- **数据访问**: Spring Data JPA + MyBatis Plus
- **连接池**: Alibaba Druid
- **缓存**: Redis + Spring Cache
- **认证**: Spring Security + JWT
- **消息推送**: WebSocket
- **构建**: Maven

### 前端技术栈
- **框架**: React 18 + TypeScript
- **构建工具**: Vite 5
- **UI组件**: Ant Design 5.x
- **状态管理**: React Query + React Hooks
- **路由**: React Router 6
- **代码编辑**: Monaco Editor
- **图表**: Ant Design Charts

### 部署架构
- **容器化**: Docker + Docker Compose
- **反向代理**: Nginx
- **数据存储**: Docker Volumes
- **网络**: Bridge Network

## 项目结构

```
anydb-web/
├── backend/                     # Spring Boot 后端项目
│   ├── src/main/java/com/anydb/
│   │   ├── AnyDBBackendApplication.java    # 启动类
│   │   ├── connector/           # 数据库连接器模块
│   │   │   ├── DatabaseConfig.java         # 数据库配置
│   │   │   ├── DatabaseConnector.java      # 连接器接口
│   │   │   ├── DatabaseType.java           # 数据库类型枚举
│   │   │   └── impl/                         # 具体实现
│   │   ├── controller/         # REST API控制器
│   │   │   └── DatabaseController.java      # 数据库管理API
│   │   └── service/            # 业务逻辑服务
│   │       └── DatabaseService.java         # 数据库服务
│   ├── src/main/resources/
│   │   └── application.yml     # 主配置文件
│   ├── pom.xml                 # Maven依赖配置
│   └── Dockerfile              # 后端Docker镜像
├── frontend/                    # React 前端项目
│   ├── src/
│   │   ├── App.tsx             # 应用根组件
│   │   ├── main.tsx            # 应用入口
│   │   ├── components/layout/  # 布局组件
│   │   │   └── MainLayout.tsx  # 主布局
│   │   ├── pages/              # 页面组件
│   │   │   ├── Home.tsx        # 首页
│   │   │   ├── DatabaseManager.tsx  # 数据库管理
│   │   │   ├── SQLEditor.tsx   # SQL编辑器
│   │   │   ├── Monitor.tsx     # 监控页面
│   │   │   └── Settings.tsx    # 设置页面
│   │   ├── services/           # API服务
│   │   │   ├── api.ts          # HTTP客户端
│   │   │   └── database.ts     # 数据库相关API
│   │   ├── types/              # TypeScript类型定义
│   │   └── utils/              # 工具函数
│   ├── package.json            # 前端依赖配置
│   ├── vite.config.ts          # Vite配置
│   ├── tsconfig.json           # TypeScript配置
│   └── Dockerfile              # 前端Docker镜像
├── docker-compose.yml          # Docker Compose配置
├── start.sh                    # 启动脚本
├── verify.sh                   # 验证脚本
└── README.md                   # 项目说明文档
```

## 快速启动

### 1. Docker 部署（推荐）

```bash
# 克隆项目
git clone https://github.com/your-username/anydb-web.git
cd anydb-web

# 一键启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

**服务地址**:
- 前端界面: http://localhost
- 后端API: http://localhost:8080/api
- Druid监控: http://localhost:8080/api/druid

### 2. 手动开发部署

#### 后端启动
```bash
cd backend
mvn spring-boot:run
# 或者
mvn clean package -DskipTests
java -jar target/anydb-backend-1.0.0.jar
```

#### 前端启动
```bash
cd frontend
npm install
npm run dev
```

**前端开发服务器**: http://localhost:5173

## 开发指南

### 前端开发

#### 主要页面路由
- `/` - 首页Dashboard
- `/databases` - 数据库管理
- `/sql-editor` - SQL编辑器
- `/monitor` - 性能监控
- `/settings` - 系统设置

#### API调用
前端通过 `frontend/src/services/api.ts` 进行HTTP请求：
- 自动处理JWT token
- 统一的错误处理
- 请求/响应拦截器

#### 主要依赖
```json
{
  "react": "^18.2.0",
  "antd": "^5.12.8",
  "@tanstack/react-query": "^5.17.19",
  "@monaco-editor/react": "^4.6.0",
  "axios": "^1.6.7"
}
```

### 后端开发

#### REST API设计
- **路径前缀**: `/api/databases`
- **主要端点**:
  - `GET /types` - 获取支持的数据库类型
  - `POST /test` - 测试数据库连接
  - `POST /{configId}/databases` - 获取数据库列表
  - `POST /{configId}/tables` - 获取表列表
  - `POST /{configId}/query` - 执行SQL查询

#### 数据库连接器模式
项目采用连接器模式支持多种数据库：
```java
public interface DatabaseConnector {
    boolean testConnection(DatabaseConfig config);
    List<String> getDatabases(Long configId);
    List<TableInfo> getTables(Long configId, String database);
    QueryResult executeQuery(Long configId, String sql);
}
```

#### 配置管理
主要配置文件 `application.yml` 包含：
- Spring Boot配置
- Druid连接池配置
- Redis缓存配置
- JWT安全配置
- 自定义业务配置

## 常用开发命令

### 前端命令
```bash
npm run dev      # 开发模式启动
npm run build    # 生产构建
npm run preview  # 预览构建结果
npm run lint     # 代码检查
npm run type-check # 类型检查
```

### 后端命令
```bash
mvn spring-boot:run  # 开发模式启动
mvn clean package    # 构建jar包
mvn test             # 运行测试
mvn verify           # 验证构建
```

### Docker命令
```bash
docker-compose up -d     # 后台启动所有服务
docker-compose down      # 停止所有服务
docker-compose logs -f   # 查看实时日志
docker-compose ps        # 查看服务状态
```

## 数据库支持

### 关系型数据库
- MySQL 8.0+
- PostgreSQL 15+
- Oracle Database
- Microsoft SQL Server
- MariaDB
- DB2
- 达梦数据库
- OceanBase
- TiDB

### NoSQL数据库
- Redis
- MongoDB 6.0+
- Elasticsearch 8.11+
- etcd

### 大数据和时序数据库
- ClickHouse
- Apache Kafka
- Apache Zookeeper
- TDEngine
- InfluxDB

## 监控和维护

### 健康检查
- **后端健康检查**: http://localhost:8080/api/actuator/health
- **Druid监控**: http://localhost:8080/api/druid
- **容器健康检查**: Docker Compose自动监控

### 日志管理
- **后端日志**: `logs/anydb.log`
- **前端日志**: 浏览器控制台
- **容器日志**: `docker-compose logs -f [service-name]`

### 数据备份
项目中包含自动备份服务：
- MySQL: 每日自动备份
- PostgreSQL: 每日自动备份
- Redis: 自动持久化

## 安全考虑

### 认证机制
- **JWT Token**: 24小时有效期
- **自动刷新**: 7天刷新令牌
- **权限控制**: 基于角色的访问控制

### 连接安全
- **数据库密码**: 加密存储
- **连接加密**: 支持SSL/TLS
- **IP白名单**: 可配置的访问控制

### 审计日志
- **操作记录**: 所有数据库操作记录
- **用户追踪**: 用户操作行为追踪
- **SQL注入防护**: 参数化查询

## 开发规范

### 代码风格
- **后端**: 遵循阿里巴巴Java开发手册
- **前端**: ESLint + Prettier统一代码格式
- **TypeScript**: 严格类型检查

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构代码
- test: 测试相关
- chore: 构建或辅助工具变动

### 测试策略
- **单元测试**: JUnit 5 + Mockito
- **集成测试**: Spring Boot Test
- **前端测试**: Jest + React Testing Library

## 部署指南

### 开发环境
```bash
# 启动开发环境
./start.sh

# 验证服务
./verify.sh
```

### 生产环境
```bash
# 构建生产镜像
docker-compose -f docker-compose.prod.yml build

# 部署到生产
docker-compose -f docker-compose.prod.yml up -d
```

### 环境变量配置
```bash
# 数据库配置
DB_HOST=mysql
DB_USERNAME=anydb
DB_PASSWORD=anydb123

# Redis配置
REDIS_HOST=redis
REDIS_PASSWORD=anydb123

# JWT配置
JWT_SECRET=YourSecretKey
JWT_EXPIRATION=86400
```

## 故障排除

### 常见问题

1. **端口冲突**
   ```bash
   # 检查端口占用
   lsof -i :8080
   # 修改docker-compose.yml中的端口映射
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库容器状态
   docker-compose ps mysql
   # 查看数据库日志
   docker-compose logs mysql
   ```

3. **前端无法访问后端**
   - 检查后端服务是否正常运行
   - 验证CORS配置
   - 检查API地址配置

### 调试技巧

1. **后端调试**
   ```bash
   # 开启调试模式
   mvn spring-boot:run -Dspring-boot.run.profiles=debug
   
   # 查看详细日志
   tail -f logs/anydb.log
   ```

2. **前端调试**
   ```bash
   # 开启详细日志
   npm run dev -- --debug
   
   # 检查构建结果
   npm run build && npm run preview
   ```

3. **Docker调试**
   ```bash
   # 进入容器调试
   docker exec -it anydb-backend bash
   
   # 查看容器资源使用
   docker stats
   ```

## 贡献指南

1. Fork 项目仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 创建 Pull Request

### 开发环境要求
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.8+
- npm 9+

---

**最后更新**: 2024-11-26
**项目版本**: v1.0.0
**维护团队**: AnyDB Team