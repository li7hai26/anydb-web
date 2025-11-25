export interface User {
  id: number
  username: string
  email: string
  phone?: string
  department?: string
  avatar?: string
  role: string
  status: 'active' | 'inactive' | 'banned'
  createTime: string
  lastLoginTime?: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  expiresIn: number
  user: User
}

export interface LoginRequest {
  username: string
  password: string
  remember?: boolean
}

export interface SystemConfig {
  theme: 'light' | 'dark' | 'auto'
  language: 'zh-CN' | 'en-US' | 'ja-JP'
  autoSave: boolean
  enableNotifications: boolean
  enableAuditLog: boolean
  sessionTimeout: number
}

export interface NotificationSettings {
  emailQueryResults: boolean
  emailErrors: boolean
  emailDailyReport: boolean
  browserNotifications: boolean
  soundNotifications: boolean
  quietHoursStart: string
  quietHoursEnd: string
}

export interface SecuritySettings {
  twoFactor: boolean
  sessionTimeout: number
  encryptPasswords: boolean
  requireSSL: boolean
  ipWhitelist: string[]
}

export interface DatabaseConnection {
  id: number
  name: string
  type: string
  host: string
  port: number
  database?: string
  username: string
  status: 'connected' | 'disconnected' | 'error' | 'testing'
  enabled: boolean
  createTime: string
  lastTestTime?: string
}

export interface QueryLog {
  id: number
  databaseId: number
  databaseName: string
  sql: string
  executionTime: number
  affectedRows: number
  status: 'success' | 'error' | 'timeout'
  errorMessage?: string
  userId: number
  userName: string
  executeTime: string
}

export interface PerformanceMetric {
  timestamp: string
  responseTime: number
  throughput: number
  errorRate: number
  cpuUsage: number
  memoryUsage: number
  activeConnections: number
  maxConnections: number
}

export interface SlowQuery {
  id: number
  databaseId: number
  databaseName: string
  sql: string
  duration: number
  rowsAffected: number
  userId: number
  userName: string
  timestamp: string
  status: 'running' | 'completed' | 'failed' | 'timeout'
  executionPlan?: string
}

export interface ApiResponse<T = any> {
  success: boolean
  data: T
  message?: string
  code?: number
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface ChartData {
  date: string
  value: number
  name?: string
}