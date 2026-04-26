import React, { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, BarChartOutlined, ToolOutlined,
  ClusterOutlined, FileTextOutlined, SafetyCertificateOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'
import api from '../../services/api'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/manager' },
  { key: 'analytics',   label: 'Reports',     icon: <BarChartOutlined />,  path: '/reports' },
  { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />,      path: '/maintenance' },
  { key: 'racks',       label: 'Capacity',    icon: <ClusterOutlined />,   path: '/racks' },
  { key: 'sla',         label: 'SLA',         icon: <SafetyCertificateOutlined />, path: '/sla' },
]

export default function ManagerDashboard() {
  const [kpis, setKpis] = useState({ zones: 0, avgCpu: 0, pendingApprovals: 0, resolved: 0 })

  useEffect(() => {
    Promise.all([
      api.get('/zones').catch(() => ({ data: [] })),
      api.get('/reports/maintenance-history').catch(() => ({ data: {} })),
      api.get('/maintenance/status/PENDING').catch(() => ({ data: [] }))
    ]).then(([z, mh, pending]) => {
      setKpis({
        zones: z.data.length,
        completionRate: mh.data.completionRatePct ?? 0,
        pendingApprovals: pending.data.filter(t => !t.approved).length,
        resolved: mh.data.resolved ?? 0
      })
    }).catch(() => {})
  }, [])

  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>Manager Overview</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Total Zones" value={kpis.zones} prefix={<ClusterOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Completion Rate" value={kpis.completionRate} suffix="%" prefix={<BarChartOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Pending Approvals" value={kpis.pendingApprovals} prefix={<ToolOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Tickets Resolved" value={kpis.resolved} prefix={<FileTextOutlined />} /></Card>
        </Col>
      </Row>
    </AppLayout>
  )
}
