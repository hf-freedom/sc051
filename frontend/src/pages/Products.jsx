import React, { useState, useEffect } from 'react'
import { Table, Button, Tag, Modal, Form, Input, InputNumber, Select, message, Space, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons'
import { productApi } from '../api'

const { TextArea } = Input

const Products = () => {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadProducts()
  }, [])

  const loadProducts = async () => {
    try {
      setLoading(true)
      const result = await productApi.getAll()
      setProducts(result.data || [])
    } catch (error) {
      message.error('加载商品列表失败: ' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingProduct(null)
    form.resetFields()
    form.setFieldsValue({
      taxRate: 0.13,
      weight: 0,
      price: 0,
      stock: 0,
      isBonded: true
    })
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingProduct(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await productApi.delete(id)
      message.success('删除成功')
      loadProducts()
    } catch (error) {
      message.error('删除失败: ' + error.message)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      if (editingProduct) {
        await productApi.update(editingProduct.id, values)
        message.success('更新成功')
      } else {
        await productApi.create(values)
        message.success('创建成功')
      }
      setModalVisible(false)
      loadProducts()
    } catch (error) {
      message.error('操作失败: ' + error.message)
    }
  }

  const columns = [
    {
      title: '商品名称',
      dataIndex: 'name',
      key: 'name'
    },
    {
      title: 'SKU',
      dataIndex: 'sku',
      key: 'sku'
    },
    {
      title: '原产国',
      dataIndex: 'originCountry',
      key: 'originCountry'
    },
    {
      title: '税率',
      dataIndex: 'taxRate',
      key: 'taxRate',
      render: (val) => `${(val * 100).toFixed(0)}%`
    },
    {
      title: '重量(kg)',
      dataIndex: 'weight',
      key: 'weight'
    },
    {
      title: '单价(¥)',
      dataIndex: 'price',
      key: 'price',
      render: (val) => `¥${val?.toFixed(2)}`
    },
    {
      title: '库存',
      dataIndex: 'stock',
      key: 'stock'
    },
    {
      title: '保税仓',
      dataIndex: 'isBonded',
      key: 'isBonded',
      render: (val) => (
        <Tag color={val ? 'green' : 'orange'}>
          {val ? '是' : '否'}
        </Tag>
      )
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定要删除该商品吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>商品管理</h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={loadProducts}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加商品
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={products}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={editingProduct ? '编辑商品' : '添加商品'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="商品名称"
            rules={[{ required: true, message: '请输入商品名称' }]}
          >
            <Input placeholder="请输入商品名称" />
          </Form.Item>
          <Form.Item
            name="sku"
            label="SKU"
            rules={[{ required: true, message: '请输入SKU' }]}
          >
            <Input placeholder="请输入SKU" />
          </Form.Item>
          <Form.Item
            name="originCountry"
            label="原产国"
            rules={[{ required: true, message: '请选择原产国' }]}
          >
            <Select placeholder="请选择原产国">
              <Select.Option value="日本">日本</Select.Option>
              <Select.Option value="韩国">韩国</Select.Option>
              <Select.Option value="美国">美国</Select.Option>
              <Select.Option value="法国">法国</Select.Option>
              <Select.Option value="德国">德国</Select.Option>
              <Select.Option value="英国">英国</Select.Option>
              <Select.Option value="意大利">意大利</Select.Option>
              <Select.Option value="其他">其他</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item label="税率">
            <Form.Item
              name="taxRate"
              style={{ display: 'inline-block', width: 'calc(50% - 8px)', marginRight: 16 }}
            >
              <InputNumber
                min={0}
                max={1}
                step={0.01}
                style={{ width: '100%' }}
                placeholder="税率"
              />
            </Form.Item>
            <span style={{ lineHeight: '32px' }}>（如0.13表示13%）</span>
          </Form.Item>
          <Form.Item
            name="weight"
            label="重量(kg)"
          >
            <InputNumber min={0} step={0.01} style={{ width: '100%' }} placeholder="重量" />
          </Form.Item>
          <Form.Item
            name="price"
            label="单价(¥)"
          >
            <InputNumber min={0} step={0.01} style={{ width: '100%' }} placeholder="单价" />
          </Form.Item>
          <Form.Item
            name="stock"
            label="库存"
          >
            <InputNumber min={0} style={{ width: '100%' }} placeholder="库存" />
          </Form.Item>
          <Form.Item
            name="isBonded"
            label="是否保税仓商品"
          >
            <Select>
              <Select.Option value={true}>是</Select.Option>
              <Select.Option value={false}>否</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="description"
            label="描述"
          >
            <TextArea rows={3} placeholder="商品描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Products
