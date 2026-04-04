import React, { useEffect, useState } from 'react'
import { Tabs, Card, Table, Typography, Statistic, Row, Col, Progress, Spin } from 'antd'
import api from '../../services/api'

const { Title } = Typography

export default function ReportsPage() {
  const [capacity, setCapacity] = useState(null)
  const [utilization, setUtilization] = useState([])
  const [maintenance, setMaintenance] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      api.get('/reports/capacity'),
      api.get('/reports/utilization'),
      api.get('/reports/maintenance-history')
    ]).then(([cap, util, maint]) => {
      setCapacity(cap.data)
      setUtilization(util.data)
      setMaintenance(maint.data)
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 80 }} />

  const zoneColumns = [
    { title: 'Zone', dataIndex: 'zoneName' },
    { title: 'Racks', dataIndex: 'rackCount', width: 80 },
    {
      title: 'U-Space Used',
      render: (_, r) => (
        <Progress percent={r.uUtilizationPct} size="small"
          status={r.uUtilizationPct > 90 ? 'exception' : 'normal'} />
      )
    },
    {
      title: 'Power Used',
      render: (_, r) => (
        <Progress percent={r.powerUtilizationPct} size="small"
          status={r.powerUtilizationPct > 85 ? 'exception' : 'normal'}
          strokeColor={r.powerUtilizationPct > 85 ? '#f5222d' : '#52c41a'} />
      )
    },
    { title: 'Total Power (kW)', dataIndex: 'totalPowerKw', render: v => v?.toFixed(1) }
  ]

  const utilColumns = [
    { title: 'Server', dataIndex: 'hostname' },
    { title: 'Status', dataIndex: 'status' },
    {
      title: 'CPU %',
      dataIndex: 'avgCpuPct',
      render: v => <Progress percent={v} size="small" style={{ minWidth: 80 }} />
    },
    {
      title: 'RAM %',
      dataIndex: 'avgRamPct',
      render: v => <Progress percent={v} size="small" style={{ minWidth: 80 }} />
    },
    {
      title: 'Disk %',
      dataIndex: 'avgDiskPct',
      render: v => <Progress percent={v} size="small" style={{ minWidth: 80 }} />
    }
  ]

  return (
    <div>
      <Title level={3}>Reports & Analytics</Title>
      <Tabs defaultActiveKey="capacity" items={[
        {
          key: 'capacity',
          label: 'Capacity',
          children: capacity && (
            <>
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={6}>
                  <Card><Statistic title="Total Racks" value={capacity.totalRacks} /></Card>
                </Col>
                <Col span={6}>
                  <Card><Statistic title="Total Servers" value={capacity.totalServers} /></Card>
                </Col>
              </Row>
              <Table rowKey="zoneId" dataSource={capacity.zones}
                columns={zoneColumns} pagination={false} />
            </>
          )
        },
        {
          key: 'utilization',
          label: 'Server Utilization',
          children: (
            <Table rowKey="serverId" dataSource={utilization}
              columns={utilColumns} pagination={{ pageSize: 20 }} />
          )
        },
        {
          key: 'maintenance',
          label: 'Maintenance History',
          children: maintenance && (
            <Row gutter={16}>
              <Col span={4}>
                <Card><Statistic title="Total Tickets" value={maintenance.total} /></Card>
              </Col>
              <Col span={4}>
                <Card><Statistic title="Resolved" value={maintenance.resolved} valueStyle={{ color: '#3f8600' }} /></Card>
              </Col>
              <Col span={4}>
                <Card><Statistic title="Cancelled" value={maintenance.cancelled} valueStyle={{ color: '#cf1322' }} /></Card>
              </Col>
              <Col span={4}>
                <Card><Statistic title="In Progress" value={maintenance.inProgress} valueStyle={{ color: '#d48806' }} /></Card>
              </Col>
              <Col span={4}>
                <Card><Statistic title="Completion Rate" value={maintenance.completionRatePct} suffix="%" /></Card>
              </Col>
              <Col span={4}>
                <Card><Statistic title="Avg Resolution (hrs)" value={maintenance.avgResolutionHours} /></Card>
              </Col>
              <Col span={24} style={{ marginTop: 16 }}>
                <Card title="By Priority">
                  <Row gutter={16}>
                    {Object.entries(maintenance.byPriority || {}).map(([priority, count]) => (
                      <Col span={6} key={priority}>
                        <Statistic title={priority} value={count} />
                      </Col>
                    ))}
                  </Row>
                </Card>
              </Col>
            </Row>
          )
        }
      ]} />
    </div>
  )
}
