import React from 'react'
import { Row, Col, Card, Statistic, Typography } from 'antd'
import {
  DashboardOutlined, BarChartOutlined, ToolOutlined,
  ClusterOutlined, FileTextOutlined
} from '@ant-design/icons'
import AppLayout from '../../components/AppLayout'

const { Title } = Typography

const menuItems = [
  { key: 'dashboard',   label: 'Dashboard',  icon: <DashboardOutlined />, path: '/dashboard/manager' },
  { key: 'analytics',   label: 'Analytics',  icon: <BarChartOutlined />,  path: '/reports' },
  { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />,     path: '/maintenance' },
  { key: 'capacity',    label: 'Capacity',   icon: <ClusterOutlined />,   path: '/capacity' },
  { key: 'compliance',  label: 'Compliance', icon: <FileTextOutlined />,  path: '/compliance' },
]

export default function ManagerDashboard() {
  return (
    <AppLayout menuItems={menuItems}>
      <Title level={4}>Manager Overview</Title>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Total Zones" value="—" prefix={<ClusterOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Avg Utilization" value="—" suffix="%" prefix={<BarChartOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Pending Approvals" value="—" prefix={<ToolOutlined />} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card><Statistic title="Compliance Reports" value="—" prefix={<FileTextOutlined />} /></Card>
        </Col>
      </Row>
    </AppLayout>
  )
}
