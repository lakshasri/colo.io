import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/auth/Login'
import RoleGuard from './components/RoleGuard'
import AppShell from './components/AppShell'
import AdminDashboard from './pages/dashboard/AdminDashboard'
import TechnicianDashboard from './pages/dashboard/TechnicianDashboard'
import CustomerDashboard from './pages/dashboard/CustomerDashboard'
import ManagerDashboard from './pages/dashboard/ManagerDashboard'
import ZoneList from './pages/zones/ZoneList'
import RackList from './pages/racks/RackList'
import RackDetail from './pages/racks/RackDetail'
import ServerList from './pages/servers/ServerList'
import ServerDetail from './pages/servers/ServerDetail'
import MaintenanceList from './pages/maintenance/MaintenanceList'
import MaintenanceDetail from './pages/maintenance/MaintenanceDetail'
import MaintenanceCalendar from './pages/maintenance/MaintenanceCalendar'
import ReportsPage from './pages/reports/ReportsPage'
import UserList from './pages/users/UserList'
import SlaList from './pages/sla/SlaList'
import AlertsPage from './pages/alerts/AlertsPage'
import { Result, Button } from 'antd'
import { useNavigate } from 'react-router-dom'

function Unauthorized() {
  const navigate = useNavigate()
  return (
    <Result
      status="403"
      title="403"
      subTitle="You don't have permission to access this page."
      extra={<Button type="primary" onClick={() => navigate(-1)}>Go Back</Button>}
    />
  )
}

const shell = (roles, Page) => (
  <RoleGuard allowedRoles={roles}>
    <AppShell><Page /></AppShell>
  </RoleGuard>
)

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      <Route path="/dashboard/admin"      element={<RoleGuard allowedRoles={['DC_ADMIN']}><AdminDashboard /></RoleGuard>} />
      <Route path="/dashboard/technician" element={<RoleGuard allowedRoles={['TECHNICIAN']}><TechnicianDashboard /></RoleGuard>} />
      <Route path="/dashboard/customer"   element={<RoleGuard allowedRoles={['CUSTOMER']}><CustomerDashboard /></RoleGuard>} />
      <Route path="/dashboard/manager"    element={<RoleGuard allowedRoles={['MANAGER']}><ManagerDashboard /></RoleGuard>} />

      <Route path="/zones"                element={shell(['DC_ADMIN', 'MANAGER', 'TECHNICIAN'], ZoneList)} />
      <Route path="/racks"                element={shell(['DC_ADMIN', 'TECHNICIAN', 'MANAGER'], RackList)} />
      <Route path="/racks/:id"            element={shell(['DC_ADMIN', 'TECHNICIAN', 'MANAGER'], RackDetail)} />
      <Route path="/servers"              element={shell(['DC_ADMIN', 'TECHNICIAN'], ServerList)} />
      <Route path="/servers/:id"          element={shell(['DC_ADMIN', 'TECHNICIAN', 'CUSTOMER'], ServerDetail)} />
      <Route path="/alerts"               element={shell(['DC_ADMIN', 'TECHNICIAN', 'MANAGER', 'CUSTOMER'], AlertsPage)} />
      <Route path="/maintenance"          element={shell(['DC_ADMIN', 'TECHNICIAN'], MaintenanceList)} />
      <Route path="/maintenance/:id"      element={shell(['DC_ADMIN', 'TECHNICIAN'], MaintenanceDetail)} />
      <Route path="/maintenance/calendar" element={shell(['DC_ADMIN', 'TECHNICIAN', 'MANAGER'], MaintenanceCalendar)} />
      <Route path="/reports"              element={shell(['DC_ADMIN', 'MANAGER'], ReportsPage)} />
      <Route path="/users"                element={shell(['DC_ADMIN'], UserList)} />
      <Route path="/sla"                  element={shell(['DC_ADMIN', 'MANAGER', 'CUSTOMER'], SlaList)} />

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
