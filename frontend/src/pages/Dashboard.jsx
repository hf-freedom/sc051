import React, { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, message } from 'antd'
import {
  ShopOutlined,
  UserOutlined,
  ShoppingCartOutlined,
  FileSearchOutlined,
  TruckOutlined,
  RollbackOutlined
} from '@ant-design/icons'
import { productApi, userApi, orderApi, customsApi, logisticsApi, refundApi } from '../api'

const Dashboard = () => {
  const [stats, setStats] = useState({
    products: 0,
    users: 0,
    orders: 0,
    customs: 0,
    logistics: 0,
    refunds: 0
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      setLoading(true)
      const [products, users, orders, customs, logistics, refunds] = await Promise.all([
        productApi.getAll(),
        userApi.getAll(),
        orderApi.getAll(),
        customsApi.getAll(),
        logisticsApi.getAll(),
        refundApi.getAll()
      ])

      setStats({
        products: products.data?.length || 0,
        users: users.data?.length || 0,
        orders: orders.data?.length || 0,
        customs: customs.data?.length || 0,
        logistics: logistics.data?.length || 0,
        refunds: refunds.data?.length || 0
      })
    } catch (error) {
      message.error('加载统计数据失败: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>系统仪表盘</h2>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card" loading={loading}>
            <Statistic
              title="商品数量"
              value={stats.products}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card" loading={loading}>
            <Statistic
              title="用户数量"
              value={stats.users}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card" loading={loading}>
            <Statistic
              title="订单数量"
              value={stats.orders}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card" loading={loading}>
            <Statistic
              title="报关单数量"
              value={stats.customs}
              prefix={<FileSearchOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card" loading={loading}>
            <Statistic
              title="物流单数量"
              value={stats.logistics}
              prefix={<TruckOutlined />}
              valueStyle={{ color: '#13c2c2' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8} lg={4}>
          <Card className="stat-card" loading={loading}>
            <Statistic
              title="退款单数量"
              value={stats.refunds}
              prefix={<RollbackOutlined />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="系统说明">
            <p><strong>跨境电商订单管理系统</strong>包含以下核心功能模块：</p>
            <ul>
              <li><strong>商品管理</strong>：管理商品信息，包括原产国、税率、重量、是否保税仓商品等</li>
              <li><strong>用户管理</strong>：管理用户实名信息、身份证号、年度跨境额度</li>
              <li><strong>订单管理</strong>：用户下单时校验实名信息和年度额度，计算税费</li>
              <li><strong>报关管理</strong>：支付成功后进入待报关，报关成功后才能发货</li>
              <li><strong>物流管理</strong>：发货后生成国际物流轨迹</li>
              <li><strong>退款管理</strong>：未报关成功可全额退款，报关成功后扣除税费或服务费</li>
            </ul>
            <p style={{ marginTop: 16 }}><strong>系统流程：</strong></p>
            <ol>
              <li>用户下单 → 校验实名信息和年度额度 → 计算税费 → 创建订单</li>
              <li>支付成功 → 订单进入待报关状态</li>
              <li>创建报关单 → 提交报关 → 报关处理 → 报关成功/失败</li>
              <li>报关成功 → 创建物流单 → 发货 → 生成物流轨迹</li>
              <li>取消订单 → 未报关成功全额退款，报关成功扣除税费和服务费</li>
              <li>退款完成 → 恢复用户年度额度</li>
            </ol>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard
