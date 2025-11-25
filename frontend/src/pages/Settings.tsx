import React, { useState } from 'react'
import { Card, Form, Input, Switch, Select, Button, Space, Divider, Tabs, message, Row, Col } from 'antd'
import { UserOutlined, SecurityScanOutlined, DatabaseOutlined, BellOutlined, GlobalOutlined } from '@ant-design/icons'

const Settings: React.FC = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  const languageOptions = [
    { value: 'zh-CN', label: '简体中文' },
    { value: 'en-US', label: 'English' },
    { value: 'ja-JP', label: '日本語' },
  ]

  const themeOptions = [
    { value: 'light', label: '浅色主题' },
    { value: 'dark', label: '深色主题' },
    { value: 'auto', label: '跟随系统' },
  ]

  const handleSave = async (values: any) => {
    setLoading(true)
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      message.success('设置保存成功')
    } catch (error) {
      message.error('设置保存失败')
    } finally {
      setLoading(false)
    }
  }

  const tabItems = [
    {
      key: 'profile',
      label: '个人信息',
      children: (
        <Card>
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSave}
            initialValues={{
              username: 'admin',
              email: 'admin@anydb.com',
              phone: '13800138000',
              department: '技术部',
            }}
          >
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="username"
                  label="用户名"
                  rules={[{ required: true, message: '请输入用户名' }]}
                >
                  <Input prefix={<UserOutlined />} placeholder="请输入用户名" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="email"
                  label="邮箱"
                  rules={[
                    { required: true, message: '请输入邮箱' },
                    { type: 'email', message: '请输入正确的邮箱格式' }
                  ]}
                >
                  <Input placeholder="请输入邮箱" />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="phone"
                  label="手机号"
                >
                  <Input placeholder="请输入手机号" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="department"
                  label="部门"
                >
                  <Input placeholder="请输入部门" />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              name="password"
              label="新密码"
              extra="留空则不修改密码"
            >
              <Input.Password placeholder="请输入新密码" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                保存设置
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
      icon: <UserOutlined />
    },
    {
      key: 'system',
      label: '系统设置',
      children: (
        <Card>
          <Form
            layout="vertical"
            onFinish={handleSave}
            initialValues={{
              language: 'zh-CN',
              theme: 'light',
              autoSave: true,
              enableNotifications: true,
              enableAuditLog: true,
            }}
          >
            <h4>界面设置</h4>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="language"
                  label="语言"
                >
                  <Select options={languageOptions} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="theme"
                  label="主题"
                >
                  <Select options={themeOptions} />
                </Form.Item>
              </Col>
            </Row>

            <Divider />

            <h4>功能设置</h4>
            <Form.Item
              name="autoSave"
              label="自动保存SQL编辑"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="enableNotifications"
              label="启用系统通知"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="enableAuditLog"
              label="启用审计日志"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                保存设置
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
      icon: <DatabaseOutlined />
    },
    {
      key: 'security',
      label: '安全设置',
      children: (
        <Card>
          <Form
            layout="vertical"
            onFinish={handleSave}
          >
            <h4>登录安全</h4>
            <Form.Item
              name="twoFactor"
              label="双因素认证"
              valuePropName="checked"
              initialValue={false}
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="sessionTimeout"
              label="会话超时时间(分钟)"
              initialValue={30}
            >
              <Select>
                <Select.Option value={15}>15分钟</Select.Option>
                <Select.Option value={30}>30分钟</Select.Option>
                <Select.Option value={60}>1小时</Select.Option>
                <Select.Option value={120}>2小时</Select.Option>
              </Select>
            </Form.Item>

            <Divider />

            <h4>数据库安全</h4>
            <Form.Item
              name="encryptPasswords"
              label="加密数据库密码"
              valuePropName="checked"
              initialValue={true}
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="requireSSL"
              label="强制SSL连接"
              valuePropName="checked"
              initialValue={false}
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="ipWhitelist"
              label="IP白名单"
            >
              <Input.TextArea 
                placeholder="每行一个IP地址，支持CIDR格式，如：192.168.1.0/24"
                rows={4}
              />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                保存设置
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
      icon: <SecurityScanOutlined />
    },
    {
      key: 'notifications',
      label: '通知设置',
      children: (
        <Card>
          <Form
            layout="vertical"
            onFinish={handleSave}
          >
            <h4>邮件通知</h4>
            <Form.Item
              name="emailQueryResults"
              label="查询结果超过1000行时发送邮件"
              valuePropName="checked"
              initialValue={false}
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="emailErrors"
              label="系统错误时发送邮件"
              valuePropName="checked"
              initialValue={true}
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="emailDailyReport"
              label="每日使用报告"
              valuePropName="checked"
              initialValue={true}
            >
              <Switch />
            </Form.Item>

            <Divider />

            <h4>系统通知</h4>
            <Form.Item
              name="browserNotifications"
              label="浏览器通知"
              valuePropName="checked"
              initialValue={true}
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name="soundNotifications"
              label="声音提醒"
              valuePropName="checked"
              initialValue={false}
            >
              <Switch />
            </Form.Item>

            <Divider />

            <h4>通知时间</h4>
            <Form.Item
              name="quietHoursStart"
              label="免打扰开始时间"
              initialValue="22:00"
            >
              <Input type="time" />
            </Form.Item>

            <Form.Item
              name="quietHoursEnd"
              label="免打扰结束时间"
              initialValue="08:00"
            >
              <Input type="time" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>
                保存设置
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
      icon: <BellOutlined />
    },
  ]

  return (
    <div>
      <h2>系统设置</h2>
      
      <Tabs
        defaultActiveKey="profile"
        items={tabItems}
        tabPosition="left"
        style={{ marginTop: 16 }}
      />
    </div>
  )
}

export default Settings