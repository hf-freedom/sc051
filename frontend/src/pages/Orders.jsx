import React, { useState, useEffect } from 'react'
import {
  Table, Button, Tag, Modal, Form, Input, Select, InputNumber,
  message, Space, Descriptions, Card, Row, Col, Statistic, Divider
} from 'antd'
import {
  PlusOutlined, ReloadOutlined, EyeOutlined, PayCircleOutlined,
  ShoppingCartOutlined, CheckCircleOutlined, CloseCircleOutlined, ExclamationCircleOutlined
} from '@ant-design/icons'
import { orderApi, productApi, userApi, customsApi, refundApi } from '../api'

const { Option } = Select
const { TextArea } = Input

const Orders = () => {
  const [orders, setOrders] = useState([])
  const [products, setProducts] = useState([])
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [refundModalVisible, setRefundModalVisible] = useState(false)
  const [selectedOrder, setSelectedOrder] = useState(null)
  const [form] = Form.useForm()
  const [refundForm] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [ordersResult, productsResult, usersResult] = await Promise.all([
        orderApi.getAll(),
        productApi.getAll(),
        userApi.getAll()
      ])
      setOrders(ordersResult.data || [])
      setProducts(productsResult.data || [])
      setUsers(usersResult.data || [])
    } catch (error) {
      message.error('加载数据失败: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status) => {
    const colors = {
      'PENDING_PAYMENT': 'orange',
      'PENDING_CUSTOMS': 'blue',
      'CUSTOMS_PROCESSING': 'cyan',
      'CUSTOMS_SUCCESS': 'green',
      'CUSTOMS_FAILED': 'red',
      'PENDING_SHIPMENT': 'geekblue',
      'SHIPPED': 'purple',
      'DELIVERED': 'success',
      'CANCELLED': 'default',
      'REFUNDING': 'warning',
      'REFUNDED': 'red'
    }
    return colors[status] || 'default'
  }

  const getStatusDesc = (status) => {
    const descs = {
      'PENDING_PAYMENT': '待支付',
      'PENDING_CUSTOMS': '待报关',
      'CUSTOMS_PROCESSING': '报关中',
      'CUSTOMS_SUCCESS': '报关成功',
      'CUSTOMS_FAILED': '报关失败',
      'PENDING_SHIPMENT': '待发货',
      'SHIPPED': '已发货',
      'DELIVERED': '已签收',
      'CANCELLED': '已取消',
      'REFUNDING': '退款中',
      'REFUNDED': '已退款'
    }
    return descs[status] || status
  }

  const canRefund = (status) => {
    const refundableStatuses = [
      'PENDING_CUSTOMS',
      'CUSTOMS_PROCESSING',
      'CUSTOMS_SUCCESS',
      'PENDING_SHIPMENT',
      'SHIPPED'
    ]
    return refundableStatuses.includes(status)
  }

  const getRefundRule = (status) => {
    if (status === 'PENDING_CUSTOMS' || status === 'CUSTOMS_PROCESSING') {
      return '全额退款'
    }
    if (status === 'CUSTOMS_SUCCESS' || status === 'PENDING_SHIPMENT' || status === 'SHIPPED') {
      return '扣除税费 + 5%服务费'
    }
    return ''
  }

  const handleAdd = () => {
    form.resetFields()
    form.setFieldsValue({
      items: []
    })
    setModalVisible(true)
  }

  const handleViewDetail = (record) => {
    setSelectedOrder(record)
    setDetailModalVisible(true)
  }

  const handlePay = async (id) => {
    try {
      const result = await orderApi.pay(id)
      message.success('支付成功')
      loadData()
    } catch (error) {
      message.error('支付失败: ' + error.message)
    }
  }

  const handleRefund = (record) => {
    setSelectedOrder(record)
    refundForm.resetFields()
    setRefundModalVisible(true)
  }

  const handleSubmitRefund = async () => {
    try {
      const values = await refundForm.validateFields()
      values.orderId = selectedOrder.id
      await refundApi.apply(values)
      message.success('退款申请提交成功')
      setRefundModalVisible(false)
      loadData()
    } catch (error) {
      message.error('申请退款失败: ' + error.message)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      await orderApi.create(values)
      message.success('订单创建成功')
      setModalVisible(false)
      loadData()
    } catch (error) {
      message.error('创建订单失败: ' + error.message)
    }
  }

  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 180
    },
    {
      title: '用户',
      dataIndex: 'userId',
      key: 'userId',
      render: (val) => {
        const user = users.find(u => u.id === val)
        return user ? (
          <div>
            <div>{user.realName}</div>
            <div style={{ fontSize: 12, color: '#666' }}>{user.username}</div>
          </div>
        ) : val
      }
    },
    {
      title: '商品数量',
      dataIndex: 'items',
      key: 'items',
      render: (val) => val?.length || 0
    },
    {
      title: '订单金额(¥)',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '优惠金额(¥)',
      dataIndex: 'discountAmount',
      key: 'discountAmount',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '税费(¥)',
      dataIndex: 'taxAmount',
      key: 'taxAmount',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '实付金额(¥)',
      dataIndex: 'actualAmount',
      key: 'actualAmount',
      render: (val) => <strong style={{ color: '#f5222d' }}>¥{val?.toFixed(2)}</strong>
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
          {record.status === 'PENDING_PAYMENT' && (
            <Button type="link" icon={<PayCircleOutlined />} onClick={() => handlePay(record.id)}>
              支付
            </Button>
          )}
          {canRefund(record.status) && (
            <Button type="link" danger onClick={() => handleRefund(record)}>
              申请退款
            </Button>
          )}
        </Space>
      )
    }
  ]

  const stats = [
    { title: '总订单数', value: orders.length, icon: <ShoppingCartOutlined />, color: '#1890ff' },
    { title: '待支付', value: orders.filter(o => o.status === 'PENDING_PAYMENT').length, icon: <ExclamationCircleOutlined />, color: '#faad14' },
    { title: '待报关', value: orders.filter(o => o.status === 'PENDING_CUSTOMS').length, icon: <CheckCircleOutlined />, color: '#1890ff' },
    { title: '已完成', value: orders.filter(o => o.status === 'DELIVERED').length, icon: <CheckCircleOutlined />, color: '#52c41a' }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>订单管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            创建订单
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
        dataSource={orders}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1400 }}
      />

      <Modal
        title="创建订单"
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={800}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="userId"
            label="选择用户"
            rules={[{ required: true, message: '请选择用户' }]}
          >
            <Select placeholder="请选择用户" style={{ width: '100%' }}>
              {users.map(user => (
                <Option key={user.id} value={user.id}>
                  {user.realName} ({user.username}) - {user.isVerified ? '已认证' : '未认证'}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.List name="items">
            {(fields, { add, remove }) => (
              <div>
                <Divider>商品列表</Divider>
                {fields.map(({ key, name, ...restField }) => (
                  <Card key={key} size="small" style={{ marginBottom: 8 }}>
                    <Space wrap>
                      <Form.Item
                        {...restField}
                        name={[name, 'productId']}
                        rules={[{ required: true, message: '请选择商品' }]}
                        style={{ minWidth: 200, marginBottom: 0 }}
                      >
                        <Select placeholder="选择商品" style={{ width: 200 }}>
                          {products.map(product => (
                            <Option key={product.id} value={product.id}>
                              {product.name} - ¥{product.price?.toFixed(2)}
                            </Option>
                          ))}
                        </Select>
                      </Form.Item>
                      <Form.Item
                        {...restField}
                        name={[name, 'quantity']}
                        rules={[{ required: true, message: '请输入数量' }]}
                        style={{ marginBottom: 0 }}
                      >
                        <InputNumber min={1} placeholder="数量" />
                      </Form.Item>
                      <Form.Item
                        {...restField}
                        name={[name, 'discountAmount']}
                        style={{ marginBottom: 0 }}
                      >
                        <InputNumber min={0} placeholder="优惠金额" prefix="¥" />
                      </Form.Item>
                      <Button danger size="small" onClick={() => remove(name)}>
                        删除
                      </Button>
                    </Space>
                  </Card>
                ))}
                <Button type="dashed" onClick={() => add()} block>
                  添加商品
                </Button>
              </div>
            )}
          </Form.List>

          <Divider />

          <Form.Item
            name="discountAmount"
            label="订单总优惠金额(¥)"
          >
            <InputNumber min={0} style={{ width: '100%' }} placeholder="订单总优惠金额" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="订单详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {selectedOrder && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="订单号">{selectedOrder.orderNo}</Descriptions.Item>
              <Descriptions.Item label="订单状态">
                <Tag color={getStatusColor(selectedOrder.status)}>
                  {getStatusDesc(selectedOrder.status)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="用户">
                {users.find(u => u.id === selectedOrder.userId)?.realName || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="支付时间">
                {selectedOrder.paymentTime || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="订单金额">¥{selectedOrder.totalAmount?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="优惠金额">¥{selectedOrder.discountAmount?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="税费">¥{selectedOrder.taxAmount?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="实付金额">
                <strong style={{ color: '#f5222d' }}>¥{selectedOrder.actualAmount?.toFixed(2)}</strong>
              </Descriptions.Item>
              <Descriptions.Item label="创建时间" span={2}>
                {selectedOrder.createTime}
              </Descriptions.Item>
            </Descriptions>

            <Divider>商品列表</Divider>
            <Table
              dataSource={selectedOrder.items}
              rowKey="id"
              pagination={false}
              columns={[
                { title: '商品名称', dataIndex: 'productName', key: 'productName' },
                { title: 'SKU', dataIndex: 'sku', key: 'sku' },
                { title: '原产国', dataIndex: 'originCountry', key: 'originCountry' },
                { title: '单价', dataIndex: 'price', key: 'price', render: v => `¥${v?.toFixed(2)}` },
                { title: '数量', dataIndex: 'quantity', key: 'quantity' },
                { title: '税率', dataIndex: 'taxRate', key: 'taxRate', render: v => `${(v * 100).toFixed(0)}%` },
                { title: '小计', dataIndex: 'totalAmount', key: 'totalAmount', render: v => `¥${v?.toFixed(2)}` }
              ]}
            />
          </div>
        )}
      </Modal>

      <Modal
        title="申请退款"
        open={refundModalVisible}
        onOk={handleSubmitRefund}
        onCancel={() => setRefundModalVisible(false)}
        okText="提交申请"
        cancelText="取消"
        width={500}
      >
        {selectedOrder && (
          <div>
            <Card size="small" style={{ marginBottom: 16 }}>
              <Descriptions column={1} size="small">
                <Descriptions.Item label="订单号">{selectedOrder.orderNo}</Descriptions.Item>
                <Descriptions.Item label="订单状态">
                  <Tag color={getStatusColor(selectedOrder.status)}>
                    {getStatusDesc(selectedOrder.status)}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="实付金额">
                  <strong style={{ color: '#f5222d', fontSize: 16 }}>
                    ¥{selectedOrder.actualAmount?.toFixed(2)}
                  </strong>
                </Descriptions.Item>
                <Descriptions.Item label="税费">
                  ¥{selectedOrder.taxAmount?.toFixed(2)}
                </Descriptions.Item>
                <Descriptions.Item label="退款规则">
                  <Tag color={getRefundRule(selectedOrder.status) === '全额退款' ? 'green' : 'orange'}>
                    {getRefundRule(selectedOrder.status)}
                  </Tag>
                </Descriptions.Item>
              </Descriptions>
            </Card>
            <Form form={refundForm} layout="vertical">
              <Form.Item
                name="reason"
                label="退款原因"
                rules={[{ required: true, message: '请输入退款原因' }]}
              >
                <TextArea rows={4} placeholder="请输入退款原因" />
              </Form.Item>
            </Form>
            <div style={{ marginTop: 16, padding: 12, backgroundColor: '#fff7e6', borderRadius: 4 }}>
              <p style={{ margin: 0, fontSize: 12, color: '#fa8c16' }}>
                <strong>温馨提示：</strong>
              </p>
              <p style={{ margin: 0, fontSize: 12, color: '#666', marginTop: 4 }}>
                • 未报关成功（待支付、待报关、报关中）：全额退款
              </p>
              <p style={{ margin: 0, fontSize: 12, color: '#666' }}>
                • 报关成功后（报关成功、待发货、已发货）：扣除税费 + 5%服务费
              </p>
              <p style={{ margin: 0, fontSize: 12, color: '#666' }}>
                • 退款成功后，用户年度跨境额度将自动恢复
              </p>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Orders
