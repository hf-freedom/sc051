import React, { useState } from 'react'
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import { Layout, Menu, theme } from 'antd'
import {
  ShopOutlined,
  UserOutlined,
  ShoppingCartOutlined,
  FileSearchOutlined,
  TruckOutlined,
  RollbackOutlined,
  DashboardOutlined
} from '@ant-design/icons'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import Users from './pages/Users'
import Orders from './pages/Orders'
import Customs from './pages/Customs'
import Logistics from './pages/Logistics'
import Refunds from './pages/Refunds'

const { Header, Sider, Content } = Layout

const menuItems = [
  { key: '/', icon: <DashboardOutlined />, label: '仪表盘' },
  { key: '/products', icon: <ShopOutlined />, label: '商品管理' },
  { key: '/users', icon: <UserOutlined />, label: '用户管理' },
  { key: '/orders', icon: <ShoppingCartOutlined />, label: '订单管理' },
  { key: '/customs', icon: <FileSearchOutlined />, label: '报关管理' },
  { key: '/logistics', icon: <TruckOutlined />, label: '物流管理' },
  { key: '/refunds', icon: <RollbackOutlined />, label: '退款管理' }
]

function App() {
  const [collapsed, setCollapsed] = useState(false)
  const {
    token: { colorBgContainer }
  } = theme.useToken()

  return (
    <Router>
      <Layout style={{ minHeight: '100vh' }}>
        <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
          <div className="logo">
            {collapsed ? '跨境' : '跨境电商系统'}
          </div>
          <Menu
            theme="dark"
            defaultSelectedKeys={['/']}
            mode="inline"
            items={menuItems.map(item => ({
              key: item.key,
              icon: item.icon,
              label: <Link to={item.key}>{item.label}</Link>
            }))}
          />
        </Sider>
        <Layout className="site-layout">
          <Header style={{ padding: 0, background: colorBgContainer }}>
            <h2 style={{ marginLeft: 24, lineHeight: '64px', margin: 0, paddingLeft: 24 }}>
              跨境电商订单管理系统
            </h2>
          </Header>
          <Content
            style={{
              margin: '24px 16px',
              padding: 24,
              minHeight: 280,
              background: colorBgContainer
            }}
          >
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/products" element={<Products />} />
              <Route path="/users" element={<Users />} />
              <Route path="/orders" element={<Orders />} />
              <Route path="/customs" element={<Customs />} />
              <Route path="/logistics" element={<Logistics />} />
              <Route path="/refunds" element={<Refunds />} />
            </Routes>
          </Content>
        </Layout>
      </Layout>
    </Router>
  )
}

export default App
