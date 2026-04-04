import React, { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, HddOutlined, AlertOutlined, ToolOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'
import api from '../../services/api'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/customer' },
  { key: 'servers',     label: 'My Servers',  icon: <HddOutlined />,       path: '/servers' },
  { key: 'alerts',      label: 'Alerts',      icon: <AlertOutlined />,     path: '/alerts' },
  { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />,      path: '/maintenance' },
]

export default function CustomerDashboard() {
  const [kpis, setKpis] = useState({ servers: 0, alerts: 0, upcoming: 0 })

  useEffect(() => {
    Promise.all([
      api.get('/servers').catch(() => ({ data: [] })),
      api.get('/alerts').catch(() => ({ data: [] })),
      api.get('/maintenance/status/PENDING').catch(() => ({ data: [] }))
    ]).then(([s, a, m]) => {
      setKpis({ servers: s.data.length, alerts: a.data.length, upcoming: m.data.length })
    }).catch(() => {})
  }, [])

  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>My Overview</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Allocated Servers" value={kpis.servers} prefix={<HddOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Active Alerts" value={kpis.alerts} prefix={<AlertOutlined />} valueStyle={{ color: kpis.alerts > 0 ? '#cf1322' : undefined }} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Upcoming Maintenance" value={kpis.upcoming} prefix={<ToolOutlined />} /></Card>
        </Col>
      </Row>
    </AppLayout>
  )
}
