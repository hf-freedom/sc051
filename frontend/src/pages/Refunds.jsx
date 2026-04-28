import React, { useState, useEffect } from 'react'
import {
  Table, Button, Tag, Modal, Form, Input, Select, message, Space,
  Descriptions, Card, Row, Col, Statistic, Divider
} from 'antd'
import {
  ReloadOutlined, EyeOutlined, RollbackOutlined, PlusOutlined,
  CheckCircleOutlined, CloseCircleOutlined, ExclamationCircleOutlined
} from '@ant-design/icons'
import { refundApi, orderApi, userApi } from '../api'

const { Option } = Select
const { TextArea } = Input

const Refunds = () => {
  const [refunds, setRefunds] = useState([])
  const [orders, setOrders] = useState([])
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [rejectModalVisible, setRejectModalVisible] = useState(false)
  const [selectedRefund, setSelectedRefund] = useState(null)
  const [form] = Form.useForm()
  const [rejectForm] = Form.useForm()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [refundsResult, ordersResult, usersResult] = await Promise.all([
        refundApi.getAll(),
        orderApi.getAll(),
        userApi.getAll()
      ])
      setRefunds(refundsResult.data || [])
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
      'APPROVED': 'blue',
      'PROCESSING': 'cyan',
      'COMPLETED': 'green',
      'REJECTED': 'red'
    }
    return colors[status] || 'default'
  }

  const getStatusDesc = (status) => {
    const descs = {
      'PENDING': '待审核',
      'APPROVED': '已同意',
      'PROCESSING': '处理中',
      'COMPLETED': '已完成',
      'REJECTED': '已拒绝'
    }
    return descs[status] || status
  }

  const getTypeDesc = (type) => {
    const descs = {
      'FULL_REFUND': '全额退款',
      'PARTIAL_REFUND': '部分退款'
    }
    return descs[type] || type
  }

  const handleViewDetail = (record) => {
    setSelectedRefund(record)
    setDetailModalVisible(true)
  }

  const handleCreate = () => {
    form.resetFields()
    setCreateModalVisible(true)
  }

  const handleApprove = async (refundId) => {
    try {
      await refundApi.approve(refundId)
      message.success('退款批准成功，已处理退款并恢复用户额度')
      loadData()
    } catch (error) {
      message.error('批准退款失败: ' + error.message)
    }
  }

  const handleReject = (record) => {
    setSelectedRefund(record)
    rejectForm.resetFields()
    setRejectModalVisible(true)
  }

  const handleCreateSubmit = async () => {
    try {
      const values = await form.validateFields()
      await refundApi.apply(values)
      message.success('退款申请提交成功')
      setCreateModalVisible(false)
      loadData()
    } catch (error) {
      message.error('提交退款申请失败: ' + error.message)
    }
  }

  const handleRejectSubmit = async () => {
    try {
      const values = await rejectForm.validateFields()
      await refundApi.reject(selectedRefund.id, values.reason)
      message.success('退款申请已拒绝')
      setRejectModalVisible(false)
      loadData()
    } catch (error) {
      message.error('拒绝退款失败: ' + error.message)
    }
  }

  const refundableOrders = orders.filter(o =>
    ['PENDING_CUSTOMS', 'CUSTOMS_PROCESSING', 'CUSTOMS_SUCCESS', 'PENDING_SHIPMENT', 'SHIPPED'].includes(o.status) &&
    !refunds.some(r => r.orderId === o.id && r.status !== 'REJECTED')
  )

  const columns = [
    {
      title: '退款单号',
      dataIndex: 'refundNo',
      key: 'refundNo',
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
      title: '用户',
      dataIndex: 'userId',
      key: 'userId',
      render: (val) => {
        const user = users.find(u => u.id === val)
        return user ? user.realName : '-'
      }
    },
    {
      title: '退款类型',
      dataIndex: 'type',
      key: 'type',
      render: (val) => getTypeDesc(val)
    },
    {
      title: '订单总金额(¥)',
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
      title: '扣款金额(¥)',
      dataIndex: 'deductAmount',
      key: 'deductAmount',
      render: (val) => (
        <span style={{ color: '#ff4d4f' }}>
          ¥{val?.toFixed(2)}
        </span>
      )
    },
    {
      title: '实际退款(¥)',
      dataIndex: 'actualRefundAmount',
      key: 'actualRefundAmount',
      render: (val) => (
        <strong style={{ color: '#52c41a' }}>
          ¥{val?.toFixed(2)}
        </strong>
      )
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
      title: '额度已恢复',
      dataIndex: 'quotaRestored',
      key: 'quotaRestored',
      render: (val) => (
        <Tag color={val ? 'green' : 'default'}>
          {val ? '是' : '否'}
        </Tag>
      )
    },
    {
      title: '申请时间',
      dataIndex: 'applyTime',
      key: 'applyTime',
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
            <>
              <Button type="link" onClick={() => handleApprove(record.id)}>
                同意退款
              </Button>
              <Button type="link" danger onClick={() => handleReject(record)}>
                拒绝退款
              </Button>
            </>
          )}
        </Space>
      )
    }
  ]

  const stats = [
    { title: '总退款单', value: refunds.length, icon: <RollbackOutlined />, color: '#1890ff' },
    { title: '待审核', value: refunds.filter(r => r.status === 'PENDING').length, icon: <ExclamationCircleOutlined />, color: '#faad14' },
    { title: '已完成', value: refunds.filter(r => r.status === 'COMPLETED').length, icon: <CheckCircleOutlined />, color: '#52c41a' },
    { title: '已拒绝', value: refunds.filter(r => r.status === 'REJECTED').length, icon: <CloseCircleOutlined />, color: '#ff4d4f' }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>退款管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadData}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate} disabled={refundableOrders.length === 0}>
            申请退款
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

      <Card type="inner" style={{ marginBottom: 16, backgroundColor: '#fff7e6' }}>
        <p><strong>退款规则说明：</strong></p>
        <ul>
          <li><strong>未报关成功</strong>（待支付、待报关、报关中）：全额退款</li>
          <li><strong>报关成功后</strong>（报关成功、待发货、已发货）：扣除税费和5%服务费</li>
          <li><strong>退款后</strong>：自动恢复用户年度跨境额度</li>
        </ul>
      </Card>

      <Table
        columns={columns}
        dataSource={refunds}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1600 }}
      />

      <Modal
        title="申请退款"
        open={createModalVisible}
        onOk={handleCreateSubmit}
        onCancel={() => setCreateModalVisible(false)}
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="orderId"
            label="选择订单"
            rules={[{ required: true, message: '请选择订单' }]}
          >
            <Select placeholder="请选择订单" style={{ width: '100%' }}>
              {refundableOrders.map(order => {
                const orderStatus = {
                  'PENDING_PAYMENT': '待支付（全额退款）',
                  'PENDING_CUSTOMS': '待报关（全额退款）',
                  'CUSTOMS_PROCESSING': '报关中（全额退款）',
                  'CUSTOMS_SUCCESS': '报关成功（扣税费+5%服务费）',
                  'PENDING_SHIPMENT': '待发货（扣税费+5%服务费）',
                  'SHIPPED': '已发货（扣税费+5%服务费）'
                }
                return (
                  <Option key={order.id} value={order.id}>
                    {order.orderNo} - ¥{order.actualAmount?.toFixed(2)} - {orderStatus[order.status]}
                  </Option>
                )
              })}
            </Select>
          </Form.Item>
          <Form.Item
            name="reason"
            label="退款原因"
            rules={[{ required: true, message: '请输入退款原因' }]}
          >
            <TextArea rows={3} placeholder="请输入退款原因" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="拒绝退款"
        open={rejectModalVisible}
        onOk={handleRejectSubmit}
        onCancel={() => setRejectModalVisible(false)}
        width={400}
      >
        <Form form={rejectForm} layout="vertical">
          <Form.Item
            name="reason"
            label="拒绝原因"
            rules={[{ required: true, message: '请输入拒绝原因' }]}
          >
            <TextArea rows={3} placeholder="请输入拒绝原因" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="退款单详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={600}
      >
        {selectedRefund && (
          <div>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="退款单号">{selectedRefund.refundNo}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={getStatusColor(selectedRefund.status)}>
                  {getStatusDesc(selectedRefund.status)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="订单号">
                {orders.find(o => o.id === selectedRefund.orderId)?.orderNo || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="退款类型">{getTypeDesc(selectedRefund.type)}</Descriptions.Item>
              <Descriptions.Item label="订单总金额">¥{selectedRefund.totalAmount?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="税费">¥{selectedRefund.taxAmount?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="扣款金额">
                <span style={{ color: '#ff4d4f' }}>¥{selectedRefund.deductAmount?.toFixed(2)}</span>
              </Descriptions.Item>
              <Descriptions.Item label="实际退款">
                <strong style={{ color: '#52c41a' }}>¥{selectedRefund.actualRefundAmount?.toFixed(2)}</strong>
              </Descriptions.Item>
              <Descriptions.Item label="额度已恢复">
                <Tag color={selectedRefund.quotaRestored ? 'green' : 'default'}>
                  {selectedRefund.quotaRestored ? '是' : '否'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="申请时间">{selectedRefund.applyTime || '-'}</Descriptions.Item>
              <Descriptions.Item label="完成时间" span={2}>
                {selectedRefund.completeTime || '-'}
              </Descriptions.Item>
            </Descriptions>

            {selectedRefund.deductReason && (
              <div style={{ marginTop: 16 }}>
                <Divider>扣款说明</Divider>
                <Card type="inner" style={{ backgroundColor: '#fff2f0' }}>
                  {selectedRefund.deductReason}
                </Card>
              </div>
            )}

            {selectedRefund.reason && (
              <div style={{ marginTop: 16 }}>
                <Divider>退款原因</Divider>
                <Card type="inner">
                  {selectedRefund.reason}
                </Card>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default Refunds
