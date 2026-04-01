import React from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, HddOutlined, AlertOutlined, ToolOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard', label: 'Dashboard', icon: <DashboardOutlined />, path: '/dashboard/customer' },
  { key: 'servers',   label: 'My Servers', icon: <HddOutlined />,     path: '/my-servers' },
  { key: 'alerts',    label: 'Alerts',    icon: <AlertOutlined />,    path: '/alerts' },
  { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />, path: '/maintenance' },
]

export default function CustomerDashboard() {
  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>My Servers</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Allocated Servers" value="—" prefix={<HddOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Active Alerts" value="—" prefix={<AlertOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card><Statistic title="Upcoming Maintenance" value="—" prefix={<ToolOutlined />} /></Card>
        </Col>
      </Row>
    </AppLayout>
  )
}
