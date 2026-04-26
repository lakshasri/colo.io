import React, { useEffect, useState } from 'react'
import { Table, Tag, Button, Typography, Space, Select, Row, Col, Card, Statistic } from 'antd'
import {
  CheckCircleOutlined, ExclamationCircleOutlined,
  WarningOutlined, InfoCircleOutlined
} from '@ant-design/icons'
import AlertBadge from '../../components/AlertBadge'
import { useWebSocket } from '../../hooks/useWebSocket'
import api from '../../services/api'

const { Title } = Typography

export default function AlertsPage() {
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)
  const [severityFilter, setSeverityFilter] = useState(null)

  const loadAlerts = async () => {
    try {
      const alertsRes = await api.get('/alerts')
      setAlerts(alertsRes.data)
    } catch (err) {
      console.error('Failed to load alerts:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadAlerts() }, [])

  useWebSocket('/topic/alerts', (data) => {
    if (data.alertId) {
      setAlerts(prev => [data, ...prev])
    }
  })

  const handleAcknowledge = async (alertId) => {
    try {
      await api.put(`/alerts/${alertId}/acknowledge`)
      setAlerts(prev => prev.filter(a => a.alertId !== alertId))
    } catch (err) {
      console.error('Failed to acknowledge:', err)
    }
  }

  const filteredAlerts = severityFilter
    ? alerts.filter(a => a.severity === severityFilter)
    : alerts

  const columns = [
    {
      title: 'Severity',
      dataIndex: 'severity',
      key: 'severity',
      width: 120,
      render: (severity) => <AlertBadge severity={severity} />,
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type) => <Tag style={{ borderRadius: 0 }}>{type}</Tag>,
    },
    {
      title: 'Message',
      dataIndex: 'message',
      key: 'message',
    },
    {
      title: 'Source',
      key: 'source',
      width: 150,
      render: (_, record) => record.sourceType ? `${record.sourceType} #${record.sourceId}` : '-',
    },
    {
      title: 'Time',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (ts) => ts ? new Date(ts).toLocaleString() : '-',
    },
    {
      title: 'Action',
      key: 'action',
      width: 120,
      render: (_, record) =>
        !record.acknowledged ? (
          <Button type="primary" size="small" onClick={() => handleAcknowledge(record.alertId)}>
            Acknowledge
          </Button>
        ) : (
          <Tag color="green" icon={<CheckCircleOutlined />} style={{ borderRadius: 0 }}>Done</Tag>
        ),
    },
  ]

  return (
    <>
      <Title level={4}>Alerts</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8} lg={6}>
          <Card>
            <Statistic
              title="Active Alerts"
              value={alerts.filter(a => !a.acknowledged).length}
              prefix={<ExclamationCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} lg={6}>
          <Card>
            <Statistic
              title="Critical"
              value={alerts.filter(a => a.severity === 'CRITICAL' && !a.acknowledged).length}
              prefix={<WarningOutlined />}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} lg={6}>
          <Card>
            <Statistic
              title="High"
              value={alerts.filter(a => a.severity === 'HIGH' && !a.acknowledged).length}
              prefix={<InfoCircleOutlined />}
              valueStyle={{ color: '#d46b08' }}
            />
          </Card>
        </Col>
      </Row>

      <Space style={{ marginBottom: 16 }}>
        <Select
          placeholder="Filter by severity"
          allowClear
          style={{ width: 180 }}
          onChange={setSeverityFilter}
          options={[
            { value: 'CRITICAL', label: 'Critical' },
            { value: 'HIGH', label: 'High' },
            { value: 'MEDIUM', label: 'Medium' },
            { value: 'LOW', label: 'Low' },
          ]}
        />
      </Space>

      <Table
        columns={columns}
        dataSource={filteredAlerts}
        rowKey="alertId"
        loading={loading}
        pagination={{ pageSize: 20 }}
        size="small"
      />
    </>
  )
}
