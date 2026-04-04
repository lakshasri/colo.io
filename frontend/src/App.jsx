import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/auth/Login'
import RoleGuard from './components/RoleGuard'
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

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      <Route path="/dashboard/admin" element={
        <RoleGuard allowedRoles={['DC_ADMIN']}>
          <AdminDashboard />
        </RoleGuard>
      } />

      <Route path="/dashboard/technician" element={
        <RoleGuard allowedRoles={['TECHNICIAN']}>
          <TechnicianDashboard />
        </RoleGuard>
      } />

      <Route path="/dashboard/customer" element={
        <RoleGuard allowedRoles={['CUSTOMER']}>
          <CustomerDashboard />
        </RoleGuard>
      } />

      <Route path="/dashboard/manager" element={
        <RoleGuard allowedRoles={['MANAGER']}>
          <ManagerDashboard />
        </RoleGuard>
      } />

      {/* Inventory routes — DC_ADMIN + TECHNICIAN */}
      <Route path="/zones" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'MANAGER', 'TECHNICIAN']}>
          <ZoneList />
        </RoleGuard>
      } />
      <Route path="/racks" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'TECHNICIAN', 'MANAGER']}>
          <RackList />
        </RoleGuard>
      } />
      <Route path="/racks/:id" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'TECHNICIAN', 'MANAGER']}>
          <RackDetail />
        </RoleGuard>
      } />
      <Route path="/servers" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'TECHNICIAN']}>
          <ServerList />
        </RoleGuard>
      } />
      <Route path="/servers/:id" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'TECHNICIAN', 'CUSTOMER']}>
          <ServerDetail />
        </RoleGuard>
      } />

      <Route path="/maintenance" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'TECHNICIAN']}>
          <MaintenanceList />
        </RoleGuard>
      } />
      <Route path="/maintenance/:id" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'TECHNICIAN']}>
          <MaintenanceDetail />
        </RoleGuard>
      } />
      <Route path="/maintenance/calendar" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'TECHNICIAN', 'MANAGER']}>
          <MaintenanceCalendar />
        </RoleGuard>
      } />
      <Route path="/reports" element={
        <RoleGuard allowedRoles={['DC_ADMIN', 'MANAGER']}>
          <ReportsPage />
        </RoleGuard>
      } />

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
