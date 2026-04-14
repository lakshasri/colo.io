import React, { useState, useEffect, useRef } from 'react'
import { Layout, Menu, Button, Avatar, Space, Badge, Tooltip } from 'antd'
import { LogoutOutlined, UserOutlined, BellOutlined, BulbOutlined, BulbFilled } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const { Header, Sider, Content } = Layout

export default function AppLayout({ menuItems, children }) {
  const { user, logout } = useAuth()
  const { isDark, toggle: toggleTheme } = useTheme()
  const navigate = useNavigate()
  const location = useLocation()
  const [notifCount, setNotifCount] = useState(0)
  const stompRef = useRef(null)

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      onConnect: () => {
        client.subscribe('/topic/maintenance', () => setNotifCount(c => c + 1))
        client.subscribe('/topic/alerts',      () => setNotifCount(c => c + 1))
      }
    })
    client.activate()
    stompRef.current = client
    return () => client.deactivate()
  }, [])

  const handleLogout = async () => { await logout(); navigate('/login') }

  const selectedKey = menuItems.find(item =>
    location.pathname.startsWith(item.path)
  )?.key ?? menuItems[0]?.key

  return (
    <Layout style={{ minHeight: '100vh', fontFamily: "'Ubuntu Mono', monospace" }}>
      <Sider
        collapsible={false}
        theme="dark"
        style={{ background: '#000', borderRight: '1px solid #1a1a1a' }}
        width={220}
      >
        {/* logo area */}
        <div style={{
          padding: '24px 20px',
          borderBottom: '1px solid #1a1a1a',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <img src="/logo.png" alt="colo.io"
            style={{ width: 120, height: 120, objectFit: 'contain', filter: 'invert(1) grayscale(1)' }} />
        </div>

        <Menu
          theme="dark"
          selectedKeys={[selectedKey]}
          mode="inline"
          style={{ background: '#000', marginTop: 8 }}
          items={menuItems.map(item => ({
            key: item.key,
            icon: item.icon,
            label: (
              <span style={{ fontFamily: "'Ubuntu Mono', monospace", fontSize: 13, letterSpacing: '0.02em' }}>
                {item.label}
              </span>
            ),
            onClick: () => navigate(item.path)
          }))}
        />
      </Sider>

      <Layout>
        <Header style={{
          background: isDark ? '#141414' : '#fff',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: `1px solid ${isDark ? '#303030' : '#e0e0e0'}`,
          height: 52,
        }}>
          <span style={{
            fontSize: 11,
            letterSpacing: '0.1em',
            textTransform: 'uppercase',
            color: '#999',
            fontFamily: "'Ubuntu Mono', monospace",
          }}>
            {user?.role?.replace('_', ' ')}
          </span>

          <Space size={4}>
            <Tooltip title={isDark ? 'Light mode' : 'Dark mode'}>
              <Button
                icon={isDark ? <BulbFilled /> : <BulbOutlined />}
                type="text"
                size="small"
                onClick={toggleTheme}
                style={{ color: isDark ? '#e0e0e0' : '#666' }}
              />
            </Tooltip>
            <Tooltip title="Alerts">
              <Badge count={notifCount} size="small" color={isDark ? '#fff' : '#000'}>
                <Button
                  icon={<BellOutlined />}
                  type="text"
                  size="small"
                  onClick={() => { setNotifCount(0); navigate('/alerts') }}
                  style={{ color: isDark ? '#e0e0e0' : '#666' }}
                />
              </Badge>
            </Tooltip>
            <Avatar
              size={28}
              icon={<UserOutlined />}
              style={{ background: '#000', color: '#fff', fontSize: 12 }}
            />
            <span style={{ fontSize: 13, color: isDark ? '#e0e0e0' : '#333', fontFamily: "'Ubuntu Mono', monospace" }}>
              {user?.username}
            </span>
            <Button
              icon={<LogoutOutlined />}
              onClick={handleLogout}
              type="text"
              size="small"
              style={{ color: '#999', fontSize: 12 }}
            >
              {'logout'}
            </Button>
          </Space>
        </Header>

        <Content style={{
          margin: 24,
          padding: 24,
          background: isDark ? '#141414' : '#fff',
          minHeight: 360,
          fontFamily: "'Ubuntu Mono', monospace",
        }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  )
}
