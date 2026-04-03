import React, { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Row, Col, Card, Statistic, Button, Tag, Typography, Spin, Progress } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import api from '../../services/api'
import { useWebSocket } from '../../hooks/useWebSocket'
import RackVisualizer from '../../components/RackVisualizer'

const { Title, Text } = Typography
const STATUS_COLOR = { ACTIVE: 'green', MAINTENANCE: 'orange', DECOMMISSIONED: 'red' }

export default function RackDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [rack, setRack] = useState(null)
  const [servers, setServers] = useState([])
  const [utilization, setUtilization] = useState(null)
  const [loading, setLoading] = useState(true)

  const onRackUpdate = useCallback((data) => {
    if (data.rackId && parseInt(data.rackId) === parseInt(id)) {
      setUtilization((prev) => ({
        ...prev,
        uSpaceUsed: data.uSpaceUsed || prev?.uSpaceUsed,
        uSpacePercent: data.uSpacePercent || prev?.uSpacePercent,
        powerUsedKw: data.powerUsedKw || prev?.powerUsedKw,
        powerPercent: data.powerPercent || prev?.powerPercent,
      }))
    }
  }, [id])

  useWebSocket(`/topic/rack/${id}`, onRackUpdate)

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [rackRes, serverRes, utilRes] = await Promise.all([
          api.get(`/racks/${id}`),
          api.get(`/servers/rack/${id}`),
          api.get(`/racks/${id}/utilization`)
        ])
        setRack(rackRes.data)
        setServers(serverRes.data)
        setUtilization(utilRes.data)
      } finally {
        setLoading(false)
      }
    }
    fetchAll()
  }, [id])

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '80px auto' }} />

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/racks')}
              style={{ marginBottom: 16 }}>Back</Button>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
        <Title level={4} style={{ margin: 0 }}>{rack?.name}</Title>
        <Tag color={STATUS_COLOR[rack?.status]}>{rack?.status}</Tag>
        <Text type="secondary">{rack?.location}</Text>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card title="U-Space">
            <Statistic
              value={utilization?.uSpaceUsed}
              suffix={`/ ${utilization?.uSpaceTotal}U`}
            />
            <Progress
              percent={utilization?.uSpacePercent || 0}
              status={utilization?.uSpacePercent > 85 ? 'exception' : 'normal'}
              style={{ marginTop: 8 }}
            />
            <div style={{ marginTop: 8 }}>
              <Text type="secondary">{utilization?.uSpacePercent?.toFixed(1)}% used</Text>
            </div>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card title="Power">
            <Statistic
              value={utilization?.powerUsedKw?.toFixed(2)}
              suffix={`/ ${utilization?.powerMaxKw}kW`}
            />
            <Progress
              percent={utilization?.powerPercent || 0}
              status={utilization?.powerPercent > 85 ? 'exception' : 'normal'}
              style={{ marginTop: 8 }}
            />
            <div style={{ marginTop: 8 }}>
              <Text type="secondary">{utilization?.powerPercent?.toFixed(1)}% used</Text>
            </div>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card title="Servers">
            <Statistic value={servers.length} suffix="installed" />
          </Card>
        </Col>
      </Row>

      <Card title="Rack Layout" style={{ marginTop: 16 }}>
        <RackVisualizer rack={rack} servers={servers} />
      </Card>
    </div>
  )
}
