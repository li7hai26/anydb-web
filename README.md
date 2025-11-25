# AnyDB Web - 开源数据库管理平台

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue.svg)](https://www.typescriptlang.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

AnyDB Web是一个功能强大的开源数据库管理平台，支持多种数据库类型的连接、管理和监控。

## ✨ 主要特性

### 🔗 多数据库支持
- **关系型数据库**: MySQL, PostgreSQL, Oracle, MariaDB, SQL Server, 达梦, DB2, OceanBase, TiDB
- **NoSQL数据库**: Redis, Elasticsearch, MongoDB, Etcd
- **时序数据库**: TDEngine, InfluxDB
- **消息队列**: Kafka, Zookeeper
- **大数据**: ClickHouse, Presto, Trino

### 🎯 核心功能
- 🔐 用户认证和权限管理
- 🔗 多种数据库连接管理
- 📝 强大的SQL编辑器（支持语法高亮和自动补全）
- 📊 表结构浏览和数据查看
- ⚡ 实时性能监控
- 📋 SQL执行历史和慢查询分析
- 🔄 数据备份和恢复
- 🎨 现代化的Web界面（支持深色主题）

### 🛡️ 安全特性
- JWT身份认证
- 连接加密存储
- SQL注入防护
- 操作审计日志
- IP白名单控制

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.2.1
- **语言**: Java 17+
- **数据访问**: Spring Data JPA + MyBatis Plus
- **连接池**: Druid
- **缓存**: Redis
- **认证**: Spring Security + JWT
- **消息推送**: WebSocket
- **构建工具**: Maven

### 前端技术栈
- **框架**: React 18 + TypeScript
- **构建工具**: Vite 5
- **UI组件**: Ant Design
- **状态管理**: React Query
- **路由**: React Router
- **代码编辑**: Monaco Editor
- **图表**: Ant Design Charts

## 🚀 快速开始

### 前置要求
- Java 17+
- Node.js 18+
- Docker & Docker Compose（可选）

### 1. 克隆项目
```bash
git clone https://github.com/your-username/anydb-web.git
cd anydb-web
```

### 2. Docker部署（推荐）

#### 一键启动所有服务
```bash
# 启动所有数据库和服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

#### 访问服务
- 前端界面: http://localhost
- 后端API: http://localhost:8080/api
- 数据库服务:
  - MySQL: localhost:3306
  - PostgreSQL: localhost:5432
  - Redis: localhost:6379
  - Elasticsearch: localhost:9200
  - MongoDB: localhost:27017

### 3. 手动部署

#### 后端启动
```bash
cd backend
mvn spring-boot:run
# 或者
java -jar target/anydb-backend-1.0.0.jar
```

#### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## 📖 使用指南

### 添加数据库连接
1. 进入"数据库管理"页面
2. 点击"添加数据库"
3. 选择数据库类型，填写连接信息
4. 点击"测试连接"验证配置
5. 保存连接配置

### SQL编辑器使用
1. 进入"SQL编辑器"页面
2. 选择要操作的数据库
3. 在编辑器中输入SQL语句
4. 点击"执行"按钮运行查询
5. 查看结果并下载（如需要）

### 性能监控
1. 进入"性能监控"页面
2. 查看系统资源使用情况
3. 监控数据库连接和查询性能
4. 分析慢查询和错误日志

## 📁 项目结构

```
anydb-web/
├── backend/                    # 后端Spring Boot项目
│   ├── src/main/java/com/anydb/
│   │   ├── controller/        # REST API控制器
│   │   ├── service/           # 业务逻辑服务
│   │   ├── repository/        # 数据访问层
│   │   ├── connector/         # 数据库连接器
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── config/            # 配置类
│   │   ├── security/          # 安全认证
│   │   └── util/              # 工具类
│   └── src/main/resources/
│       ├── application.yml    # 主配置文件
│       └── db-migrations/     # 数据库迁移脚本
├── frontend/                   # 前端React项目
│   ├── src/
│   │   ├── components/        # 通用组件
│   │   ├── pages/             # 页面组件
│   │   ├── hooks/             # React Hooks
│   │   ├── services/          # API服务
│   │   ├── types/             # TypeScript类型
│   │   └── utils/             # 工具函数
│   ├── public/                # 静态资源
│   └── package.json
├── docker-compose.yml          # Docker部署配置
├── nginx/                      # Nginx配置
└── README.md                   # 项目文档
```

## 🔧 配置说明

### 后端配置
主要配置文件位于 `backend/src/main/resources/application.yml`，包含：
- 数据库连接配置
- Redis缓存配置
- JWT认证配置
- 日志配置

### 前端配置
前端环境变量配置：
```bash
# .env
VITE_API_URL=http://localhost:8080/api
VITE_APP_TITLE=AnyDB Web
VITE_APP_VERSION=1.0.0
```

## 🧪 测试

### 后端测试
```bash
cd backend
mvn test                    # 运行单元测试
mvn integration-test        # 运行集成测试
mvn verify                  # 运行所有测试
```

### 前端测试
```bash
cd frontend
npm run test                # 运行单元测试
npm run test:coverage       # 运行测试并生成覆盖率报告
npm run e2e                 # 运行端到端测试
```

## 📦 构建和部署

### 构建生产版本
```bash
# 构建后端
cd backend
mvn clean package -DskipTests

# 构建前端
cd frontend
npm run build
```

### Docker镜像构建
```bash
# 构建后端镜像
docker build -t anydb-backend ./backend

# 构建前端镜像
docker build -t anydb-frontend ./frontend

# 推送镜像到仓库
docker push your-registry/anydb-backend:latest
docker push your-registry/anydb-frontend:latest
```

## 🤝 贡献指南

我们欢迎社区贡献！请遵循以下步骤：

1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 开发规范
- 遵循现有代码风格
- 添加必要的单元测试
- 更新相关文档
- 确保所有测试通过

## 📄 许可证

本项目采用Apache 2.0许可证，详情请见 [LICENSE](LICENSE) 文件。

## 🆘 支持与反馈

- 📧 邮箱: support@anydb.com
- 🐛 问题反馈: [GitHub Issues](https://github.com/your-username/anydb-web/issues)
- 💬 讨论区: [GitHub Discussions](https://github.com/your-username/anydb-web/discussions)
- 📖 文档: [项目Wiki](https://github.com/your-username/anydb-web/wiki)

## 🎯 路线图

### v1.1.0 (计划中)
- [ ] 支持更多数据库类型 (Cassandra, Neo4j, InfluxDB)
- [ ] 添加数据导入导出功能
- [ ] 支持SQL脚本批量执行
- [ ] 优化大数据量查询性能

### v1.2.0 (规划中)
- [ ] 移动端适配优化
- [ ] 多语言国际化支持
- [ ] 插件系统和自定义连接器
- [ ] 更强大的权限管理

### v2.0.0 (展望)
- [ ] 微服务架构重构
- [ ] 集群管理功能
- [ ] AI辅助SQL优化建议
- [ ] 云原生部署支持

## 📊 项目统计

![GitHub stars](https://img.shields.io/github/stars/your-username/anydb-web.svg?style=social&label=Star)
![GitHub forks](https://img.shields.io/github/forks/your-username/anydb-web.svg?style=social&label=Fork)
![GitHub watchers](https://img.shields.io/github/watchers/your-username/anydb-web.svg?style=social&label=Watch)

## 🙏 致谢

感谢以下开源项目的支持：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [Ant Design](https://ant.design/)
- [Monaco Editor](https://microsoft.github.io/monaco-editor/)
- [Apache Druid](https://druid.apache.org/)

---

<div align="center">
  <strong>⭐ 如果这个项目对你有帮助，请给我们一个Star！ ⭐</strong>
  <br><br>
  Made with ❤️ by AnyDB Team
</div>