import React, { useEffect, useState } from 'react'
import { Table, Button, Tag, Typography, Space, Modal, Form, Input, InputNumber, Select, message } from 'antd'
import { PlusOutlined, EyeOutlined, SearchOutlined, DeleteOutlined } from '@ant-design/icons'
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
  const [searchHostname, setSearchHostname] = useState('')
  const [searchStatus, setSearchStatus] = useState(null)
  const [selectedRowKeys, setSelectedRowKeys] = useState([])
  const [bulkStatus, setBulkStatus] = useState(null)
  const navigate = useNavigate()

  const fetchServers = async (hostname, status) => {
    setLoading(true)
    try {
      const params = {}
      if (hostname) params.hostname = hostname
      if (status) params.status = status
      const res = Object.keys(params).length
        ? await api.get('/servers/search', { params })
        : await api.get('/servers')
      setServers(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchServers() }, [])

  const handleBulkStatusUpdate = async () => {
    if (!bulkStatus || selectedRowKeys.length === 0) return
    try {
      await api.patch('/bulk/servers/status', { serverIds: selectedRowKeys, status: bulkStatus })
      message.success(`Updated ${selectedRowKeys.length} servers to ${bulkStatus}`)
      setSelectedRowKeys([])
      setBulkStatus(null)
      fetchServers(searchHostname, searchStatus)
    } catch {
      message.error('Bulk update failed')
    }
  }

  const handleBulkDecommission = async () => {
    if (selectedRowKeys.length === 0) return
    Modal.confirm({
      title: 'Decommission Selected Servers',
      content: `Decommission ${selectedRowKeys.length} server(s)?`,
      okText: 'Decommission',
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await api.post('/bulk/servers/decommission', { serverIds: selectedRowKeys })
          message.success(`Decommissioned ${selectedRowKeys.length} servers`)
          setSelectedRowKeys([])
          fetchServers(searchHostname, searchStatus)
        } catch {
          message.error('Bulk decommission failed')
        }
      }
    })
  }

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

      <Space style={{ marginBottom: 12 }}>
        <Input placeholder="Search hostname..." prefix={<SearchOutlined />}
          value={searchHostname} onChange={e => setSearchHostname(e.target.value)}
          style={{ width: 220 }} allowClear />
        <Select placeholder="Filter by status" allowClear style={{ width: 180 }}
          value={searchStatus} onChange={setSearchStatus}
          options={['OPERATIONAL','FAULTY','MAINTENANCE','DECOMMISSIONED','UNALLOCATED']
            .map(s => ({ value: s, label: s }))} />
        <Button icon={<SearchOutlined />}
          onClick={() => fetchServers(searchHostname, searchStatus)}>
          Search
        </Button>
      </Space>

      {selectedRowKeys.length > 0 && (
        <Space style={{ marginBottom: 12, padding: '8px 12px', background: '#f0f5ff', borderRadius: 6 }}>
          <span>{selectedRowKeys.length} selected</span>
          <Select placeholder="Set status..." style={{ width: 180 }} value={bulkStatus}
            onChange={setBulkStatus}
            options={['OPERATIONAL','FAULTY','MAINTENANCE']
              .map(s => ({ value: s, label: s }))} />
          <Button type="primary" size="small" onClick={handleBulkStatusUpdate}
                  disabled={!bulkStatus}>Apply</Button>
          <Button danger size="small" icon={<DeleteOutlined />} onClick={handleBulkDecommission}>
            Decommission All
          </Button>
        </Space>
      )}

      <Table
        dataSource={servers}
        columns={columns}
        rowKey="serverId"
        loading={loading}
        rowSelection={{ selectedRowKeys, onChange: setSelectedRowKeys }}
      />

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
