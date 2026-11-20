import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { AppLayout } from '../components/layout/AppLayout';

// Pages
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterPage } from '../pages/auth/RegisterPage';
import { EmployeeDashboardPage } from '../pages/employee/EmployeeDashboardPage';
import { ExpenseSubmitPage } from '../pages/employee/ExpenseSubmitPage';
import { MyExpensesPage } from '../pages/employee/MyExpensesPage';
import { ExpenseDetailsPage } from '../pages/employee/ExpenseDetailsPage';
import { ManagerDashboardPage } from '../pages/manager/ManagerDashboardPage';
import { PendingApprovalsPage } from '../pages/manager/PendingApprovalsPage';
import { AdminDashboardPage } from '../pages/admin/AdminDashboardPage';
import { UserManagementPage } from '../pages/admin/UserManagementPage';
import { CategoryManagementPage } from '../pages/admin/CategoryManagementPage';
import { AuditLogsPage } from '../pages/admin/AuditLogsPage';
import { ExpenseReportsPage } from '../pages/reports/ExpenseReportsPage';
import { NotFoundPage } from '../pages/NotFoundPage';

import { ROLES } from '../utils/constants';

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected Routes inside App Shell */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          {/* Default Redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* Employee Routes */}
          <Route path="/dashboard" element={<EmployeeDashboardPage />} />
          <Route path="/submit-expense" element={<ExpenseSubmitPage />} />
          <Route path="/my-expenses" element={<MyExpensesPage />} />
          <Route path="/expenses/:id" element={<ExpenseDetailsPage />} />

          {/* Manager & Admin Routes */}
          <Route element={<ProtectedRoute allowedRoles={[ROLES.MANAGER, ROLES.ADMIN]} />}>
            <Route path="/manager" element={<ManagerDashboardPage />} />
            <Route path="/approvals" element={<PendingApprovalsPage />} />
            <Route path="/reports" element={<ExpenseReportsPage />} />
          </Route>

          {/* Admin Exclusive Routes */}
          <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
            <Route path="/admin" element={<AdminDashboardPage />} />
            <Route path="/admin/users" element={<UserManagementPage />} />
            <Route path="/admin/categories" element={<CategoryManagementPage />} />
            <Route path="/admin/audit-logs" element={<AuditLogsPage />} />
          </Route>
        </Route>
      </Route>

      {/* 404 Fallback */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
