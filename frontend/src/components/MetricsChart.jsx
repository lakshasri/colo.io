import { useEffect, useRef, useState } from 'react';
import { Card, Spin, Empty, Select } from 'antd';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler } from 'chart.js';
import { Line } from 'react-chartjs-2';
import api from '../services/api';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

const MetricsChart = ({ serverId, hostname, refreshInterval = 5000 }) => {
  const [metrics, setMetrics] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedMetric, setSelectedMetric] = useState('cpuUsagePercent');
  const chartRef = useRef(null);

  const fetchMetrics = async () => {
    setLoading(true);
    try {
      const response = await api.get(`/servers/${serverId}/metrics/history`, {
        params: { page: 0, size: 100 },
      });
      setMetrics(response.data.sort((a, b) => new Date(a.recordedAt) - new Date(b.recordedAt)));
    } catch (err) {
      console.error('Failed to fetch metrics:', err);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchMetrics();
    const interval = setInterval(fetchMetrics, refreshInterval);
    return () => clearInterval(interval);
  }, [serverId, refreshInterval]);

  const metricLabel = {
    cpuUsagePercent: 'CPU Usage (%)',
    ramUsagePercent: 'RAM Usage (%)',
    diskUsagePercent: 'Disk Usage (%)',
  };

  const metricColor = {
    cpuUsagePercent: 'rgba(75, 192, 192, 1)',
    ramUsagePercent: 'rgba(54, 162, 235, 1)',
    diskUsagePercent: 'rgba(255, 159, 64, 1)',
  };

  const chartData = {
    labels: metrics.map((m) => new Date(m.recordedAt).toLocaleTimeString()),
    datasets: [
      {
        label: metricLabel[selectedMetric],
        data: metrics.map((m) => m[selectedMetric]),
        borderColor: metricColor[selectedMetric],
        backgroundColor: metricColor[selectedMetric].replace('1)', '0.1)'),
        fill: true,
        tension: 0.4,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
      title: {
        display: true,
        text: `Server Metrics - ${hostname}`,
      },
    },
    scales: {
      y: {
        min: 0,
        max: 100,
        title: {
          display: true,
          text: 'Percentage (%)',
        },
      },
    },
  };

  return (
    <Card
      title="Metrics History"
      extra={
        <Select
          value={selectedMetric}
          onChange={setSelectedMetric}
          options={[
            { label: 'CPU Usage', value: 'cpuUsagePercent' },
            { label: 'RAM Usage', value: 'ramUsagePercent' },
            { label: 'Disk Usage', value: 'diskUsagePercent' },
          ]}
          style={{ width: 150 }}
        />
      }
    >
      <Spin spinning={loading}>
        {metrics.length === 0 ? (
          <Empty description="No metrics available" />
        ) : (
          <Line ref={chartRef} data={chartData} options={chartOptions} />
        )}
      </Spin>
    </Card>
  );
};

export default MetricsChart;
