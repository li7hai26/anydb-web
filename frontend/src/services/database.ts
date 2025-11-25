import api from './api'

export interface DatabaseConfig {
  id?: number
  name: string
  type: string
  host: string
  port: number
  database?: string
  username: string
  password?: string
  remark?: string
  enabled: boolean
}

export interface DatabaseType {
  code: string
  displayName: string
  driverClass: string
  urlPrefix: string
}

export interface QueryResult {
  columns: string[]
  rows: any[][]
  total: number
  executionTime: number
}

export interface UpdateResult {
  affectedRows: number
  executionTime: number
  message: string
}

export interface TableInfo {
  name: string
  comment: string
  columns: ColumnInfo[]
  rowCount: number
  createTime: number
  updateTime: number
}

export interface ColumnInfo {
  name: string
  type: string
  comment: string
  nullable: boolean
  primaryKey: boolean
  maxLength?: number
  precision?: number
  scale?: number
  defaultValue?: string
}

export interface DatabaseService {
  // 获取支持的数据库类型
  getSupportedTypes(): Promise<DatabaseType[]>
  
  // 测试连接
  testConnection(config: DatabaseConfig): Promise<{ success: boolean; message: string }>
  
  // 获取数据库列表
  getDatabases(configId: number): Promise<string[]>
  
  // 获取表列表
  getTables(configId: number, database?: string): Promise<TableInfo[]>
  
  // 获取表结构信息
  getTableInfo(configId: number, database: string, tableName: string): Promise<TableInfo>
  
  // 获取表数据
  getTableData(
    configId: number, 
    database: string, 
    tableName: string,
    page: number,
    size: number,
    orderBy?: string,
    orderDirection?: string
  ): Promise<QueryResult>
  
  // 执行SQL查询
  executeQuery(configId: number, sql: string): Promise<QueryResult>
  
  // 执行SQL更新
  executeUpdate(configId: number, sql: string): Promise<UpdateResult>
}

const databaseService: DatabaseService = {
  async getSupportedTypes() {
    const response = await api.get('/databases/types')
    return response.data
  },

  async testConnection(config: DatabaseConfig) {
    const response = await api.post('/databases/test', config)
    return response.data
  },

  async getDatabases(configId: number) {
    const response = await api.post(`/databases/${configId}/databases`)
    return response.data
  },

  async getTables(configId: number, database?: string) {
    const params = database ? { database } : {}
    const response = await api.post(`/databases/${configId}/tables`, null, { params })
    return response.data
  },

  async getTableInfo(configId: number, database: string, tableName: string) {
    const response = await api.post(`/databases/${configId}/table-info`, null, {
      params: { database, tableName }
    })
    return response.data
  },

  async getTableData(configId: number, database: string, tableName: string, page: number, size: number, orderBy?: string, orderDirection?: string) {
    const params = {
      database,
      tableName,
      page,
      size,
      orderBy,
      orderDirection
    }
    const response = await api.post(`/databases/${configId}/table-data`, null, { params })
    return response.data
  },

  async executeQuery(configId: number, sql: string) {
    const response = await api.post(`/databases/${configId}/query`, null, {
      params: { sql }
    })
    return response.data
  },

  async executeUpdate(configId: number, sql: string) {
    const response = await api.post(`/databases/${configId}/update`, null, {
      params: { sql }
    })
    return response.data
  }
}

export default databaseService