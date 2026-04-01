import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/auth/Login'
import RoleGuard from './components/RoleGuard'
import AdminDashboard from './pages/dashboard/AdminDashboard'
import TechnicianDashboard from './pages/dashboard/TechnicianDashboard'
import CustomerDashboard from './pages/dashboard/CustomerDashboard'
import ManagerDashboard from './pages/dashboard/ManagerDashboard'
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

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
