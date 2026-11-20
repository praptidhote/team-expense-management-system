import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  PlusCircle,
  Receipt,
  CheckSquare,
  Users,
  Tags,
  BarChart3,
  ShieldCheck,
  FileSpreadsheet,
  X,
} from 'lucide-react';

export const Sidebar = ({ isOpen, onClose }) => {
  const { isAdmin, isManager } = useAuth();

  const navItemClass = ({ isActive }) =>
    `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
      isActive
        ? 'bg-brand-50 text-brand-700 font-semibold shadow-xs'
        : 'text-slate-600 hover:bg-slate-100/80 hover:text-slate-900'
    }`;

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-xs lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={`fixed top-16 bottom-0 left-0 z-40 w-64 bg-white border-r border-slate-200/80 transition-transform duration-200 ease-in-out lg:translate-x-0 overflow-y-auto ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex flex-col h-full justify-between p-4">
          <div className="space-y-6">
            {/* Employee Section */}
            <div>
              <p className="px-3 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
                Employee Portal
              </p>
              <nav className="mt-2 space-y-1">
                <NavLink to="/dashboard" className={navItemClass} onClick={onClose}>
                  <LayoutDashboard className="w-4 h-4" />
                  My Dashboard
                </NavLink>
                <NavLink to="/submit-expense" className={navItemClass} onClick={onClose}>
                  <PlusCircle className="w-4 h-4" />
                  Submit Expense
                </NavLink>
                <NavLink to="/my-expenses" className={navItemClass} onClick={onClose}>
                  <Receipt className="w-4 h-4" />
                  My Expenses
                </NavLink>
              </nav>
            </div>

            {/* Manager Section */}
            {isManager && (
              <div>
                <p className="px-3 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
                  Management
                </p>
                <nav className="mt-2 space-y-1">
                  <NavLink to="/manager" className={navItemClass} onClick={onClose}>
                    <BarChart3 className="w-4 h-4" />
                    Manager Dashboard
                  </NavLink>
                  <NavLink to="/approvals" className={navItemClass} onClick={onClose}>
                    <CheckSquare className="w-4 h-4" />
                    Pending Approvals
                  </NavLink>
                </nav>
              </div>
            )}

            {/* Admin Section */}
            {isAdmin && (
              <div>
                <p className="px-3 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
                  Administration
                </p>
                <nav className="mt-2 space-y-1">
                  <NavLink to="/admin" className={navItemClass} onClick={onClose}>
                    <ShieldCheck className="w-4 h-4" />
                    Admin Overview
                  </NavLink>
                  <NavLink to="/admin/users" className={navItemClass} onClick={onClose}>
                    <Users className="w-4 h-4" />
                    User Directory
                  </NavLink>
                  <NavLink to="/admin/categories" className={navItemClass} onClick={onClose}>
                    <Tags className="w-4 h-4" />
                    Categories
                  </NavLink>
                  <NavLink to="/admin/audit-logs" className={navItemClass} onClick={onClose}>
                    <FileSpreadsheet className="w-4 h-4" />
                    Audit Logs
                  </NavLink>
                </nav>
              </div>
            )}

            {/* Shared Reports Section */}
            {(isManager || isAdmin) && (
              <div>
                <p className="px-3 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
                  Analytics & Reports
                </p>
                <nav className="mt-2 space-y-1">
                  <NavLink to="/reports" className={navItemClass} onClick={onClose}>
                    <FileSpreadsheet className="w-4 h-4" />
                    Expense Reports
                  </NavLink>
                </nav>
              </div>
            )}
          </div>

          {/* Footer Info */}
          <div className="pt-4 border-t border-slate-100 px-3">
            <p className="text-[11px] text-slate-400 font-medium">Team Expense Management</p>
            <p className="text-[10px] text-slate-400">Production Build v1.0.0</p>
          </div>
        </div>
      </aside>
    </>
  );
};
