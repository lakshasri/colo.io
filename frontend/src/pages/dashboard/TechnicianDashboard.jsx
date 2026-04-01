import React from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, ToolOutlined, HddOutlined, AlertOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/technician' },
  { key: 'maintenance', label: 'My Tasks',    icon: <ToolOutlined />,      path: '/maintenance' },
  { key: 'servers',     label: 'Servers',     icon: <HddOutlined />,       path: '/servers' },
  { key: 'alerts',      label: 'Alerts',      icon: <AlertOutlined />,     path: '/alerts' },
]

export default function TechnicianDashboard() {
  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>Technician Overview</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Assigned Tasks" value="—" prefix={<ToolOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Open Alerts" value="—" prefix={<AlertOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Servers Monitored" value="—" prefix={<HddOutlined />} /></Card>
        </Col>
      </Row>
    </AppLayout>
  )
}
