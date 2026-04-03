import React, { useEffect, useState } from 'react'
import { Table, Button, Modal, Form, Input, InputNumber, Select, Space,
         Typography, Progress, Tag } from 'antd'
import { PlusOutlined, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import api from '../../services/api'

const { Title } = Typography

const STATUS_COLOR = { ACTIVE: 'green', MAINTENANCE: 'orange', DECOMMISSIONED: 'red' }

export default function RackList() {
  const [racks, setRacks] = useState([])
  const [zones, setZones] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const fetchAll = async () => {
    setLoading(true)
    try {
      const [rackRes, zoneRes] = await Promise.all([api.get('/racks'), api.get('/zones')])
      setRacks(rackRes.data)
      setZones(zoneRes.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchAll() }, [])

  const onSubmit = async (values) => {
    await api.post('/racks', values)
    setModalOpen(false)
    fetchAll()
  }

  const columns = [
    { title: 'Name',     dataIndex: 'name',     key: 'name' },
    { title: 'Location', dataIndex: 'location', key: 'location' },
    {
      title: 'U-Space', key: 'uspace',
      render: (_, r) => (
        <div style={{ minWidth: 120 }}>
          <Progress
            percent={Math.round((r.usedUSpace / r.totalUSpace) * 100)}
            size="small"
            format={() => `${r.usedUSpace}/${r.totalUSpace}U`}
          />
        </div>
      )
    },
    {
      title: 'Power', key: 'power',
      render: (_, r) => (
        <div style={{ minWidth: 120 }}>
          <Progress
            percent={r.maxPowerKw ? Math.round((r.currentPowerKw / r.maxPowerKw) * 100) : 0}
            size="small"
            status={r.currentPowerKw / r.maxPowerKw > 0.85 ? 'exception' : 'normal'}
            format={() => `${r.currentPowerKw?.toFixed(1)}/${r.maxPowerKw}kW`}
          />
        </div>
      )
    },
    {
      title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={STATUS_COLOR[s]}>{s}</Tag>
    },
    {
      title: 'Actions', key: 'actions',
      render: (_, record) => (
        <Button icon={<EyeOutlined />} size="small"
                onClick={() => navigate(`/racks/${record.rackId}`)}>View</Button>
      )
    }
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <Title level={4} style={{ margin: 0 }}>Racks</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          New Rack
        </Button>
      </Space>

      <Table dataSource={racks} columns={columns} rowKey="rackId" loading={loading} />

      <Modal title="New Rack" open={modalOpen}
             onCancel={() => setModalOpen(false)}
             onOk={() => form.submit()} destroyOnClose>
        <Form form={form} layout="vertical" onFinish={onSubmit}>
          <Form.Item name="name" label="Rack Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. R-A01" />
          </Form.Item>
          <Form.Item name="zoneId" label="Zone" rules={[{ required: true }]}>
            <Select options={zones.map(z => ({ value: z.zoneId, label: z.name }))} />
          </Form.Item>
          <Form.Item name="location" label="Location">
            <Input />
          </Form.Item>
          <Form.Item name="totalUSpace" label="Total U-Space" initialValue={42}>
            <InputNumber style={{ width: '100%' }} min={1} max={52} />
          </Form.Item>
          <Form.Item name="maxPowerKw" label="Max Power (kW)">
            <InputNumber style={{ width: '100%' }} step={0.5} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
