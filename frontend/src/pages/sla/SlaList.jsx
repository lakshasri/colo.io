import React, { useEffect, useState } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, DatePicker, Select, Tag, Space, message } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import api from '../../services/api'
import dayjs from 'dayjs'

const statusColor = { ACTIVE: 'green', EXPIRED: 'red', SUSPENDED: 'orange' }

export default function SlaList() {
  const [slas, setSlas] = useState([])
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()

  const fetchSlas = async () => {
    setLoading(true)
    try {
      const res = await api.get('/sla')
      setSlas(res.data)
    } catch {
      message.error('Failed to load SLAs')
    } finally {
      setLoading(false)
    }
  }

  const fetchCustomers = async () => {
    try {
      const res = await api.get('/users')
      setCustomers(res.data.filter(u => u.role === 'CUSTOMER'))
    } catch {}
  }

  useEffect(() => { fetchSlas(); fetchCustomers() }, [])

  const handleCreate = async (values) => {
    try {
      await api.post('/sla', {
        ...values,
        startDate: values.startDate.format('YYYY-MM-DD'),
        endDate: values.endDate ? values.endDate.format('YYYY-MM-DD') : undefined,
      })
      message.success('SLA created')
      setModalOpen(false)
      form.resetFields()
      fetchSlas()
    } catch {
      message.error('Failed to create SLA')
    }
  }

  const handleStatusChange = async (id, status) => {
    try {
      await api.patch(`/sla/${id}/status`, { status })
      fetchSlas()
    } catch {
      message.error('Failed to update status')
    }
  }

  const handleDelete = async (id) => {
    try {
      await api.delete(`/sla/${id}`)
      message.success('Deleted')
      fetchSlas()
    } catch {
      message.error('Failed to delete')
    }
  }

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Customer', dataIndex: ['customer', 'username'], key: 'customer' },
    { title: 'Uptime %', dataIndex: 'uptimeGuaranteePct', key: 'uptime', render: v => `${v}%` },
    { title: 'Response (min)', dataIndex: 'responseTimeMinutes', key: 'response' },
    { title: 'Resolution (hrs)', dataIndex: 'resolutionTimeHours', key: 'resolution' },
    { title: 'Start', dataIndex: 'startDate', key: 'start' },
    { title: 'End', dataIndex: 'endDate', key: 'end', render: v => v || '—' },
    {
      title: 'Status', dataIndex: 'status', key: 'status',
      render: v => <Tag color={statusColor[v]}>{v}</Tag>
    },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => (
        <Space>
          <Select
            size="small"
            value={record.status}
            onChange={v => handleStatusChange(record.slaId, v)}
            style={{ width: 110 }}
          >
            <Select.Option value="ACTIVE">Active</Select.Option>
            <Select.Option value="SUSPENDED">Suspend</Select.Option>
            <Select.Option value="EXPIRED">Expire</Select.Option>
          </Select>
          <Button size="small" danger onClick={() => handleDelete(record.slaId)}>Delete</Button>
        </Space>
      )
    }
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>SLA Agreements</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          New SLA
        </Button>
      </div>

      <Table
        dataSource={slas}
        columns={columns}
        rowKey="slaId"
        loading={loading}
        size="middle"
      />

      <Modal
        title="Create SLA Agreement"
        open={modalOpen}
        onCancel={() => { setModalOpen(false); form.resetFields() }}
        onOk={() => form.submit()}
        okText="Create"
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="customerId" label="Customer" rules={[{ required: true }]}>
            <Select placeholder="Select customer">
              {customers.map(c => (
                <Select.Option key={c.userId} value={c.userId}>{c.username}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="name" label="SLA Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="uptimeGuaranteePct" label="Uptime Guarantee (%)" rules={[{ required: true }]}>
            <InputNumber min={0} max={100} step={0.1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="responseTimeMinutes" label="Response Time (minutes)" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="resolutionTimeHours" label="Resolution Time (hours)" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="startDate" label="Start Date" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="endDate" label="End Date">
            <DatePicker style={{ width: '100%' }} disabledDate={d => d && d < dayjs()} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
