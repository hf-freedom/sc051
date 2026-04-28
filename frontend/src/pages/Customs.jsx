import React, { useState, useEffect } from 'react'
import {
  Table, Button, Tag, Modal, message, Space, Descriptions, Card, Row, Col, Statistic, Divider
} from 'antd'
import {
  ReloadOutlined, EyeOutlined, FileSearchOutlined, CheckCircleOutlined,
  SyncOutlined, ExclamationCircleOutlined
} from '@ant-design/icons'
import { customsApi, orderApi, userApi } from '../api'

const Customs = () => {
  const [customs, setCustoms] = useState([])
  const [orders, setOrders] = useState([])
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedCustoms, setSelectedCustoms] = useState(null)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [customsResult, ordersResult, usersResult] = await Promise.all([
        customsApi.getAll(),
        orderApi.getAll(),
        userApi.getAll()
      ])
      setCustoms(customsResult.data || [])
      setOrders(ordersResult.data || [])
      setUsers(usersResult.data || [])
    } catch (error) {
      message.error('加载数据失败: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status) => {
    const colors = {
      'PENDING': 'orange',
      'SUBMITTED': 'blue',
      'PROCESSING': 'cyan',
      'SUCCESS': 'green',
      'FAILED': 'red'
    }
    return colors[status] || 'default'
  }

  const getStatusDesc = (status) => {
    const descs = {
      'PENDING': '待提交',
      'SUBMITTED': '已提交',
      'PROCESSING': '处理中',
      'SUCCESS': '申报成功',
      'FAILED': '申报失败'
    }
    return descs[status] || status
  }

  const handleViewDetail = (record) => {
    setSelectedCustoms(record)
    setDetailModalVisible(true)
  }

  const handleCreate = async (orderId) => {
    try {
      await customsApi.create(orderId)
      message.success('报关单创建成功')
      loadData()
    } catch (error) {
      message.error('创建报关单失败: ' + error.message)
    }
  }

  const handleSubmit = async (declarationId) => {
    try {
      await customsApi.submit(declarationId)
      message.success('报关单提交成功')
      loadData()
    } catch (error) {
      message.error('提交报关单失败: ' + error.message)
    }
  }

  const handleProcess = async (declarationId) => {
    try {
      const result = await customsApi.process(declarationId)
      if (result.code === 200) {
        message.success('报关成功')
      } else {
        message.error('报关失败: ' + result.message)
      }
      loadData()
    } catch (error) {
      message.error('报关处理失败: ' + error.message)
    }
  }

  const handleRetry = async (declarationId) => {
    try {
      await customsApi.retry(declarationId)
      message.success('报关单重试成功')
      loadData()
    } catch (error) {
      message.error('重试失败: ' + error.message)
    }
  }

  const pendingCustomsOrders = orders.filter(o =>
    o.status === 'PENDING_CUSTOMS' && !customs.find(c => c.orderId === o.id)
  )

  const columns = [
    {
      title: '报关单号',
      dataIndex: 'declarationNo',
      key: 'declarationNo',
      width: 200
    },
    {
      title: '订单号',
      dataIndex: 'orderId',
      key: 'orderId',
      render: (val) => {
        const order = orders.find(o => o.id === val)
        return order ? order.orderNo : val
      }
    },
    {
      title: '申报人',
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
      title: '申报金额(¥)',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '税费(¥)',
      dataIndex: 'taxAmount',
      key: 'taxAmount',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (val) => (
        <Tag color={getStatusColor(val)}>
          {getStatusDesc(val)}
        </Tag>
      )
    },
    {
      title: '重试次数',
      dataIndex: 'retryCount',
      key: 'retryCount'
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space wrap>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            详情
          </Button>
          {record.status === 'PENDING' && (
            <Button type="link" onClick={() => handleSubmit(record.id)}>
              提交报关
            </Button>
          )}
          {record.status === 'SUBMITTED' && (
            <Button type="link" onClick={() => handleProcess(record.id)}>
              处理报关
            </Button>
          )}
          {record.status === 'FAILED' && record.retryCount < 3 && (
            <Button type="link" icon={<SyncOutlined />} onClick={() => handleRetry(record.id)}>
              重试报关
            </Button>
          )}
        </Space>
      )
    }
  ]

  const stats = [
    { title: '总报关单', value: customs.length, icon: <FileSearchOutlined />, color: '#1890ff' },
    { title: '待提交', value: customs.filter(c => c.status === 'PENDING').length, icon: <ExclamationCircleOutlined />, color: '#faad14' },
    { title: '报关成功', value: customs.filter(c => c.status === 'SUCCESS').length, icon: <CheckCircleOutlined />, color: '#52c41a' },
    { title: '报关失败', value: customs.filter(c => c.status === 'FAILED').length, icon: <ExclamationCircleOutlined />, color: '#ff4d4f' }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>报关管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
        </Space>
      </div>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        {stats.map((stat, index) => (
          <Col span={6} key={index}>
            <Card className="stat-card">
              <Statistic
                title={stat.title}
                value={stat.value}
                prefix={stat.icon}
                valueStyle={{ color: stat.color }}
              />
            </Card>
          </Col>
        ))}
      </Row>

      {pendingCustomsOrders.length > 0 && (
        <Card title="待报关订单（可创建报关单）" style={{ marginBottom: 16 }}>
          <Table
            dataSource={pendingCustomsOrders}
            rowKey="id"
            pagination={false}
            columns={[
              { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
              {
                title: '用户',
                dataIndex: 'userId',
                key: 'userId',
                render: (val) => users.find(u => u.id === val)?.realName || '-'
              },
              { title: '订单金额', dataIndex: 'actualAmount', key: 'actualAmount', render: v => `¥${v?.toFixed(2)}` },
              {
                title: '状态',
                dataIndex: 'status',
                key: 'status',
                render: (val) => (
                  <Tag color="blue">待报关</Tag>
                )
              },
              {
                title: '操作',
                key: 'action',
                render: (_, record) => (
                  <Button type="primary" size="small" onClick={() => handleCreate(record.id)}>
                    创建报关单
                  </Button>
                )
              }
            ]}
          />
        </Card>
      )}

      <Table
        columns={columns}
        dataSource={customs}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1300 }}
      />

      <Modal
        title="报关单详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedCustoms && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="报关单号">{selectedCustoms.declarationNo}</Descriptions.Item>
              <Descriptions.Item label="报关状态">
                <Tag color={getStatusColor(selectedCustoms.status)}>
                  {getStatusDesc(selectedCustoms.status)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="申报人">{selectedCustoms.realName}</Descriptions.Item>
              <Descriptions.Item label="身份证号">
                {selectedCustoms.idCardNumber?.replace(/(.{6}).*(.{4})/, '$1********$2')}
              </Descriptions.Item>
              <Descriptions.Item label="申报金额">¥{selectedCustoms.totalAmount?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="税费">¥{selectedCustoms.taxAmount?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="重试次数">{selectedCustoms.retryCount || 0}</Descriptions.Item>
              <Descriptions.Item label="提交时间">{selectedCustoms.submitTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间" span={2}>
                {selectedCustoms.createTime}
              </Descriptions.Item>
            </Descriptions>

            {selectedCustoms.failureReason && (
              <div style={{ marginTop: 16 }}>
                <Divider>失败原因</Divider>
                <Card type="inner" style={{ backgroundColor: '#fff2f0' }}>
                  {selectedCustoms.failureReason}
                </Card>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Customs
