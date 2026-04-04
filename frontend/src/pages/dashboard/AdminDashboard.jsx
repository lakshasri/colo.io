import React, { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, HddOutlined, ClusterOutlined,
  AlertOutlined, ToolOutlined, TeamOutlined, FileTextOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'
import AlertsPanel from '../../components/AlertsPanel'
import api from '../../services/api'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/admin' },
  { key: 'zones',       label: 'Zones',       icon: <FileTextOutlined />,  path: '/zones' },
  { key: 'racks',       label: 'Racks',       icon: <ClusterOutlined />,   path: '/racks' },
  { key: 'servers',     label: 'Servers',     icon: <HddOutlined />,       path: '/servers' },
  { key: 'alerts',      label: 'Alerts',      icon: <AlertOutlined />,     path: '/alerts' },
  { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />,      path: '/maintenance' },
  { key: 'reports',     label: 'Reports',     icon: <FileTextOutlined />,  path: '/reports' },
  { key: 'users',       label: 'Users',       icon: <TeamOutlined />,      path: '/users' },
]

export default function AdminDashboard() {
  const [counts, setCounts] = useState({ racks: 0, servers: 0, alerts: 0, maintenance: 0 })

  useEffect(() => {
    Promise.all([
      api.get('/racks'),
      api.get('/servers'),
      api.get('/alerts').catch(() => ({ data: [] })),
      api.get('/maintenance/status/OPEN').catch(() => ({ data: [] }))
    ]).then(([r, s, a, m]) => {
      setCounts({ racks: r.data.length, servers: s.data.length, alerts: a.data.length, maintenance: m.data.length })
    }).catch(() => {})
  }, [])

  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>DC Admin Overview</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Total Racks" value={counts.racks} prefix={<ClusterOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Registered Servers" value={counts.servers} prefix={<HddOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Active Alerts" value={counts.alerts} prefix={<AlertOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Open Maintenance" value={counts.maintenance} prefix={<ToolOutlined />} /></Card>
        </Col>
      </Row>
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24}>
          <AlertsPanel />
        </Col>
      </Row>
    </AppLayout>
  )
}
