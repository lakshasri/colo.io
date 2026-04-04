import React, { useEffect, useState } from 'react'
import { Table, Button, Tag, Typography, Space, Modal, Form, Input, Select, message } from 'antd'
import { PlusOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import api from '../../services/api'

const { Title } = Typography

const PRIORITY_COLOR = { LOW: 'blue', MEDIUM: 'gold', HIGH: 'orange', CRITICAL: 'red' }
const STATUS_COLOR = { OPEN: 'red', IN_PROGRESS: 'orange', RESOLVED: 'green', CLOSED: 'default' }

export default function MaintenanceList() {
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const fetchTickets = async () => {
    setLoading(true)
    try {
      const res = await api.get('/maintenance')
      setTickets(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchTickets() }, [])

  const handleCreate = async (values) => {
    try {
      await api.post('/maintenance', values)
      message.success('Ticket created')
      setModalOpen(false)
      form.resetFields()
      fetchTickets()
    } catch {
      message.error('Failed to create ticket')
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'ticketId', width: 70 },
    { title: 'Title', dataIndex: 'title' },
    {
      title: 'Priority',
      dataIndex: 'priority',
      render: p => <Tag color={PRIORITY_COLOR[p]}>{p}</Tag>
    },
    {
      title: 'Status',
      dataIndex: 'status',
      render: s => <Tag color={STATUS_COLOR[s]}>{s}</Tag>
    },
    { title: 'Assigned To', dataIndex: 'assignedTo', render: v => v || '—' },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      render: v => new Date(v).toLocaleDateString()
    },
    {
      title: 'Actions',
      render: (_, row) => (
        <Button icon={<EyeOutlined />} size="small"
          onClick={() => navigate(`/maintenance/${row.ticketId}`)}>
          View
        </Button>
      )
    }
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <Title level={3} style={{ margin: 0 }}>Maintenance Tickets</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          New Ticket
        </Button>
      </Space>

      <Table rowKey="ticketId" columns={columns} dataSource={tickets} loading={loading} />

      <Modal title="New Maintenance Ticket" open={modalOpen}
        onCancel={() => setModalOpen(false)} onOk={() => form.submit()}>
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="serverId" label="Server ID" rules={[{ required: true }]}>
            <Input type="number" />
          </Form.Item>
          <Form.Item name="title" label="Title" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="priority" label="Priority" initialValue="MEDIUM">
            <Select options={['LOW','MEDIUM','HIGH','CRITICAL'].map(p => ({ value: p, label: p }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
