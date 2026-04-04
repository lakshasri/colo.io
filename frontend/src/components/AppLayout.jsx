import React, { useState, useEffect, useRef } from 'react'
import { Layout, Menu, Button, Avatar, Typography, Space, Badge, Tooltip } from 'antd'
import { LogoutOutlined, UserOutlined, BellOutlined } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const { Header, Sider, Content } = Layout
const { Text } = Typography

export default function AppLayout({ menuItems, children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  const [notifCount, setNotifCount] = useState(0)
  const stompRef = useRef(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      onConnect: () => {
        client.subscribe('/topic/maintenance', () => {
          setNotifCount(c => c + 1)
        })
        client.subscribe('/topic/alerts', () => {
          setNotifCount(c => c + 1)
        })
      }
    })
    client.activate()
    stompRef.current = client
    return () => client.deactivate()
  }, [])

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const selectedKey = menuItems.find(item =>
    location.pathname.startsWith(item.path)
  )?.key ?? menuItems[0]?.key

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} theme="dark">
        <div style={{ padding: '16px', textAlign: 'center' }}>
          {!collapsed && (
            <Text strong style={{ color: '#fff', fontSize: 18 }}>colo.io</Text>
          )}
        </div>
        <Menu
          theme="dark"
          selectedKeys={[selectedKey]}
          mode="inline"
          items={menuItems.map(item => ({
            key: item.key,
            icon: item.icon,
            label: item.label,
            onClick: () => navigate(item.path)
          }))}
        />
      </Sider>

      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px',
                         display: 'flex', alignItems: 'center',
                         justifyContent: 'space-between', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
          <Text strong style={{ fontSize: 16 }}>{user?.role?.replace('_', ' ')}</Text>
          <Space>
            <Tooltip title="Notifications">
              <Badge count={notifCount} size="small">
                <Button icon={<BellOutlined />} type="text"
                  onClick={() => setNotifCount(0)} />
              </Badge>
            </Tooltip>
            <Avatar icon={<UserOutlined />} />
            <Text>{user?.username}</Text>
            <Button icon={<LogoutOutlined />} onClick={handleLogout} type="text">
              Logout
            </Button>
          </Space>
        </Header>

        <Content style={{ margin: 24, padding: 24,
                          background: '#fff', borderRadius: 8, minHeight: 360 }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  )
}
