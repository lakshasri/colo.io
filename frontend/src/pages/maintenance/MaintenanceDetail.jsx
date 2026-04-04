import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card, Descriptions, Tag, Button, Select, Space, Typography,
  message, Divider, List, Checkbox, Input, Form
} from 'antd'
import { ArrowLeftOutlined, UndoOutlined, RedoOutlined, PlusOutlined } from '@ant-design/icons'
import api from '../../services/api'

const { Title } = Typography
const PRIORITY_COLOR = { LOW: 'blue', MEDIUM: 'gold', HIGH: 'orange', CRITICAL: 'red' }
const STATUS_COLOR = { OPEN: 'red', PENDING: 'gold', IN_PROGRESS: 'orange', RESOLVED: 'green', CLOSED: 'default', CANCELLED: 'default' }
const STATUS_OPTIONS = ['OPEN', 'PENDING', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED']
  .map(s => ({ value: s, label: s }))

export default function MaintenanceDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [ticket, setTicket] = useState(null)
  const [newStatus, setNewStatus] = useState('')
  const [checklist, setChecklist] = useState([])
  const [newItem, setNewItem] = useState('')
  const [commandHistory, setCommandHistory] = useState([])
  const [canUndo, setCanUndo] = useState(false)
  const [canRedo, setCanRedo] = useState(false)

  const fetchTicket = async () => {
    const res = await api.get(`/maintenance/${id}`)
    setTicket(res.data)
    setNewStatus(res.data.status)
  }

  const fetchChecklist = async () => {
    const res = await api.get(`/maintenance/${id}/checklist`)
    setChecklist(res.data)
  }

  const fetchHistory = async () => {
    const res = await api.get('/maintenance/commands/history')
    setCommandHistory(res.data.history)
    setCanUndo(res.data.canUndo)
    setCanRedo(res.data.canRedo)
  }

  useEffect(() => {
    fetchTicket()
    fetchChecklist()
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

  const addChecklistItem = async () => {
    if (!newItem.trim()) return
    await api.post(`/maintenance/${id}/checklist`, { description: newItem })
    setNewItem('')
    fetchChecklist()
  }

  const tickItem = async (itemId) => {
    const user = JSON.parse(localStorage.getItem('username') || '"unknown"')
    await api.patch(`/maintenance/checklist/${itemId}/tick`, { completedBy: user })
    fetchChecklist()
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

  const handleLifecycle = async (action) => {
    try {
      await api.post(`/maintenance/${id}/${action}`)
      message.success(`${action} successful`)
      fetchTicket()
    } catch {
      message.error(`${action} failed`)
    }
  }

  if (!ticket) return null

  const completedCount = checklist.filter(i => i.completed).length

  return (
    <div>
      <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/maintenance')}
        style={{ marginBottom: 16 }}>Back</Button>

      <Card title={<Title level={4} style={{ margin: 0 }}>Ticket #{ticket.ticketId}</Title>}
        extra={
          <Space>
            <Button onClick={() => handleLifecycle('start')} disabled={ticket.status !== 'PENDING'}>Start</Button>
            <Button onClick={() => handleLifecycle('complete')} type="primary" disabled={ticket.status !== 'IN_PROGRESS'}>Complete</Button>
            <Button onClick={() => handleLifecycle('cancel')} danger disabled={['RESOLVED','CLOSED','CANCELLED'].includes(ticket.status)}>Cancel</Button>
          </Space>
        }>
        <Descriptions bordered column={2}>
          <Descriptions.Item label="Title" span={2}>{ticket.title}</Descriptions.Item>
          <Descriptions.Item label="Priority">
            <Tag color={PRIORITY_COLOR[ticket.priority]}>{ticket.priority}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={STATUS_COLOR[ticket.status]}>{ticket.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Assigned To">{ticket.assignedTo || '—'}</Descriptions.Item>
          <Descriptions.Item label="Approved">{ticket.approved ? 'Yes' : 'No'}</Descriptions.Item>
          <Descriptions.Item label="Scheduled">
            {ticket.scheduledAt ? new Date(ticket.scheduledAt).toLocaleString() : '—'}
          </Descriptions.Item>
          <Descriptions.Item label="Created">
            {new Date(ticket.createdAt).toLocaleString()}
          </Descriptions.Item>
          {ticket.resolvedAt && (
            <Descriptions.Item label="Resolved">
              {new Date(ticket.resolvedAt).toLocaleString()}
            </Descriptions.Item>
          )}
          <Descriptions.Item label="Description" span={2}>{ticket.description || '—'}</Descriptions.Item>
        </Descriptions>

        <Divider />
        <Space>
          <Select value={newStatus} options={STATUS_OPTIONS} onChange={setNewStatus} style={{ width: 160 }} />
          <Button type="primary" onClick={updateStatus}>Update Status</Button>
        </Space>
      </Card>

      <Card title={`Checklist (${completedCount}/${checklist.length} done)`} style={{ marginTop: 16 }}
        extra={
          <Space>
            <Input value={newItem} onChange={e => setNewItem(e.target.value)}
              placeholder="New item" style={{ width: 200 }}
              onPressEnter={addChecklistItem} />
            <Button icon={<PlusOutlined />} onClick={addChecklistItem}>Add</Button>
          </Space>
        }>
        <List dataSource={checklist} rowKey="itemId"
          renderItem={item => (
            <List.Item>
              <Checkbox checked={item.completed} disabled={item.completed}
                onChange={() => tickItem(item.itemId)}>
                <span style={{ textDecoration: item.completed ? 'line-through' : 'none' }}>
                  {item.description}
                </span>
                {item.completedBy && <span style={{ color: '#999', marginLeft: 8 }}>— {item.completedBy}</span>}
              </Checkbox>
            </List.Item>
          )} />
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
