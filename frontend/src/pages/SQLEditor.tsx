import React, { useState } from 'react'
import { Card, Select, Button, Space, Typography, Row, Col, Divider, Spin, message } from 'antd'
import { PlayCircleOutlined, DownloadOutlined, SaveOutlined, ClearOutlined } from '@ant-design/icons'
import Editor from '@monaco-editor/react'
import { format as formatSQL } from 'sql-formatter'

const { Title, Text } = Typography

interface QueryResult {
  columns: string[]
  rows: any[][]
  total: number
  executionTime: number
}

const SQLEditor: React.FC = () => {
  const [sql, setSQL] = useState('SELECT * FROM users LIMIT 10;')
  const [selectedDatabase, setSelectedDatabase] = useState<string>('mysql-1')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<QueryResult | null>(null)

  const databaseOptions = [
    { value: 'mysql-1', label: 'MySQL - 生产环境' },
    { value: 'postgresql-1', label: 'PostgreSQL - 测试环境' },
    { value: 'redis-1', label: 'Redis - 缓存' },
  ]

  const sqlTemplates = [
    {
      category: '基础查询',
      templates: [
        'SELECT * FROM table_name LIMIT 10;',
        'SELECT column1, column2 FROM table_name WHERE condition;',
        'SELECT COUNT(*) FROM table_name;',
        'SELECT * FROM table_name ORDER BY column_name DESC LIMIT 5;',
      ]
    },
    {
      category: '数据操作',
      templates: [
        'INSERT INTO table_name (col1, col2) VALUES (value1, value2);',
        'UPDATE table_name SET column1 = value1 WHERE condition;',
        'DELETE FROM table_name WHERE condition;',
      ]
    },
    {
      category: '统计分析',
      templates: [
        'SELECT column1, COUNT(*) FROM table_name GROUP BY column1;',
        'SELECT column1, AVG(column2) FROM table_name GROUP BY column1;',
        'SELECT * FROM table_name WHERE column1 IN (SELECT column1 FROM other_table);',
      ]
    }
  ]

  const handleExecute = async () => {
    if (!sql.trim()) {
      message.warning('请输入SQL语句')
      return
    }

    setLoading(true)
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 模拟查询结果
      const mockResult: QueryResult = {
        columns: ['id', 'name', 'email', 'created_at'],
        rows: [
          [1, '张三', 'zhangsan@example.com', '2024-11-25 10:30:00'],
          [2, '李四', 'lisi@example.com', '2024-11-25 11:15:00'],
          [3, '王五', 'wangwu@example.com', '2024-11-25 14:20:00'],
          [4, '赵六', 'zhaoliu@example.com', '2024-11-25 15:45:00'],
        ],
        total: 4,
        executionTime: 156
      }
      
      setResult(mockResult)
      message.success('查询执行成功')
    } catch (error) {
      message.error('查询执行失败')
    } finally {
      setLoading(false)
    }
  }

  const handleFormat = () => {
    try {
      const formatted = formatSQL(sql, { 
        language: selectedDatabase.includes('mysql') ? 'mysql' : 'postgresql',
        tabWidth: 2,
        keywordCase: 'upper',
      })
      setSQL(formatted)
    } catch (error) {
      message.error('SQL格式化失败')
    }
  }

  const handleClear = () => {
    setSQL('')
    setResult(null)
  }

  const handleTemplateClick = (template: string) => {
    setSQL(template)
  }

  return (
    <div>
      <Title level={2}>
        <PlayCircleOutlined style={{ marginRight: 8 }} />
        SQL编辑器
      </Title>
      
      <Row gutter={[16, 16]}>
        <Col span={18}>
          <Card title="SQL编辑器" extra={
            <Space>
              <Button onClick={handleFormat} icon={<DownloadOutlined />}>
                格式化
              </Button>
              <Button onClick={handleClear} icon={<ClearOutlined />}>
                清空
              </Button>
              <Button 
                type="primary" 
                onClick={handleExecute} 
                loading={loading}
                icon={<PlayCircleOutlined />}
              >
                执行
              </Button>
            </Space>
          }>
            <div style={{ marginBottom: 16 }}>
              <Select
                style={{ width: 200, marginRight: 8 }}
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
            </div>
            
            <Editor
              height="400px"
              language="sql"
              value={sql}
              onChange={(value) => setSQL(value || '')}
              theme="vs-light"
              options={{
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                fontSize: 14,
                lineNumbers: 'on',
                wordWrap: 'on',
                automaticLayout: true,
                tabSize: 2,
                insertSpaces: true,
                folding: true,
                lineDecorationsWidth: 0,
                lineNumbersMinChars: 3,
                renderWhitespace: 'none',
                suggest: {
                  showKeywords: true,
                  showSnippets: true,
                }
              }}
            />
          </Card>

          {result && (
            <Card 
              title={`查询结果 (${result.total} 行记录, ${result.executionTime}ms)`}
              style={{ marginTop: 16 }}
            >
              <div style={{ overflow: 'auto', maxHeight: 400 }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr>
                      {result.columns.map((column, index) => (
                        <th
                          key={index}
                          style={{
                            border: '1px solid #d9d9d9',
                            padding: '8px',
                            backgroundColor: '#fafafa',
                            fontWeight: 'bold',
                            textAlign: 'left',
                          }}
                        >
                          {column}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {result.rows.map((row, rowIndex) => (
                      <tr key={rowIndex}>
                        {row.map((cell, cellIndex) => (
                          <td
                            key={cellIndex}
                            style={{
                              border: '1px solid #d9d9d9',
                              padding: '8px',
                              fontFamily: 'monospace',
                              fontSize: '13px',
                            }}
                          >
                            {cell === null ? 'NULL' : String(cell)}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}

          {loading && (
            <Card style={{ marginTop: 16 }}>
              <div style={{ textAlign: 'center', padding: '20px' }}>
                <Spin size="large" />
                <div style={{ marginTop: 16 }}>正在执行SQL查询...</div>
              </div>
            </Card>
          )}
        </Col>

        <Col span={6}>
          <Card title="SQL模板">
            {sqlTemplates.map((category, categoryIndex) => (
              <div key={categoryIndex} style={{ marginBottom: 16 }}>
                <Divider orientation="left" orientationMargin="0">
                  <Text style={{ fontSize: '14px', color: '#666' }}>
                    {category.category}
                  </Text>
                </Divider>
                <div>
                  {category.templates.map((template, templateIndex) => (
                    <Button
                      key={templateIndex}
                      block
                      style={{ 
                        textAlign: 'left', 
                        marginBottom: '8px',
                        fontSize: '12px',
                        height: 'auto',
                        whiteSpace: 'normal',
                        wordBreak: 'break-word',
                      }}
                      onClick={() => handleTemplateClick(template)}
                    >
                      <code style={{ fontSize: '11px' }}>{template}</code>
                    </Button>
                  ))}
                </div>
              </div>
            ))}
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default SQLEditor