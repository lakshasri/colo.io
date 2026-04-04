import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Tag, Button, Select, Space, Typography, message, Divider, List } from 'antd'
import { ArrowLeftOutlined, UndoOutlined, RedoOutlined } from '@ant-design/icons'
import api from '../../services/api'

const { Title } = Typography
const PRIORITY_COLOR = { LOW: 'blue', MEDIUM: 'gold', HIGH: 'orange', CRITICAL: 'red' }
const STATUS_COLOR = { OPEN: 'red', IN_PROGRESS: 'orange', RESOLVED: 'green', CLOSED: 'default' }
const STATUS_OPTIONS = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map(s => ({ value: s, label: s }))

export default function MaintenanceDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [ticket, setTicket] = useState(null)
  const [newStatus, setNewStatus] = useState('')
  const [commandHistory, setCommandHistory] = useState([])
  const [canUndo, setCanUndo] = useState(false)
  const [canRedo, setCanRedo] = useState(false)

  const fetchTicket = async () => {
    const res = await api.get(`/maintenance/${id}`)
    setTicket(res.data)
    setNewStatus(res.data.status)
  }

  const fetchHistory = async () => {
    const res = await api.get('/maintenance/commands/history')
    setCommandHistory(res.data.history)
    setCanUndo(res.data.canUndo)
    setCanRedo(res.data.canRedo)
  }

  useEffect(() => {
    fetchTicket()
    fetchHistory()
  }, [id])

  const updateStatus = async () => {
    try {
      await api.patch(`/maintenance/${id}/status`, { status: newStatus })
      message.success('Status updated')
      fetchTicket()
    } catch {
      message.error('Update failed')
    }
  }

  const handleUndo = async () => {
    await api.post('/maintenance/commands/undo')
    message.info('Last command undone')
    fetchHistory()
  }

  const handleRedo = async () => {
    await api.post('/maintenance/commands/redo')
    message.info('Command redone')
    fetchHistory()
  }

  if (!ticket) return null

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/maintenance')}
        style={{ marginBottom: 16 }}>
        Back
      </Button>

      <Card title={<Title level={4} style={{ margin: 0 }}>Ticket #{ticket.ticketId}</Title>}>
        <Descriptions bordered column={2}>
          <Descriptions.Item label="Title" span={2}>{ticket.title}</Descriptions.Item>
          <Descriptions.Item label="Priority">
            <Tag color={PRIORITY_COLOR[ticket.priority]}>{ticket.priority}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={STATUS_COLOR[ticket.status]}>{ticket.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Assigned To">{ticket.assignedTo || '—'}</Descriptions.Item>
          <Descriptions.Item label="Created">
            {new Date(ticket.createdAt).toLocaleString()}
          </Descriptions.Item>
          {ticket.resolvedAt && (
            <Descriptions.Item label="Resolved">
              {new Date(ticket.resolvedAt).toLocaleString()}
            </Descriptions.Item>
          )}
          <Descriptions.Item label="Description" span={2}>
            {ticket.description || '—'}
          </Descriptions.Item>
        </Descriptions>

        <Divider />
        <Space>
          <Select value={newStatus} options={STATUS_OPTIONS} onChange={setNewStatus}
            style={{ width: 160 }} />
          <Button type="primary" onClick={updateStatus}>Update Status</Button>
        </Space>
      </Card>

      <Card title="Command History" style={{ marginTop: 16 }}
        extra={
          <Space>
            <Button icon={<UndoOutlined />} disabled={!canUndo} onClick={handleUndo}>Undo</Button>
            <Button icon={<RedoOutlined />} disabled={!canRedo} onClick={handleRedo}>Redo</Button>
          </Space>
        }>
        {commandHistory.length === 0
          ? <p style={{ color: '#999' }}>No commands executed yet.</p>
          : <List size="small" dataSource={commandHistory}
              renderItem={item => <List.Item>{item}</List.Item>} />
        }
      </Card>
    </div>
  )
}
