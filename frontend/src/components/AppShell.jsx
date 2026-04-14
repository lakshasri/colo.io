import {
  DashboardOutlined, HddOutlined, ClusterOutlined,
  AlertOutlined, ToolOutlined, TeamOutlined, FileTextOutlined
} from '@ant-design/icons'
import AppLayout from './AppLayout'
import { useAuth } from '../context/AuthContext'

const ALL_ITEMS = {
  adminDashboard:   { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/admin' },
  techDashboard:    { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/technician' },
  custDashboard:    { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/customer' },
  mgrDashboard:     { key: 'dashboard',   label: 'Dashboard',   icon: <DashboardOutlined />, path: '/dashboard/manager' },
  zones:            { key: 'zones',       label: 'Zones',       icon: <FileTextOutlined />,  path: '/zones' },
  racks:            { key: 'racks',       label: 'Racks',       icon: <ClusterOutlined />,   path: '/racks' },
  servers:          { key: 'servers',     label: 'Servers',     icon: <HddOutlined />,       path: '/servers' },
  alerts:           { key: 'alerts',      label: 'Alerts',      icon: <AlertOutlined />,     path: '/alerts' },
  maintenance:      { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />,      path: '/maintenance' },
  maintenanceCal:   { key: 'maintenance', label: 'Maintenance', icon: <ToolOutlined />,      path: '/maintenance/calendar' },
  reports:          { key: 'reports',     label: 'Reports',     icon: <FileTextOutlined />,  path: '/reports' },
  users:            { key: 'users',       label: 'Users',       icon: <TeamOutlined />,      path: '/users' },
  sla:              { key: 'sla',         label: 'SLA',         icon: <FileTextOutlined />,  path: '/sla' },
}

const MENU_BY_ROLE = {
  DC_ADMIN: ['adminDashboard','zones','racks','servers','alerts','maintenance','reports','users','sla'],
  TECHNICIAN: ['techDashboard','racks','servers','alerts','maintenance'],
  CUSTOMER: ['custDashboard','alerts','sla'],
  MANAGER: ['mgrDashboard','zones','racks','alerts','maintenanceCal','reports'],
}

export default function AppShell({ children }) {
  const { user } = useAuth()
  const keys = MENU_BY_ROLE[user?.role] || MENU_BY_ROLE.DC_ADMIN
  const menuItems = keys.map(k => ALL_ITEMS[k])
  return <AppLayout menuItems={menuItems}>{children}</AppLayout>
}
