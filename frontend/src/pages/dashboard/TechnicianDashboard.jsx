import React, { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, ToolOutlined, HddOutlined, AlertOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'
import api from '../../services/api'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/technician' },
  { key: 'maintenance', label: 'My Tasks',    icon: <ToolOutlined />,      path: '/maintenance' },
  { key: 'servers',     label: 'Servers',     icon: <HddOutlined />,       path: '/servers' },
  { key: 'alerts',      label: 'Alerts',      icon: <AlertOutlined />,     path: '/alerts' },
]

export default function TechnicianDashboard() {
  const [kpis, setKpis] = useState({ tasks: 0, alerts: 0, servers: 0, inProgress: 0 })

  useEffect(() => {
    const username = localStorage.getItem('username') || ''
    Promise.all([
      api.get('/maintenance/status/IN_PROGRESS').catch(() => ({ data: [] })),
      api.get('/alerts').catch(() => ({ data: [] })),
      api.get('/servers').catch(() => ({ data: [] })),
      api.get('/maintenance/status/OPEN').catch(() => ({ data: [] }))
    ]).then(([inProg, alerts, servers, open]) => {
      setKpis({
        tasks: open.data.length,
        alerts: alerts.data.length,
        servers: servers.data.length,
        inProgress: inProg.data.length
      })
    }).catch(() => {})
  }, [])

  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>Technician Overview</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Open Tasks" value={kpis.tasks} prefix={<ToolOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="In Progress" value={kpis.inProgress} prefix={<ToolOutlined />} valueStyle={{ color: '#d48806' }} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Open Alerts" value={kpis.alerts} prefix={<AlertOutlined />} valueStyle={{ color: '#cf1322' }} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Servers" value={kpis.servers} prefix={<HddOutlined />} /></Card>
        </Col>
      </Row>
    </AppLayout>
  )
}
