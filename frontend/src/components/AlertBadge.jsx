import { Tag } from 'antd'
import { ExclamationCircleOutlined, WarningOutlined, InfoCircleOutlined } from '@ant-design/icons'

const severityConfig = {
  CRITICAL: { color: 'red',    icon: <ExclamationCircleOutlined /> },
  HIGH:     { color: 'orange', icon: <WarningOutlined /> },
  MEDIUM:   { color: 'blue',   icon: <InfoCircleOutlined /> },
  LOW:      { color: 'green',  icon: <InfoCircleOutlined /> },
}

export default function AlertBadge({ severity, style }) {
  const config = severityConfig[severity] || { color: 'default', icon: <InfoCircleOutlined /> }
  return (
    <Tag
      color={config.color}
      icon={config.icon}
      style={{ borderRadius: 0, fontFamily: "'Ubuntu Mono', monospace", fontSize: 11, ...style }}
    >
      {severity}
    </Tag>
  )
}
