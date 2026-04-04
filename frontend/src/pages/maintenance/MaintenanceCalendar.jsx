import React, { useEffect, useState } from 'react'
import { Calendar, Badge, Typography, Spin, Tag } from 'antd'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import api from '../../services/api'

const { Title } = Typography

const PRIORITY_COLOR = { LOW: 'blue', MEDIUM: 'gold', HIGH: 'orange', CRITICAL: 'red' }

export default function MaintenanceCalendar() {
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    setLoading(true)
    api.get('/maintenance').then(r => setTickets(r.data)).finally(() => setLoading(false))
  }, [])

  const getListData = (value) => {
    return tickets.filter(t => {
      const date = t.scheduledAt || t.createdAt
      return date && dayjs(date).isSame(value, 'day')
    })
  }

  const dateCellRender = (value) => {
    const items = getListData(value)
    return (
      <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
        {items.map(ticket => (
          <li key={ticket.ticketId} style={{ cursor: 'pointer' }}
            onClick={() => navigate(`/maintenance/${ticket.ticketId}`)}>
            <Badge
              color={PRIORITY_COLOR[ticket.priority] || 'default'}
              text={<span style={{ fontSize: 11 }}>{ticket.title}</span>}
            />
          </li>
        ))}
      </ul>
    )
  }

  if (loading) return <Spin />

  return (
    <div>
      <Title level={3}>Maintenance Calendar</Title>
      <Calendar cellRender={dateCellRender} />
    </div>
  )
}
