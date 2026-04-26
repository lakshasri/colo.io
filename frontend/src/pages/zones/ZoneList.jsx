import React, { useEffect, useState } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, Space, Typography, Tag } from 'antd'
import { PlusOutlined, EditOutlined } from '@ant-design/icons'
import api from '../../services/api'

const { Title } = Typography

export default function ZoneList() {
  const [zones, setZones] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingZone, setEditingZone] = useState(null)
  const [form] = Form.useForm()

  const fetchZones = async () => {
    setLoading(true)
    try {
      const res = await api.get('/zones')
      setZones(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchZones() }, [])

  const openCreate = () => { setEditingZone(null); form.resetFields(); setModalOpen(true) }
  const openEdit = (zone) => { setEditingZone(zone); form.setFieldsValue(zone); setModalOpen(true) }

  const onSubmit = async (values) => {
    if (editingZone) {
      await api.put(`/zones/${editingZone.zoneId}`, values)
    } else {
      await api.post('/zones', values)
    }
    setModalOpen(false)
    fetchZones()
  }

  const columns = [
    { title: 'Name',     dataIndex: 'name',           key: 'name' },
    { title: 'Floor',    dataIndex: 'floor',          key: 'floor' },
    { title: 'Power Budget (kW)', dataIndex: 'powerBudgetKw', key: 'powerBudgetKw' },
    { title: 'Cooling',  dataIndex: 'coolingCapacity', key: 'coolingCapacity' },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => (
        <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(record)}>Edit</Button>
      )
    }
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <Title level={4} style={{ margin: 0 }}>Zones</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>New Zone</Button>
      </Space>

      <Table dataSource={zones} columns={columns} rowKey="zoneId" loading={loading} />

      <Modal title={editingZone ? 'Edit Zone' : 'New Zone'}
             open={modalOpen} onCancel={() => setModalOpen(false)}
             onOk={() => form.submit()} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={onSubmit}>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="floor" label="Floor">
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="powerBudgetKw" label="Power Budget (kW)">
            <InputNumber style={{ width: '100%' }} step={0.1} />
          </Form.Item>
          <Form.Item name="coolingCapacity" label="Cooling Capacity">
            <InputNumber style={{ width: '100%' }} step={0.1} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
