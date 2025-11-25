import React, { useState } from 'react'
import { Card, Table, Button, Space, Modal, Form, Input, Select, InputNumber, Switch, message, Tag, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, DatabaseOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'

interface DatabaseConfig {
  id: number
  name: string
  type: string
  host: string
  port: number
  database?: string
  username: string
  remark?: string
  enabled: boolean
  status?: 'connected' | 'disconnected' | 'error'
}

const DatabaseManager: React.FC = () => {
  const [databases, setDatabases] = useState<DatabaseConfig[]>([
    {
      id: 1,
      name: '生产MySQL',
      type: 'mysql',
      host: '192.168.1.100',
      port: 3306,
      database: 'production_db',
      username: 'root',
      remark: '生产环境主要数据库',
      enabled: true,
      status: 'connected'
    },
    {
      id: 2,
      name: '测试PostgreSQL',
      type: 'postgresql',
      host: '192.168.1.101',
      port: 5432,
      database: 'test_db',
      username: 'admin',
      remark: '测试环境数据库',
      enabled: true,
      status: 'connected'
    },
    {
      id: 3,
      name: 'Redis缓存',
      type: 'redis',
      host: '192.168.1.102',
      port: 6379,
      username: 'default',
      remark: 'Redis缓存服务器',
      enabled: true,
      status: 'connected'
    }
  ])
  
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRecord, setEditingRecord] = useState<DatabaseConfig | null>(null)
  const [form] = Form.useForm()

  const databaseTypes = [
    { value: 'mysql', label: 'MySQL' },
    { value: 'postgresql', label: 'PostgreSQL' },
    { value: 'oracle', label: 'Oracle' },
    { value: 'mariadb', label: 'MariaDB' },
    { value: 'sqlserver', label: 'SQL Server' },
    { value: 'redis', label: 'Redis' },
    { value: 'elasticsearch', label: 'Elasticsearch' },
    { value: 'mongodb', label: 'MongoDB' },
    { value: 'tidb', label: 'TiDB' },
    { value: 'oceanbase', label: 'OceanBase' },
    { value: 'db2', label: 'DB2' },
    { value: 'clickhouse', label: 'ClickHouse' },
  ]

  const columns: ColumnsType<DatabaseConfig> = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <Space>
          <DatabaseOutlined style={{ color: '#1890ff' }} />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type) => {
        const typeInfo = databaseTypes.find(t => t.value === type)
        return <Tag color="blue">{typeInfo?.label || type}</Tag>
      },
    },
    {
      title: '连接信息',
      key: 'connection',
      render: (_, record) => (
        <span>
          {record.host}:{record.port}
          {record.database && `/${record.database}`}
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const statusMap = {
          connected: { color: 'green', text: '已连接' },
          disconnected: { color: 'red', text: '未连接' },
          error: { color: 'orange', text: '连接错误' }
        }
        const statusInfo = statusMap[status as keyof typeof statusMap] || statusMap.disconnected
        return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
      },
    },
    {
      title: '启用',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled) => (
        <Switch checked={enabled} disabled />
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button 
            type="primary" 
            size="small" 
            icon={<EyeOutlined />}
            onClick={() => handleTestConnection(record.id)}
          >
            测试
          </Button>
          <Button 
            size="small" 
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个数据库配置吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button 
              danger 
              size="small" 
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  const handleAdd = () => {
    setEditingRecord(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: DatabaseConfig) => {
    setEditingRecord(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = (id: number) => {
    setDatabases(databases.filter(item => item.id !== id))
    message.success('删除成功')
  }

  const handleTestConnection = (id: number) => {
    const database = databases.find(item => item.id === id)
    if (database) {
      message.success(`正在测试 ${database.name} 的连接...`)
      // 模拟API调用
      setTimeout(() => {
        const success = Math.random() > 0.2 // 80%成功率
        if (success) {
          message.success('连接测试成功')
          setDatabases(databases.map(item => 
            item.id === id ? { ...item, status: 'connected' as const } : item
          ))
        } else {
          message.error('连接测试失败')
          setDatabases(databases.map(item => 
            item.id === id ? { ...item, status: 'error' as const } : item
          ))
        }
      }, 2000)
    }
  }

  const handleSubmit = async (values: any) => {
    try {
      if (editingRecord) {
        // 编辑模式
        setDatabases(databases.map(item => 
          item.id === editingRecord.id 
            ? { ...values, id: editingRecord.id }
            : item
        ))
        message.success('更新成功')
      } else {
        // 新增模式
        const newRecord = {
          ...values,
          id: Math.max(...databases.map(d => d.id)) + 1,
          status: 'disconnected' as const
        }
        setDatabases([...databases, newRecord])
        message.success('添加成功')
      }
      setModalVisible(false)
    } catch (error) {
      message.error('操作失败')
    }
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>数据库管理</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加数据库
        </Button>
      </div>

      <Card>
        <Table
          columns={columns}
          dataSource={databases}
          rowKey="id"
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      <Modal
        title={editingRecord ? '编辑数据库配置' : '添加数据库配置'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="name"
            label="连接名称"
            rules={[{ required: true, message: '请输入连接名称' }]}
          >
            <Input placeholder="请输入连接名称" />
          </Form.Item>

          <Form.Item
            name="type"
            label="数据库类型"
            rules={[{ required: true, message: '请选择数据库类型' }]}
          >
            <Select placeholder="请选择数据库类型">
              {databaseTypes.map(type => (
                <Select.Option key={type.value} value={type.value}>
                  {type.label}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="host"
            label="主机地址"
            rules={[{ required: true, message: '请输入主机地址' }]}
          >
            <Input placeholder="请输入主机地址" />
          </Form.Item>

          <Form.Item
            name="port"
            label="端口号"
            rules={[{ required: true, message: '请输入端口号' }]}
          >
            <InputNumber placeholder="请输入端口号" style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="database"
            label="数据库名"
          >
            <Input placeholder="请输入数据库名（可选）" />
          </Form.Item>

          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" />
          </Form.Item>

          <Form.Item
            name="remark"
            label="备注"
          >
            <Input.TextArea placeholder="请输入备注信息（可选）" rows={3} />
          </Form.Item>

          <Form.Item
            name="enabled"
            label="启用"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default DatabaseManager