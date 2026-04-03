import React, { useState } from 'react'
import { Modal, Form, Select, Alert, Typography, Descriptions } from 'antd'
import api from '../../services/api'

const { Text } = Typography

const STRATEGIES = [
  { value: 'FIRST_FIT',        label: 'First Fit — first rack with enough space' },
  { value: 'BEST_FIT',         label: 'Best Fit — minimises wasted U-space' },
  { value: 'POWER_OPTIMIZED',  label: 'Power Optimized — consolidates power load' },
  { value: 'ZONE_AWARE',       label: 'Zone Aware — balances load across zones' }
]

export default function ServerAllocate({ serverId, open, onClose, onSuccess }) {
  const [form] = Form.useForm()
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const onFinish = async ({ strategy }) => {
    setLoading(true)
    setError(null)
    try {
      const res = await api.post(`/servers/${serverId}/allocate`, null, {
        params: { strategy }
      })
      setResult(res.data)
    } catch (e) {
      setError(e.response?.data?.message ?? 'Allocation failed — no rack available')
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setResult(null)
    setError(null)
    form.resetFields()
    onClose()
  }

  return (
    <Modal
      title="Allocate Server to Rack"
      open={open}
      onCancel={handleClose}
      onOk={result ? onSuccess : () => form.submit()}
      okText={result ? 'Done' : 'Allocate'}
      confirmLoading={loading}
      destroyOnClose
    >
      {!result ? (
        <>
          <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
            Select an allocation strategy. The system will find the best rack automatically.
          </Text>
          {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 12 }} />}
          <Form form={form} layout="vertical" onFinish={onFinish}>
            <Form.Item name="strategy" label="Strategy"
                       initialValue="FIRST_FIT" rules={[{ required: true }]}>
              <Select options={STRATEGIES} />
            </Form.Item>
          </Form>
        </>
      ) : (
        <>
          <Alert message="Allocation successful" type="success" showIcon style={{ marginBottom: 16 }} />
          <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="Rack">{result.rackName}</Descriptions.Item>
            <Descriptions.Item label="U-Position">U{result.uPosition}</Descriptions.Item>
            <Descriptions.Item label="Strategy used">{result.strategyUsed}</Descriptions.Item>
          </Descriptions>
        </>
      )}
    </Modal>
  )
}
