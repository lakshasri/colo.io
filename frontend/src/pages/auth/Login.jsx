import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, Button, Alert } from 'antd'
import { useAuth } from '../../context/AuthContext'

const ROLE_HOME = {
  DC_ADMIN:   '/dashboard/admin',
  TECHNICIAN: '/dashboard/technician',
  CUSTOMER:   '/dashboard/customer',
  MANAGER:    '/dashboard/manager'
}

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const onFinish = async ({ username, password }) => {
    setLoading(true)
    setError(null)
    try {
      const role = await login(username, password)
      navigate(ROLE_HOME[role] ?? '/dashboard')
    } catch (e) {
      setError(e.response?.data?.message ?? 'Invalid credentials')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      background: '#000',
      fontFamily: "'Ubuntu Mono', monospace",
    }}>
      {/* left panel */}
      <div style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '64px 80px',
        borderRight: '1px solid #222',
      }}>
        <img
          src="/logo.png"
          alt="colo.io"
          style={{ width: 120, marginBottom: 48, filter: 'invert(1) grayscale(1)' }}
        />
        <div style={{ color: '#fff', fontSize: 13, lineHeight: 2, opacity: 0.35, letterSpacing: '0.02em' }}>
          <div>// server & rack management system</div>
          <div>// colocation data center platform</div>
          <div>// v2.0 — sprint 6 complete</div>
        </div>

        <div style={{ marginTop: 64, display: 'flex', flexDirection: 'column', gap: 6 }}>
          {[
            ['DC_ADMIN',   'admin / admin123'],
            ['MANAGER',    'manager / admin123'],
            ['TECHNICIAN', 'tech / admin123'],
            ['CUSTOMER',   'customer / admin123'],
          ].map(([role, cred]) => (
            <div key={role} style={{ display: 'flex', gap: 24, color: '#fff', fontSize: 12, opacity: 0.45 }}>
              <span style={{ width: 100, opacity: 0.6 }}>{role}</span>
              <span style={{ fontFamily: 'inherit', letterSpacing: '0.05em' }}>{cred}</span>
            </div>
          ))}
        </div>
      </div>

      {/* right panel — form */}
      <div style={{
        width: 420,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '64px 56px',
        background: '#fff',
      }}>
        <div style={{ marginBottom: 40 }}>
          <div style={{ fontSize: 11, letterSpacing: '0.12em', textTransform: 'uppercase', color: '#999', marginBottom: 8 }}>
            colo.io
          </div>
          <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.01em' }}>
            Sign in
          </div>
        </div>

        {error && (
          <Alert message={error} type="error" showIcon={false}
            style={{ marginBottom: 20, borderRadius: 0, border: '1px solid #000', background: '#f5f5f5', color: '#000', fontFamily: 'inherit' }} />
        )}

        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="username" rules={[{ required: true, message: '' }]}
            style={{ marginBottom: 16 }}>
            <Input
              placeholder="username"
              style={{ fontFamily: 'inherit', fontSize: 14, height: 44, borderColor: '#e0e0e0' }}
            />
          </Form.Item>

          <Form.Item name="password" rules={[{ required: true, message: '' }]}
            style={{ marginBottom: 32 }}>
            <Input.Password
              placeholder="password"
              style={{ fontFamily: 'inherit', fontSize: 14, height: 44, borderColor: '#e0e0e0' }}
            />
          </Form.Item>

          <Button
            type="primary"
            htmlType="submit"
            block
            loading={loading}
            style={{ height: 44, fontSize: 13, letterSpacing: '0.08em', textTransform: 'uppercase' }}
          >
            Sign In →
          </Button>
        </Form>

        <div style={{ marginTop: 40, fontSize: 11, color: '#ccc', letterSpacing: '0.04em' }}>
          SRMS · OOAD Project
        </div>
      </div>
    </div>
  )
}
