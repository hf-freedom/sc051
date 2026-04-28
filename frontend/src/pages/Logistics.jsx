import React, { useState, useEffect } from 'react'
import {
  Table, Button, Tag, Modal, Form, Input, Select, message, Space,
  Descriptions, Card, Row, Col, Statistic, Divider, Timeline, Steps
} from 'antd'
import {
  ReloadOutlined, EyeOutlined, TruckOutlined, PlusOutlined,
  CheckCircleOutlined, ShopOutlined, ExportOutlined
} from '@ant-design/icons'
import { logisticsApi, orderApi, customsApi } from '../api'

const { Option } = Select
const { TextArea } = Input
const { Step } = Steps

const Logistics = () => {
  const [logisticsList, setLogisticsList] = useState([])
  const [orders, setOrders] = useState([])
  const [customs, setCustoms] = useState([])
  const [loading, setLoading] = useState(false)
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [updateStatusModalVisible, setUpdateStatusModalVisible] = useState(false)
  const [selectedLogistics, setSelectedLogistics] = useState(null)
  const [trackingList, setTrackingList] = useState([])
  const [form] = Form.useForm()
  const [statusForm] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [logisticsResult, ordersResult, customsResult] = await Promise.all([
        logisticsApi.getAll(),
        orderApi.getAll(),
        customsApi.getAll()
      ])
      setLogisticsList(logisticsResult.data || [])
      setOrders(ordersResult.data || [])
      setCustoms(customsResult.data || [])
    } catch (error) {
      message.error('加载数据失败: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status) => {
    const colors = {
      'PENDING': 'orange',
      'SHIPPED': 'blue',
      'IN_TRANSIT': 'cyan',
      'CUSTOMS_CLEARANCE': 'purple',
      'LAST_MILE': 'geekblue',
      'DELIVERED': 'green'
    }
    return colors[status] || 'default'
  }

  const getStatusDesc = (status) => {
    const descs = {
      'PENDING': '待发货',
      'SHIPPED': '已发货',
      'IN_TRANSIT': '运输中',
      'CUSTOMS_CLEARANCE': '清关中',
      'LAST_MILE': '末端派送',
      'DELIVERED': '已签收'
    }
    return descs[status] || status
  }

  const getStatusStepIndex = (status) => {
    const steps = ['PENDING', 'SHIPPED', 'IN_TRANSIT', 'CUSTOMS_CLEARANCE', 'LAST_MILE', 'DELIVERED']
    return steps.indexOf(status)
  }

  const isOrderRefunded = (orderId) => {
    const order = orders.find(o => o.id === orderId)
    if (!order) return false
    return ['REFUNDING', 'REFUNDED', 'CANCELLED'].includes(order.status)
  }

  const handleViewDetail = async (record) => {
    setSelectedLogistics(record)
    try {
      const result = await logisticsApi.getTracking(record.id)
      setTrackingList(result.data || [])
    } catch (error) {
      message.error('获取物流轨迹失败: ' + error.message)
    }
    setDetailModalVisible(true)
  }

  const handleCreate = () => {
    form.resetFields()
    setCreateModalVisible(true)
  }

  const handleShip = async (logisticsId) => {
    try {
      await logisticsApi.ship(logisticsId)
      message.success('发货成功')
      loadData()
    } catch (error) {
      message.error('发货失败: ' + error.message)
    }
  }

  const handleUpdateStatus = (record) => {
    setSelectedLogistics(record)
    statusForm.resetFields()
    setUpdateStatusModalVisible(true)
  }

  const handleCreateSubmit = async () => {
    try {
      const values = await form.validateFields()
      await logisticsApi.create(values)
      message.success('物流单创建成功')
      setCreateModalVisible(false)
      loadData()
    } catch (error) {
      message.error('创建物流单失败: ' + error.message)
    }
  }

  const handleStatusSubmit = async () => {
    try {
      const values = await statusForm.validateFields()
      values.logisticsId = selectedLogistics.id
      await logisticsApi.updateStatus(values)
      message.success('物流状态更新成功')
      setUpdateStatusModalVisible(false)
      loadData()
    } catch (error) {
      message.error('更新状态失败: ' + error.message)
    }
  }

  const availableOrders = orders.filter(o => {
    const hasLogistics = logisticsList.some(l => l.orderId === o.id)
    const isRefunded = ['REFUNDING', 'REFUNDED', 'CANCELLED'].includes(o.status)
    return (o.status === 'CUSTOMS_SUCCESS' || o.status === 'PENDING_SHIPMENT') && !hasLogistics && !isRefunded
  })

  const columns = [
    {
      title: '物流单号',
      dataIndex: 'trackingNo',
      key: 'trackingNo',
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
      title: '承运人',
      dataIndex: 'carrier',
      key: 'carrier'
    },
    {
      title: '收货人',
      dataIndex: 'receiverName',
      key: 'receiverName',
      render: (val, record) => (
        <div>
          <div>{val}</div>
          <div style={{ fontSize: 12, color: '#666' }}>{record.receiverPhone}</div>
        </div>
      )
    },
    {
      title: '收货地址',
      dataIndex: 'toAddress',
      key: 'toAddress',
      ellipsis: true
    },
    {
      title: '重量(kg)',
      dataIndex: 'weight',
      key: 'weight'
    },
    {
      title: '运费(¥)',
      dataIndex: 'shippingFee',
      key: 'shippingFee',
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
      title: '发货时间',
      dataIndex: 'shipTime',
      key: 'shipTime',
      width: 180
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space wrap>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            轨迹
          </Button>
          {record.status === 'PENDING' && !isOrderRefunded(record.orderId) && (
            <Button type="link" icon={<ExportOutlined />} onClick={() => handleShip(record.id)}>
              发货
            </Button>
          )}
          {record.status === 'PENDING' && isOrderRefunded(record.orderId) && (
            <Tag color="red">订单已退款</Tag>
          )}
          {record.status !== 'DELIVERED' && record.status !== 'PENDING' && (
            <Button type="link" onClick={() => handleUpdateStatus(record)}>
              更新状态
            </Button>
          )}
        </Space>
      )
    }
  ]

  const stats = [
    { title: '总物流单', value: logisticsList.length, icon: <TruckOutlined />, color: '#1890ff' },
    { title: '待发货', value: logisticsList.filter(l => l.status === 'PENDING').length, icon: <ShopOutlined />, color: '#faad14' },
    { title: '运输中', value: logisticsList.filter(l => ['SHIPPED', 'IN_TRANSIT', 'CUSTOMS_CLEARANCE', 'LAST_MILE'].includes(l.status)).length, icon: <TruckOutlined />, color: '#1890ff' },
    { title: '已签收', value: logisticsList.filter(l => l.status === 'DELIVERED').length, icon: <CheckCircleOutlined />, color: '#52c41a' }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>物流管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate} disabled={availableOrders.length === 0}>
            创建物流单
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

      <Table
        columns={columns}
        dataSource={logisticsList}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1400 }}
      />

      <Modal
        title="创建物流单"
        open={createModalVisible}
        onOk={handleCreateSubmit}
        onCancel={() => setCreateModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="orderId"
            label="选择订单"
            rules={[{ required: true, message: '请选择订单' }]}
          >
            <Select placeholder="请选择订单" style={{ width: '100%' }}>
              {availableOrders.map(order => (
                <Option key={order.id} value={order.id}>
                  {order.orderNo} - ¥{order.actualAmount?.toFixed(2)}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="fromAddress"
            label="发货地址"
            initialValue="上海市浦东新区保税区1号仓库"
          >
            <TextArea rows={2} placeholder="发货地址" />
          </Form.Item>
          <Form.Item
            name="toAddress"
            label="收货地址"
            rules={[{ required: true, message: '请输入收货地址' }]}
          >
            <TextArea rows={2} placeholder="收货地址" />
          </Form.Item>
          <Form.Item
            name="receiverName"
            label="收货人姓名"
            rules={[{ required: true, message: '请输入收货人姓名' }]}
          >
            <Input placeholder="收货人姓名" />
          </Form.Item>
          <Form.Item
            name="receiverPhone"
            label="收货人电话"
            rules={[{ required: true, message: '请输入收货人电话' }]}
          >
            <Input placeholder="收货人电话" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="更新物流状态"
        open={updateStatusModalVisible}
        onOk={handleStatusSubmit}
        onCancel={() => setUpdateStatusModalVisible(false)}
        width={500}
      >
        <Form form={statusForm} layout="vertical">
          <Form.Item
            name="status"
            label="物流状态"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select placeholder="请选择状态">
              <Option value="IN_TRANSIT">运输中</Option>
              <Option value="CUSTOMS_CLEARANCE">清关中</Option>
              <Option value="LAST_MILE">末端派送</Option>
              <Option value="DELIVERED">已签收</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="location"
            label="当前位置"
            rules={[{ required: true, message: '请输入当前位置' }]}
          >
            <Input placeholder="当前位置" />
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
          >
            <TextArea rows={2} placeholder="物流轨迹描述" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="物流轨迹详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedLogistics && (
          <div>
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="物流单号">{selectedLogistics.trackingNo}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={getStatusColor(selectedLogistics.status)}>
                  {getStatusDesc(selectedLogistics.status)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="承运人">{selectedLogistics.carrier}</Descriptions.Item>
              <Descriptions.Item label="收货人">{selectedLogistics.receiverName} ({selectedLogistics.receiverPhone})</Descriptions.Item>
              <Descriptions.Item label="收货地址" span={2}>
                {selectedLogistics.toAddress}
              </Descriptions.Item>
            </Descriptions>

            <Divider>物流进度</Divider>
            <Steps
              current={getStatusStepIndex(selectedLogistics.status)}
              size="small"
              items={[
                { title: '待发货' },
                { title: '已发货' },
                { title: '运输中' },
                { title: '清关中' },
                { title: '末端派送' },
                { title: '已签收' }
              ]}
            />

            {trackingList.length > 0 && (
              <div>
                <Divider>物流轨迹</Divider>
                <Timeline
                  items={trackingList.map((t, index) => ({
                    color: index === 0 ? 'green' : 'blue',
                    children: (
                      <div>
                        <p style={{ margin: 0 }}>
                          <strong>{getStatusDesc(t.status)}</strong> - {t.location}
                        </p>
                        <p style={{ margin: 0, color: '#666' }}>{t.description}</p>
                        <p style={{ margin: 0, fontSize: 12, color: '#999' }}>{t.eventTime}</p>
                      </div>
                    )
                  }))}
                />
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Logistics
