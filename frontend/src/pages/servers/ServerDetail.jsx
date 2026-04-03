import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Row, Col, Card, Statistic, Button, Tag, Typography, Spin, Descriptions } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import api from '../../services/api'
import ServerAllocate from './ServerAllocate'

const { Title, Text } = Typography
const STATUS_COLOR = {
  OPERATIONAL: 'green', FAULTY: 'red',
  MAINTENANCE: 'orange', DECOMMISSIONED: 'default', UNALLOCATED: 'blue'
}

export default function ServerDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [server, setServer] = useState(null)
  const [loading, setLoading] = useState(true)
  const [allocateOpen, setAllocateOpen] = useState(false)

  const fetchServer = async () => {
    try {
      const res = await api.get(`/servers/${id}`)
      setServer(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchServer() }, [id])

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '80px auto' }} />

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/servers')}
              style={{ marginBottom: 16 }}>Back</Button>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
        <Title level={4} style={{ margin: 0 }}>{server?.hostname}</Title>
        <Tag color={STATUS_COLOR[server?.status]}>{server?.status}</Tag>
        {server?.status === 'UNALLOCATED' && (
          <Button type="primary" size="small" onClick={() => setAllocateOpen(true)}>
            Allocate to Rack
          </Button>
        )}
      </div>

      <Descriptions bordered column={2} style={{ marginBottom: 16 }}>
        <Descriptions.Item label="IP Address">{server?.ipAddress || '—'}</Descriptions.Item>
        <Descriptions.Item label="U-Size">{server?.uSize}U</Descriptions.Item>
        <Descriptions.Item label="CPU">{server?.cpuCores} cores</Descriptions.Item>
        <Descriptions.Item label="RAM">{server?.ramGb} GB</Descriptions.Item>
        <Descriptions.Item label="Disk">{server?.diskTb} TB</Descriptions.Item>
        <Descriptions.Item label="Type">{server?.description}</Descriptions.Item>
      </Descriptions>

      {server?.metrics?.available && (
        <Row gutter={[16, 16]}>
          <Col xs={24} md={8}>
            <Card><Statistic title="CPU Usage"
              value={server.metrics.cpuUsagePercent?.toFixed(1)} suffix="%" /></Card>
          </Col>
          <Col xs={24} md={8}>
            <Card><Statistic title="RAM Usage"
              value={server.metrics.ramUsagePercent?.toFixed(1)} suffix="%" /></Card>
          </Col>
          <Col xs={24} md={8}>
            <Card><Statistic title="Disk Usage"
              value={server.metrics.diskUsagePercent?.toFixed(1)} suffix="%" /></Card>
          </Col>
        </Row>
      )}

      <ServerAllocate
        serverId={id}
        open={allocateOpen}
        onClose={() => setAllocateOpen(false)}
        onSuccess={() => { setAllocateOpen(false); fetchServer() }}
      />
    </div>
  )
}
