import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { NotificationDropdown } from './NotificationDropdown';
import { LogOut, User as UserIcon, Shield, Menu } from 'lucide-react';

export const Navbar = ({ onToggleSidebar }) => {
  const { user, logout, isAdmin, isManager } = useAuth();

  const getPrimaryRoleBadge = () => {
    if (isAdmin) return { label: 'Admin', color: 'bg-purple-50 text-purple-700 border-purple-200' };
    if (isManager) return { label: 'Manager', color: 'bg-blue-50 text-blue-700 border-blue-200' };
    return { label: 'Employee', color: 'bg-slate-50 text-slate-700 border-slate-200' };
  };

  const badge = getPrimaryRoleBadge();

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between border-b border-slate-200/80 bg-white/95 px-4 sm:px-6 backdrop-blur">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onToggleSidebar}
          className="lg:hidden p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-lg"
        >
          <Menu className="w-5 h-5" />
        </button>
        <div className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-brand-600 flex items-center justify-center text-white font-bold text-base shadow-sm">
            E
          </div>
          <span className="font-bold text-slate-900 tracking-tight text-base sm:text-lg">
            Expensify<span className="text-brand-600">Enterprise</span>
          </span>
        </div>
      </div>

      <div className="flex items-center gap-3 sm:gap-4">
        <NotificationDropdown />

        <div className="h-6 w-px bg-slate-200" />

        <div className="flex items-center gap-3">
          <div className="text-right hidden sm:block">
            <p className="text-xs font-semibold text-slate-800 leading-none">
              {user?.fullName || user?.firstName}
            </p>
            <p className="text-[11px] text-slate-500 mt-1 leading-none">
              {user?.email}
            </p>
          </div>

          <span className={`px-2 py-0.5 text-[11px] font-semibold border rounded-md hidden md:inline-block ${badge.color}`}>
            {badge.label}
          </span>

          <div className="h-9 w-9 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center font-bold text-xs text-brand-700">
            {user?.firstName ? user.firstName[0] : 'U'}
          </div>

          <button
            type="button"
            onClick={logout}
            title="Log Out"
            className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
