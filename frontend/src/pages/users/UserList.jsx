import React, { useEffect, useState } from 'react'
import { Table, Button, Tag, Typography, Space, Modal, Form, Input, Select, message, Popconfirm } from 'antd'
import { PlusOutlined, UserDeleteOutlined } from '@ant-design/icons'
import api from '../../services/api'

const { Title } = Typography

const ROLE_COLOR = {
  DC_ADMIN: 'purple', TECHNICIAN: 'blue', CUSTOMER: 'green', MANAGER: 'orange'
}

export default function UserList() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()

  const fetchUsers = async () => {
    setLoading(true)
    try {
      const res = await api.get('/users')
      setUsers(res.data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchUsers() }, [])

  const handleCreate = async (values) => {
    try {
      await api.post('/users', values)
      message.success('User created')
      setModalOpen(false)
      form.resetFields()
      fetchUsers()
    } catch {
      message.error('Failed to create user')
    }
  }

  const handleDeactivate = async (id) => {
    try {
      await api.delete(`/users/${id}`)
      message.success('User deactivated')
      fetchUsers()
    } catch {
      message.error('Failed to deactivate')
    }
  }

  const columns = [
    { title: 'ID', dataIndex: 'userId', width: 70 },
    { title: 'Username', dataIndex: 'username' },
    { title: 'Email', dataIndex: 'email' },
    {
      title: 'Role',
      dataIndex: 'role',
      render: r => <Tag color={ROLE_COLOR[r]}>{r}</Tag>
    },
    {
      title: 'Active',
      dataIndex: 'active',
      render: v => <Tag color={v ? 'green' : 'default'}>{v ? 'Active' : 'Inactive'}</Tag>
    },
    {
      title: 'Actions',
      render: (_, row) => (
        <Popconfirm title="Deactivate this user?" onConfirm={() => handleDeactivate(row.userId)}>
          <Button danger icon={<UserDeleteOutlined />} size="small">Deactivate</Button>
        </Popconfirm>
      )
    }
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <Title level={3} style={{ margin: 0 }}>User Management</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          New User
        </Button>
      </Space>

      <Table rowKey="userId" columns={columns} dataSource={users} loading={loading} />

      <Modal title="Create User" open={modalOpen}
        onCancel={() => setModalOpen(false)} onOk={() => form.submit()}>
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="username" label="Username" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true, min: 8 }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="role" label="Role" rules={[{ required: true }]}>
            <Select options={['DC_ADMIN','TECHNICIAN','CUSTOMER','MANAGER'].map(r => ({ value: r, label: r }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
