import React from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, HddOutlined, ClusterOutlined,
  AlertOutlined, ToolOutlined, TeamOutlined, FileTextOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard', label: 'Dashboard', icon: <DashboardOutlined />, path: '/dashboard/admin' },
  { key: 'racks',     label: 'Racks',     icon: <ClusterOutlined />,  path: '/racks' },
  { key: 'servers',   label: 'Servers',   icon: <HddOutlined />,      path: '/servers' },
  { key: 'alerts',    label: 'Alerts',    icon: <AlertOutlined />,    path: '/alerts' },
  { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />, path: '/maintenance' },
  { key: 'users',     label: 'Users',     icon: <TeamOutlined />,     path: '/users' },
  { key: 'reports',   label: 'Reports',   icon: <FileTextOutlined />, path: '/reports' },
]

export default function AdminDashboard() {
  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>DC Admin Overview</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Total Racks" value="—" prefix={<ClusterOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Active Servers" value="—" prefix={<HddOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Active Alerts" value="—" prefix={<AlertOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Pending Maintenance" value="—" prefix={<ToolOutlined />} /></Card>
        </Col>
      </Row>
    </AppLayout>
  )
}
