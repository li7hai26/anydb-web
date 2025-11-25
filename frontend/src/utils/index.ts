import { format, parseISO, isValid } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * 格式化日期
 */
export const formatDate = (date: string | Date, formatStr = 'yyyy-MM-dd HH:mm:ss'): string => {
  try {
    const dateObj = typeof date === 'string' ? parseISO(date) : date
    if (!isValid(dateObj)) {
      return '-'
    }
    return format(dateObj, formatStr, { locale: zhCN })
  } catch (error) {
    return '-'
  }
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

/**
 * 格式化数字
 */
export const formatNumber = (num: number): string => {
  return new Intl.NumberFormat('zh-CN').format(num)
}

/**
 * 格式化百分比
 */
export const formatPercent = (value: number, total: number): string => {
  if (total === 0) return '0%'
  return `${((value / total) * 100).toFixed(1)}%`
}

/**
 * 防抖函数
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout
  return (...args: Parameters<T>) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func.apply(null, args), wait)
  }
}

/**
 * 节流函数
 */
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let lastTime = 0
  return (...args: Parameters<T>) => {
    const now = Date.now()
    if (now - lastTime >= wait) {
      lastTime = now
      func.apply(null, args)
    }
  }
}

/**
 * 深度克隆对象
 */
export const deepClone = <T>(obj: T): T => {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }

  if (obj instanceof Date) {
    return new Date(obj.getTime()) as T
  }

  if (obj instanceof Array) {
    return obj.map(item => deepClone(item)) as T
  }

  if (typeof obj === 'object') {
    const cloned = {} as T
    Object.keys(obj).forEach(key => {
      ;(cloned as any)[key] = deepClone((obj as any)[key])
    })
    return cloned
  }

  return obj
}

/**
 * 检查是否为移动设备
 */
export const isMobile = (): boolean => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

/**
 * 生成随机字符串
 */
export const generateRandomString = (length: number): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 下载文件
 */
export const downloadFile = (data: Blob | string, filename: string, type?: string) => {
  const blob = typeof data === 'string' 
    ? new Blob([data], { type: type || 'text/plain' })
    : data
    
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  window.URL.revokeObjectURL(url)
  document.body.removeChild(a)
}

/**
 * 复制到剪贴板
 */
export const copyToClipboard = async (text: string): Promise<boolean> => {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
      return true
    } else {
      // 回退方案
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      const success = document.execCommand('copy')
      document.body.removeChild(textArea)
      return success
    }
  } catch (error) {
    console.error('复制失败:', error)
    return false
  }
}

/**
 * 获取查询参数
 */
export const getQueryParam = (param: string): string | null => {
  const urlParams = new URLSearchParams(window.location.search)
  return urlParams.get(param)
}

/**
 * 设置查询参数
 */
export const setQueryParam = (key: string, value: string): void => {
  const url = new URL(window.location.href)
  url.searchParams.set(key, value)
  window.history.pushState({}, '', url.toString())
}

/**
 * 删除查询参数
 */
export const removeQueryParam = (key: string): void => {
  const url = new URL(window.location.href)
  url.searchParams.delete(key)
  window.history.pushState({}, '', url.toString())
}

/**
 * 获取随机颜色
 */
export const getRandomColor = (): string => {
  const colors = [
    '#1890ff', '#52c41a', '#faad14', '#ff4d4f', 
    '#722ed1', '#fa8c16', '#13c2c2', '#eb2f96',
    '#1890ff', '#36cfc9', '#73d13d', '#f759ab'
  ]
  return colors[Math.floor(Math.random() * colors.length)]
}

/**
 * SQL语法高亮
 */
export const highlightSQL = (sql: string): string => {
  if (!sql) return ''
  
  return sql
    .replace(/\b(SELECT|FROM|WHERE|INSERT|UPDATE|DELETE|JOIN|LEFT|RIGHT|INNER|OUTER|GROUP|ORDER|BY|LIMIT|OFFSET|AND|OR|NOT|IN|BETWEEN|LIKE|IS|NULL)\b/gi, '<span class="sql-keyword">$1</span>')
    .replace(/'([^']*)'/g, '<span class="sql-string">\'$1\'</span>')
    .replace(/"([^"]*)"/g, '<span class="sql-string">"$1"</span>')
    .replace(/\b(\d+\.?\d*)\b/g, '<span class="sql-number">$1</span>')
    .replace(/--.*$/gm, '<span class="sql-comment">$&</span>')
    .replace(/\/\*[\s\S]*?\*\//g, '<span class="sql-comment">$&</span>')
}

/**
 * 验证邮箱格式
 */
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证手机号格式
 */
export const validatePhone = (phone: string): boolean => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 计算相对时间
 */
export const getRelativeTime = (date: string | Date): string => {
  const now = new Date()
  const target = typeof date === 'string' ? parseISO(date) : date
  const diffInSeconds = Math.floor((now.getTime() - target.getTime()) / 1000)
  
  if (diffInSeconds < 60) {
    return '刚刚'
  } else if (diffInSeconds < 3600) {
    return `${Math.floor(diffInSeconds / 60)}分钟前`
  } else if (diffInSeconds < 86400) {
    return `${Math.floor(diffInSeconds / 3600)}小时前`
  } else if (diffInSeconds < 2592000) {
    return `${Math.floor(diffInSeconds / 86400)}天前`
  } else if (diffInSeconds < 31536000) {
    return `${Math.floor(diffInSeconds / 2592000)}个月前`
  } else {
    return `${Math.floor(diffInSeconds / 31536000)}年前`
  }
}