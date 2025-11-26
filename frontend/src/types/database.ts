/**
 * 数据库类型配置映射
 * 为前端提供所有支持的数据库类型的显示配置
 */

export interface DatabaseTypeConfig {
  code: string
  displayName: string
  description: string
  icon: string
  color: string
  category: 'relational' | 'nosql' | 'timeseries' | 'analytics' | 'messaging' | 'enterprise'
  defaultPort: number
  sslSupported: boolean
  features: string[]
  supportedOperations: string[]
}

// 数据库类型配置映射
export const DATABASE_TYPE_CONFIGS: Record<string, DatabaseTypeConfig> = {
  // 关系型数据库
  mysql: {
    code: 'mysql',
    displayName: 'MySQL',
    description: '开源关系型数据库管理系统',
    icon: '🐬',
    color: '#00758f',
    category: 'relational',
    defaultPort: 3306,
    sslSupported: true,
    features: ['事务', '外键', '存储过程', '触发器'],
    supportedOperations: ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'DDL', 'DCL']
  },
  postgresql: {
    code: 'postgresql',
    displayName: 'PostgreSQL',
    description: '高级开源关系型数据库',
    icon: '🐘',
    color: '#336791',
    category: 'relational',
    defaultPort: 5432,
    sslSupported: true,
    features: ['JSON支持', '全文搜索', '存储过程', '视图'],
    supportedOperations: ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'DDL', 'DCL']
  },
  mariadb: {
    code: 'mariadb',
    displayName: 'MariaDB',
    description: 'MySQL的分支，兼容MySQL',
    icon: '🐬',
    color: '#003545',
    category: 'relational',
    defaultPort: 3306,
    sslSupported: true,
    features: ['Galera集群', '高性能', '高可用'],
    supportedOperations: ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'DDL', 'DCL']
  },
  oracle: {
    code: 'oracle',
    displayName: 'Oracle',
    description: '企业级关系型数据库',
    icon: '🏢',
    color: '#ff0000',
    category: 'enterprise',
    defaultPort: 1521,
    sslSupported: true,
    features: ['分布式事务', '分区', '数据仓库', '高性能'],
    supportedOperations: ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'DDL', 'DCL', 'PLSQL']
  },
  sqlserver: {
    code: 'sqlserver',
    displayName: 'SQL Server',
    description: 'Microsoft关系型数据库管理系统',
    icon: '💿',
    color: '#0078d4',
    category: 'enterprise',
    defaultPort: 1433,
    sslSupported: true,
    features: ['报表服务', '分析服务', '集成服务'],
    supportedOperations: ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'DDL', 'DCL', 'T-SQL']
  },
  tidb: {
    code: 'tidb',
    displayName: 'TiDB',
    description: '国产分布式关系型数据库',
    icon: '⚡',
    color: '#00b894',
    category: 'analytics',
    defaultPort: 4000,
    sslSupported: true,
    features: ['水平扩展', '强一致性', '实时分析'],
    supportedOperations: ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'DDL']
  },
  clickhouse: {
    code: 'clickhouse',
    displayName: 'ClickHouse',
    description: '列式分析数据库',
    icon: '📊',
    color: '#fca311',
    category: 'analytics',
    defaultPort: 8123,
    sslSupported: true,
    features: ['实时分析', '大数据处理', '物化视图'],
    supportedOperations: ['SELECT', 'INSERT', 'DDL']
  },

  // NoSQL数据库
  redis: {
    code: 'redis',
    displayName: 'Redis',
    description: '开源内存数据结构存储',
    icon: '❤️',
    color: '#dc382d',
    category: 'nosql',
    defaultPort: 6379,
    sslSupported: true,
    features: ['缓存', '发布订阅', '事务', '脚本'],
    supportedOperations: ['GET', 'SET', 'INCR', 'LIST', 'SET', 'HASH', 'SORTED_SET']
  },
  mongodb: {
    code: 'mongodb',
    displayName: 'MongoDB',
    description: '文档导向的NoSQL数据库',
    icon: '🍃',
    color: '#4db33d',
    category: 'nosql',
    defaultPort: 27017,
    sslSupported: true,
    features: ['文档存储', '复制集', '分片', '索引'],
    supportedOperations: ['find', 'aggregate', 'insert', 'update', 'remove']
  },
  elasticsearch: {
    code: 'elasticsearch',
    displayName: 'Elasticsearch',
    description: '分布式搜索和分析引擎',
    icon: '🔍',
    color: '#ff0000',
    category: 'nosql',
    defaultPort: 9200,
    sslSupported: true,
    features: ['全文搜索', '实时搜索', '聚合', '地理搜索'],
    supportedOperations: ['search', 'index', 'update', 'delete', 'aggregate']
  },

  // 企业数据库 (未完全实现)
  oceanbase: {
    code: 'oceanbase',
    displayName: 'OceanBase',
    description: '分布式关系型数据库',
    icon: '🌊',
    color: '#0066cc',
    category: 'enterprise',
    defaultPort: 2881,
    sslSupported: true,
    features: ['分布式', '高可用', '高并发'],
    supportedOperations: ['SELECT', 'INSERT', 'UPDATE', 'DELETE']
  },

  // 消息队列 (待实现)
  kafka: {
    code: 'kafka',
    displayName: 'Apache Kafka',
    description: '分布式流处理平台',
    icon: '📡',
    color: '#000000',
    category: 'messaging',
    defaultPort: 9092,
    sslSupported: true,
    features: ['消息队列', '流处理', '数据管道'],
    supportedOperations: ['produce', 'consume', 'admin']
  },

  // 分布式协调 (待实现)
  zookeeper: {
    code: 'zookeeper',
    displayName: 'ZooKeeper',
    description: '分布式协调服务',
    icon: '🦓',
    color: '#b4a9ff',
    category: 'messaging',
    defaultPort: 2181,
    sslSupported: false,
    features: ['配置管理', '命名服务', '分布式同步'],
    supportedOperations: ['get', 'set', 'create', 'delete', 'list']
  }
}

// 数据库类别配置
export const DATABASE_CATEGORIES = {
  relational: {
    name: '关系型数据库',
    description: '支持SQL查询和事务处理',
    color: '#1890ff',
    icon: '📊'
  },
  nosql: {
    name: 'NoSQL数据库',
    description: '文档、键值和搜索引擎',
    color: '#722ed1',
    icon: '🔍'
  },
  timeseries: {
    name: '时序数据库',
    description: '时间序列数据存储和分析',
    color: '#52c41a',
    icon: '⏰'
  },
  analytics: {
    name: '分析数据库',
    description: 'OLAP和大数据分析',
    color: '#fa8c16',
    icon: '📈'
  },
  messaging: {
    name: '消息系统',
    description: '消息队列和协调服务',
    color: '#eb2f96',
    icon: '📢'
  },
  enterprise: {
    name: '企业数据库',
    description: '企业级关系型数据库',
    color: '#f5222d',
    icon: '🏢'
  }
}

// 数据库特性配置
export const DATABASE_FEATURES = {
  transactions: {
    name: '事务支持',
    icon: '🔄',
    color: '#52c41a'
  },
  ssl: {
    name: 'SSL加密',
    icon: '🔒',
    color: '#1890ff'
  },
  replication: {
    name: '数据复制',
    icon: '📋',
    color: '#fa8c16'
  },
  clustering: {
    name: '集群支持',
    icon: '🏗️',
    color: '#722ed1'
  },
  sharding: {
    name: '分片',
    icon: '🗂️',
    color: '#eb2f96'
  },
  fulltext: {
    name: '全文搜索',
    icon: '🔍',
    color: '#13c2c2'
  }
}

// 工具函数
export function getDatabaseTypeConfig(typeCode: string): DatabaseTypeConfig | null {
  return DATABASE_TYPE_CONFIGS[typeCode] || null
}

export function getDatabaseTypeByCategory(category: string): DatabaseTypeConfig[] {
  return Object.values(DATABASE_TYPE_CONFIGS).filter(config => config.category === category)
}

export function getSupportedDatabaseTypes(): DatabaseTypeConfig[] {
  return Object.values(DATABASE_TYPE_CONFIGS)
}

export function getDatabaseCategoryInfo(category: keyof typeof DATABASE_CATEGORIES) {
  return DATABASE_CATEGORIES[category]
}

// 数据库操作权限配置
export const OPERATION_PERMISSIONS = {
  SELECT: {
    name: '查询数据',
    icon: '👀',
    color: '#52c41a'
  },
  INSERT: {
    name: '插入数据',
    icon: '➕',
    color: '#1890ff'
  },
  UPDATE: {
    name: '更新数据',
    icon: '✏️',
    color: '#fa8c16'
  },
  DELETE: {
    name: '删除数据',
    icon: '🗑️',
    color: '#f5222d'
  },
  DDL: {
    name: '结构操作',
    icon: '🏗️',
    color: '#722ed1'
  },
  DCL: {
    name: '权限管理',
    icon: '🔐',
    color: '#eb2f96'
  }
}

export function hasOperationPermission(dbType: string, operation: string): boolean {
  const config = getDatabaseTypeConfig(dbType)
  return config ? config.supportedOperations.includes(operation) : false
}

// 数据库状态图标映射
export const DATABASE_STATUS_ICONS = {
  connected: '🟢',
  disconnected: '⚫',
  testing: '🟡',
  error: '🔴',
  unknown: '❓'
}

// 数据库性能指标配置
export const PERFORMANCE_METRICS = {
  query_time: {
    name: '查询时间',
    unit: 'ms',
    threshold: 1000,
    color: '#1890ff'
  },
  throughput: {
    name: '吞吐量',
    unit: 'QPS',
    threshold: 100,
    color: '#52c41a'
  },
  connection_pool: {
    name: '连接池使用率',
    unit: '%',
    threshold: 80,
    color: '#fa8c16'
  },
  memory_usage: {
    name: '内存使用率',
    unit: '%',
    threshold: 85,
    color: '#f5222d'
  },
  disk_usage: {
    name: '磁盘使用率',
    unit: '%',
    threshold: 90,
    color: '#722ed1'
  }
}
