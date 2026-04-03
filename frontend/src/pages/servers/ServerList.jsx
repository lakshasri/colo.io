import React, { useEffect, useState } from 'react'
import { Table, Button, Tag, Typography, Space, Modal, Form, Input, InputNumber } from 'antd'
import { PlusOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import api from '../../services/api'

const { Title } = Typography

const STATUS_COLOR = {
  OPERATIONAL:    'green',
  FAULTY:         'red',
  MAINTENANCE:    'orange',
  DECOMMISSIONED: 'default',
  UNALLOCATED:    'blue'
}

export default function ServerList() {
  const [servers, setServers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const fetchServers = async () => {
    setLoading(true)
    try {
      const res = await api.get('/servers')
      setServers(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchServers() }, [])

  const onRegister = async (values) => {
    await api.post('/servers', values)
    setModalOpen(false)
    form.resetFields()
    fetchServers()
  }

  const columns = [
    { title: 'Hostname',  dataIndex: 'hostname',  key: 'hostname' },
    { title: 'IP',        dataIndex: 'ipAddress', key: 'ipAddress' },
    { title: 'CPU',       dataIndex: 'cpuCores',  key: 'cpuCores',
      render: v => `${v} cores` },
    { title: 'RAM',       dataIndex: 'ramGb',     key: 'ramGb',
      render: v => `${v} GB` },
    { title: 'Disk',      dataIndex: 'diskTb',    key: 'diskTb',
      render: v => `${v} TB` },
    { title: 'U-Size',    dataIndex: 'uSize',     key: 'uSize',
      render: v => `${v}U` },
    {
      title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={STATUS_COLOR[s]}>{s}</Tag>
    },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => (
        <Button icon={<EyeOutlined />} size="small"
                onClick={() => navigate(`/servers/${record.serverId}`)}>
          View
        </Button>
      )
    }
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <Title level={4} style={{ margin: 0 }}>Servers</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          Register Server
        </Button>
      </Space>

      <Table dataSource={servers} columns={columns} rowKey="serverId" loading={loading} />

      <Modal title="Register Server" open={modalOpen}
             onCancel={() => setModalOpen(false)}
             onOk={() => form.submit()} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={onRegister}>
          <Form.Item name="hostname" label="Hostname" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="ipAddress" label="IP Address">
            <Input />
          </Form.Item>
          <Form.Item name="uSize" label="U-Size" rules={[{ required: true }]}>
            <InputNumber min={1} max={42} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="cpuCores" label="CPU Cores">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="ramGb" label="RAM (GB)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="diskTb" label="Disk (TB)">
            <InputNumber min={0} step={0.5} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
