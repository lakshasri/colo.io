import { useState, useCallback } from 'react';
import { Card, List, Tag, Empty, Button, Space } from 'antd';
import { ClockCircleOutlined, ExclamationCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useWebSocket } from '../hooks/useWebSocket';
import api from '../services/api';

const AlertsPanel = () => {
  const [alerts, setAlerts] = useState([]);

  const onMessageReceived = useCallback((data) => {
    if (data.alertId) {
      setAlerts((prev) =>
        [data, ...prev].slice(0, 50) // Keep last 50 alerts
      );
    }
  }, []);

  useWebSocket('/topic/alerts', onMessageReceived);

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return 'red';
      case 'HIGH':
        return 'orange';
      case 'MEDIUM':
        return 'blue';
      case 'LOW':
        return 'green';
      default:
        return 'default';
    }
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'HEALTH':
        return <ExclamationCircleOutlined />;
      case 'POWER':
        return <ExclamationCircleOutlined />;
      case 'CAPACITY':
        return <ExclamationCircleOutlined />;
      default:
        return <ExclamationCircleOutlined />;
    }
  };

  const handleAcknowledge = async (alertId) => {
    try {
      await api.put(`/alerts/${alertId}/acknowledge`);
      setAlerts((prev) => prev.filter((a) => a.alertId !== alertId));
    } catch (err) {
      console.error('Failed to acknowledge alert:', err);
    }
  };

  return (
    <Card
      title="Live Alerts"
      extra={<span>{alerts.length} active</span>}
      style={{ height: '100%' }}
    >
      {alerts.length === 0 ? (
        <Empty description="No active alerts" />
      ) : (
        <List
          dataSource={alerts}
          renderItem={(alert) => (
            <List.Item
              key={alert.alertId}
              extra={
                <Space>
                  <Button
                    type="primary"
                    size="small"
                    onClick={() => handleAcknowledge(alert.alertId)}
                  >
                    Acknowledge
                  </Button>
                </Space>
              }
            >
              <List.Item.Meta
                avatar={getTypeIcon(alert.type)}
                title={
                  <>
                    {alert.message}{' '}
                    <Tag color={getSeverityColor(alert.severity)}>
                      {alert.severity}
                    </Tag>
                  </>
                }
                description={
                  <>
                    <ClockCircleOutlined /> {new Date(alert.createdAt).toLocaleString()}
                  </>
                }
              />
            </List.Item>
          )}
        />
      )}
    </Card>
  );
};

export default AlertsPanel;
