import React from 'react'
import { Row, Col, Card, Statistic, Progress, List, Tag } from 'antd'
import {
  DatabaseOutlined,
  CodeOutlined,
  MonitorOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  SettingOutlined,
} from '@ant-design/icons'

const Home: React.FC = () => {
  const databaseStats = [
    { name: 'MySQL', count: 5, status: 'healthy' },
    { name: 'PostgreSQL', count: 3, status: 'healthy' },
    { name: 'Redis', count: 2, status: 'warning' },
    { name: 'Elasticsearch', count: 1, status: 'healthy' },
  ]

  const recentQueries = [
    { sql: 'SELECT * FROM users LIMIT 100', time: '2024-11-25 14:30:22', duration: '125ms', status: 'success' },
    { sql: 'UPDATE user_settings SET theme = "dark"', time: '2024-11-25 14:28:15', duration: '45ms', status: 'success' },
    { sql: 'SELECT COUNT(*) FROM orders WHERE status = "pending"', time: '2024-11-25 14:25:33', duration: '2.3s', status: 'warning' },
    { sql: 'DELETE FROM logs WHERE created_at < "2024-11-01"', time: '2024-11-25 14:20:11', duration: '890ms', status: 'success' },
  ]

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'healthy': return 'green'
      case 'warning': return 'orange'
      case 'error': return 'red'
      default: return 'default'
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'healthy': return '健康'
      case 'warning': return '警告'
      case 'error': return '错误'
      default: return '未知'
    }
  }

  return (
    <div>
      <h1>仪表板</h1>
      
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="数据库连接"
              value={11}
              prefix={<DatabaseOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="今日查询"
              value={1247}
              prefix={<CodeOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="平均响应时间"
              value={280}
              prefix={<ClockCircleOutlined />}
              suffix="ms"
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="系统状态"
              value={98.5}
              suffix="%"
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="数据库状态" extra={<span>实时监控</span>}>
            <List
              itemLayout="horizontal"
              dataSource={databaseStats}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={<DatabaseOutlined style={{ fontSize: 24, color: '#1890ff' }} />}
                    title={item.name}
                    description={
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span>{item.count} 个连接</span>
                        <Tag color={getStatusColor(item.status)}>
                          {getStatusText(item.status)}
                        </Tag>
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="最近查询" extra={<span>最近活动</span>}>
            <List
              itemLayout="horizontal"
              dataSource={recentQueries}
              renderItem={(item, index) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <code style={{ fontSize: '12px', background: '#f0f0f0', padding: '2px 4px', borderRadius: '2px' }}>
                          {item.sql}
                        </code>
                        <Tag color={item.status === 'success' ? 'green' : 'orange'}>
                          {item.duration}
                        </Tag>
                      </div>
                    }
                    description={
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                        <span>{item.time}</span>
                        {item.status === 'success' ? (
                          <CheckCircleOutlined style={{ color: '#52c41a' }} />
                        ) : (
                          <ExclamationCircleOutlined style={{ color: '#faad14' }} />
                        )}
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="系统性能">
            <div style={{ marginBottom: 16 }}>
              <div style={{ marginBottom: 8 }}>
                <span>CPU 使用率</span>
                <span style={{ float: 'right' }}>35%</span>
              </div>
              <Progress percent={35} strokeColor="#52c41a" />
            </div>
            <div style={{ marginBottom: 16 }}>
              <div style={{ marginBottom: 8 }}>
                <span>内存使用率</span>
                <span style={{ float: 'right' }}>62%</span>
              </div>
              <Progress percent={62} strokeColor="#1890ff" />
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

        <Col xs={24} lg={12}>
          <Card title="快捷操作">
            <Row gutter={[8, 8]}>
              <Col span={12}>
                <Card size="small" hoverable style={{ textAlign: 'center', cursor: 'pointer' }}>
                  <DatabaseOutlined style={{ fontSize: 24, color: '#1890ff' }} />
                  <div style={{ marginTop: 8 }}>新增数据库</div>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" hoverable style={{ textAlign: 'center', cursor: 'pointer' }}>
                  <CodeOutlined style={{ fontSize: 24, color: '#52c41a' }} />
                  <div style={{ marginTop: 8 }}>SQL编辑</div>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" hoverable style={{ textAlign: 'center', cursor: 'pointer' }}>
                  <MonitorOutlined style={{ fontSize: 24, color: '#faad14' }} />
                  <div style={{ marginTop: 8 }}>性能监控</div>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" hoverable style={{ textAlign: 'center', cursor: 'pointer' }}>
                  <SettingOutlined style={{ fontSize: 24, color: '#722ed1' }} />
                  <div style={{ marginTop: 8 }}>系统设置</div>
                </Card>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Home