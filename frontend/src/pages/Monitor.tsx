import React, { useState } from 'react'
import { Card, Row, Col, Select, DatePicker, Table, Tag, Progress, Space } from 'antd'
import { Line, Column, Pie } from '@ant-design/charts'
import { ClockCircleOutlined, DatabaseOutlined, BugOutlined, CheckCircleOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import type { ColumnsType } from 'antd/es/table'

const { RangePicker } = DatePicker

interface PerformanceMetric {
  timestamp: string
  responseTime: number
  throughput: number
  errorRate: number
  cpuUsage: number
  memoryUsage: number
}

interface SlowQuery {
  id: number
  database: string
  sql: string
  duration: number
  rowsAffected: number
  timestamp: string
  status: 'running' | 'completed' | 'failed'
}

const Monitor: React.FC = () => {
  const [selectedDatabase, setSelectedDatabase] = useState<string>('all')
  const [timeRange, setTimeRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
    dayjs().subtract(24, 'hour'),
    dayjs()
  ])

  const databaseOptions = [
    { value: 'all', label: '全部数据库' },
    { value: 'mysql-1', label: 'MySQL - 生产环境' },
    { value: 'postgresql-1', label: 'PostgreSQL - 测试环境' },
    { value: 'redis-1', label: 'Redis - 缓存' },
  ]

  const performanceData: PerformanceMetric[] = Array.from({ length: 24 }, (_, i) => ({
    timestamp: dayjs().subtract(23 - i, 'hour').format('HH:mm'),
    responseTime: Math.random() * 500 + 100,
    throughput: Math.random() * 1000 + 500,
    errorRate: Math.random() * 5,
    cpuUsage: Math.random() * 80 + 10,
    memoryUsage: Math.random() * 70 + 20,
  }))

  const slowQueries: SlowQuery[] = [
    {
      id: 1,
      database: 'MySQL',
      sql: 'SELECT u.*, o.* FROM users u LEFT JOIN orders o ON u.id = o.user_id WHERE u.created_at > "2024-11-01" ORDER BY o.created_at DESC LIMIT 1000',
      duration: 2340,
      rowsAffected: 1000,
      timestamp: '2024-11-25 14:30:22',
      status: 'completed'
    },
    {
      id: 2,
      database: 'PostgreSQL',
      sql: 'SELECT COUNT(*) FROM analytics_data WHERE event_date BETWEEN $1 AND $2',
      duration: 1890,
      rowsAffected: 0,
      timestamp: '2024-11-25 14:28:15',
      status: 'completed'
    },
    {
      id: 3,
      database: 'MySQL',
      sql: 'UPDATE user_sessions SET last_activity = NOW() WHERE user_id IN (SELECT id FROM users WHERE active = 1)',
      duration: 1560,
      rowsAffected: 1247,
      timestamp: '2024-11-25 14:25:33',
      status: 'running'
    },
    {
      id: 4,
      database: 'Redis',
      sql: 'KEYS user:session:*',
      duration: 890,
      rowsAffected: 5432,
      timestamp: '2024-11-25 14:20:11',
      status: 'completed'
    }
  ]

  const responseTimeConfig = {
    data: performanceData,
    xField: 'timestamp',
    yField: 'responseTime',
    smooth: true,
    color: '#1890ff',
    point: {
      size: 3,
      shape: 'diamond',
    },
    label: {
      style: {
        fill: '#aaa',
      },
    },
  }

  const throughputConfig = {
    data: performanceData,
    xField: 'timestamp',
    yField: 'throughput',
    color: '#52c41a',
    columnWidthRatio: 0.8,
  }

  const errorRateData = [
    { type: '正常', value: 95 },
    { type: '警告', value: 4 },
    { type: '错误', value: 1 },
  ]

  const errorRateConfig = {
    data: errorRateData,
    angleField: 'value',
    colorField: 'type',
    radius: 0.8,
    label: {
      type: 'outer',
      content: '{name} {percentage}',
    },
    interactions: [
      {
        type: 'element-active',
      },
    ],
  }

  const slowQueryColumns: ColumnsType<SlowQuery> = [
    {
      title: '数据库',
      dataIndex: 'database',
      key: 'database',
      render: (text) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: 'SQL语句',
      dataIndex: 'sql',
      key: 'sql',
      render: (sql) => (
        <div style={{ maxWidth: '400px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          <code style={{ fontSize: '12px' }}>{sql}</code>
        </div>
      ),
    },
    {
      title: '执行时间',
      dataIndex: 'duration',
      key: 'duration',
      render: (duration) => (
        <span style={{ color: duration > 1000 ? '#ff4d4f' : '#52c41a' }}>
          {duration}ms
        </span>
      ),
    },
    {
      title: '影响行数',
      dataIndex: 'rowsAffected',
      key: 'rowsAffected',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const statusConfig: Record<string, { color: string; text: string }> = {
          running: { color: 'processing', text: '执行中' },
          completed: { color: 'success', text: '已完成' },
          failed: { color: 'error', text: '失败' },
        }
        const config = statusConfig[status as string] || statusConfig.running
        return <Tag color={config.color}>{config.text}</Tag>
      },
    },
    {
      title: '执行时间',
      dataIndex: 'timestamp',
      key: 'timestamp',
    },
  ]

  return (
    <div>
      <h2>性能监控</h2>
      
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Card>
            <Space>
              <DatabaseOutlined style={{ fontSize: 24, color: '#1890ff' }} />
              <div>
                <div style={{ fontSize: '12px', color: '#666' }}>活跃连接</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold' }}>47</div>
              </div>
            </Space>
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Space>
              <ClockCircleOutlined style={{ fontSize: 24, color: '#52c41a' }} />
              <div>
                <div style={{ fontSize: '12px', color: '#666' }}>平均响应时间</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold' }}>285ms</div>
              </div>
            </Space>
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Space>
              <BugOutlined style={{ fontSize: 24, color: '#faad14' }} />
              <div>
                <div style={{ fontSize: '12px', color: '#666' }}>错误率</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold' }}>0.5%</div>
              </div>
            </Space>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col span={12}>
          <Card title="响应时间趋势" extra={
            <Space>
              <Select 
                style={{ width: 120 }} 
                value={selectedDatabase}
                onChange={setSelectedDatabase}
              >
                {databaseOptions.map(option => (
                  <Select.Option key={option.value} value={option.value}>
                    {option.label}
                  </Select.Option>
                ))}
              </Select>
              <RangePicker 
                value={timeRange}
                onChange={(dates) => dates && setTimeRange(dates as [dayjs.Dayjs, dayjs.Dayjs])}
                showTime
              />
            </Space>
          }>
            <Line {...responseTimeConfig} height={300} />
          </Card>
        </Col>
        
        <Col span={12}>
          <Card title="吞吐量">
            <Column {...throughputConfig} height={300} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={12}>
          <Card title="系统资源使用">
            <div style={{ marginBottom: 16 }}>
              <div style={{ marginBottom: 8 }}>
                <span>CPU使用率</span>
                <span style={{ float: 'right' }}>45%</span>
              </div>
              <Progress percent={45} strokeColor="#1890ff" />
            </div>
            <div style={{ marginBottom: 16 }}>
              <div style={{ marginBottom: 8 }}>
                <span>内存使用率</span>
                <span style={{ float: 'right' }}>62%</span>
              </div>
              <Progress percent={62} strokeColor="#52c41a" />
            </div>
            <div>
              <div style={{ marginBottom: 8 }}>
                <span>磁盘使用率</span>
                <span style={{ float: 'right' }}>78%</span>
              </div>
              <Progress percent={78} strokeColor="#faad14" />
            </div>
          </Card>
        </Col>
        
        <Col span={12}>
          <Card title="查询状态分布">
            <Pie {...errorRateConfig} height={300} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="慢查询分析" extra={
            <Space>
              <Select 
                style={{ width: 200 }} 
                value={selectedDatabase}
                onChange={setSelectedDatabase}
                placeholder="选择数据库"
              >
                {databaseOptions.map(option => (
                  <Select.Option key={option.value} value={option.value}>
                    {option.label}
                  </Select.Option>
                ))}
              </Select>
              <RangePicker 
                value={timeRange}
                onChange={(dates) => dates && setTimeRange(dates as [dayjs.Dayjs, dayjs.Dayjs])}
                showTime
              />
            </Space>
          }>
            <Table
              columns={slowQueryColumns}
              dataSource={slowQueries}
              rowKey="id"
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条慢查询`,
              }}
              scroll={{ x: 1000 }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Monitor