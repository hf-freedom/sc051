import React, { useState, useEffect } from 'react'
import { Table, Button, Tag, Modal, Form, Input, InputNumber, message, Space, Popconfirm, Card, Statistic, Row, Col, Progress } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, UserOutlined } from '@ant-design/icons'
import { userApi } from '../api'

const Users = () => {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [quotaModalVisible, setQuotaModalVisible] = useState(false)
  const [selectedUser, setSelectedUser] = useState(null)
  const [quotaInfo, setQuotaInfo] = useState(null)
  const [editingUser, setEditingUser] = useState(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      setLoading(true)
      const result = await userApi.getAll()
      setUsers(result.data || [])
    } catch (error) {
      message.error('加载用户列表失败: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingUser(null)
    form.resetFields()
    form.setFieldsValue({
      annualQuota: 26000,
      usedQuota: 0,
      isVerified: false
    })
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingUser(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleVerify = async (id) => {
    try {
      await userApi.verify(id)
      message.success('实名认证成功')
      loadUsers()
    } catch (error) {
      message.error('实名认证失败: ' + error.message)
    }
  }

  const handleViewQuota = async (record) => {
    setSelectedUser(record)
    try {
      const result = await userApi.getQuota(record.id)
      setQuotaInfo(result.data)
      setQuotaModalVisible(true)
    } catch (error) {
      message.error('获取额度信息失败: ' + error.message)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      if (editingUser) {
        await userApi.update(editingUser.id, values)
        message.success('更新成功')
      } else {
        await userApi.create(values)
        message.success('创建成功')
      }
      setModalVisible(false)
      loadUsers()
    } catch (error) {
      message.error('操作失败: ' + error.message)
    }
  }

  const columns = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username'
    },
    {
      title: '真实姓名',
      dataIndex: 'realName',
      key: 'realName'
    },
    {
      title: '身份证号',
      dataIndex: 'idCardNumber',
      key: 'idCardNumber',
      render: (val) => val ? val.replace(/(.{6}).*(.{4})/, '$1********$2') : '-'
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone'
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email'
    },
    {
      title: '年度额度',
      dataIndex: 'annualQuota',
      key: 'annualQuota',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '已使用额度',
      dataIndex: 'usedQuota',
      key: 'usedQuota',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '实名认证',
      dataIndex: 'isVerified',
      key: 'isVerified',
      render: (val) => (
        <Tag color={val ? 'green' : 'orange'}>
          {val ? '已认证' : '未认证'}
        </Tag>
      )
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => handleViewQuota(record)}>
            查看额度
          </Button>
          {!record.isVerified && (
            <Button type="link" onClick={() => handleVerify(record.id)}>
              实名认证
            </Button>
          )}
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
        </Space>
      )
    }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>用户管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadUsers}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加用户
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={editingUser ? '编辑用户' : '添加用户'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item
            name="realName"
            label="真实姓名"
            rules={[{ required: true, message: '请输入真实姓名' }]}
          >
            <Input placeholder="请输入真实姓名" />
          </Form.Item>
          <Form.Item
            name="idCardNumber"
            label="身份证号"
            rules={[{ required: true, message: '请输入身份证号' }]}
          >
            <Input placeholder="请输入身份证号" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="手机号"
          >
            <Input placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item
            name="email"
            label="邮箱"
          >
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item
            name="annualQuota"
            label="年度额度(¥)"
          >
            <InputNumber min={0} style={{ width: '100%' }} placeholder="年度额度" />
          </Form.Item>
          <Form.Item
            name="isVerified"
            label="是否已实名认证"
          >
            <Input.Group compact>
              <Form.Item name="isVerified" noStyle>
                <Input placeholder="是否已实名认证" />
              </Form.Item>
            </Input.Group>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="用户额度详情"
        open={quotaModalVisible}
        onCancel={() => setQuotaModalVisible(false)}
        footer={null}
        width={500}
      >
        {quotaInfo && selectedUser && (
          <div>
            <Card style={{ marginBottom: 16 }}>
              <Space>
                <UserOutlined style={{ fontSize: 48, color: '#1890ff' }} />
                <div>
                  <div style={{ fontSize: 18, fontWeight: 'bold' }}>{selectedUser.realName}</div>
                  <div style={{ color: '#666' }}>{selectedUser.username}</div>
                </div>
              </Space>
            </Card>
            <Row gutter={16}>
              <Col span={12}>
                <Card>
                  <Statistic
                    title="年度总额度"
                    value={quotaInfo.annualQuota}
                    precision={2}
                    prefix="¥"
                    valueStyle={{ color: '#3f8600' }}
                  />
                </Card>
              </Col>
              <Col span={12}>
                <Card>
                  <Statistic
                    title="已使用额度"
                    value={quotaInfo.usedQuota}
                    precision={2}
                    prefix="¥"
                    valueStyle={{ color: '#1890ff' }}
                  />
                </Card>
              </Col>
            </Row>
            <Card style={{ marginTop: 16 }}>
              <Statistic
                title="剩余额度"
                value={quotaInfo.remainingQuota}
                precision={2}
                prefix="¥"
                valueStyle={{ color: '#52c41a' }}
              />
              <div style={{ marginTop: 16 }}>
                <div style={{ marginBottom: 8 }}>使用进度</div>
                <Progress
                  percent={Math.min(100, Math.round((quotaInfo.usedQuota / quotaInfo.annualQuota) * 100))}
                  status={quotaInfo.usedQuota / quotaInfo.annualQuota >= 0.9 ? 'exception' : 'active'}
                />
              </div>
            </Card>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Users
