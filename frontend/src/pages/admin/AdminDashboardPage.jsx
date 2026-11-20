import React, { useState, useEffect } from 'react';
import { dashboardService } from '../../services/dashboardService';
import { StatCard } from '../../components/dashboard/StatCard';
import { MonthlySpendChart } from '../../components/dashboard/MonthlySpendChart';
import { CategoryPieChart } from '../../components/dashboard/CategoryPieChart';
import { Card } from '../../components/common/Card';
import { StatusBadge } from '../../components/common/StatusBadge';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { formatCurrency, formatDate } from '../../utils/formatters';
import {
  Building2,
  Users,
  CheckCircle,
  Clock,
  XCircle,
  TrendingUp,
} from 'lucide-react';

export const AdminDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadAdminData = async () => {
      try {
        const stats = await dashboardService.getAdminDashboard();
        setData(stats);
      } catch (err) {
        console.error('Failed to load admin dashboard', err);
      } finally {
        setLoading(false);
      }
    };
    loadAdminData();
  }, []);

  if (loading) return <LoadingSpinner text="Aggregating enterprise spending metrics..." />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
          Executive Finance & Admin Overview
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Complete company-wide visibility across all employee reimbursement requests, departmental budgets, and compliance
        </p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Company Approved Spend"
          value={formatCurrency(data.approvedAmount)}
          subtitle={`${data.approvedCount} approved claims`}
          icon={CheckCircle}
          color="emerald"
        />
        <StatCard
          title="Pending In Pipeline"
          value={formatCurrency(data.pendingAmount)}
          subtitle={`${data.pendingCount} awaiting approval`}
          icon={Clock}
          color="amber"
        />
        <StatCard
          title="Total Claims Volume"
          value={formatCurrency(data.totalAmount)}
          subtitle={`${data.totalExpenses} total submissions`}
          icon={TrendingUp}
          color="blue"
        />
        <StatCard
          title="Workforce Size"
          value={`${(data.totalEmployees || 0) + (data.totalManagers || 0)}`}
          subtitle={`${data.totalEmployees || 0} Employees, ${data.totalManagers || 0} Managers`}
          icon={Users}
          color="purple"
        />
      </div>

      {/* Analytics Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="Monthly Corporate Spend Trends" subtitle="Rolling 6-month reimbursement totals">
          <MonthlySpendChart data={data.monthlyTrends} />
        </Card>
        <Card title="Category Spending Distribution" subtitle="Budget allocation by expense category">
          <CategoryPieChart data={data.categoryBreakdown} />
        </Card>
      </div>

      {/* Recent System Expenses */}
      <Card title="Recent Company Transactions" subtitle="Live feed of incoming and reviewed submissions">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
              <tr>
                <th className="px-4 py-3">Submitter</th>
                <th className="px-4 py-3">Expense Details</th>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Amount</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.recentExpenses.map((exp) => (
                <tr key={exp.id} className="hover:bg-slate-50/60 transition-colors">
                  <td className="px-4 py-4">
                    <p className="font-semibold text-slate-900">{exp.submitterName}</p>
                    <p className="text-xs text-slate-400">{exp.submitterEmail}</p>
                  </td>
                  <td className="px-4 py-4 font-medium text-slate-800">{exp.title}</td>
                  <td className="px-4 py-4 text-xs text-slate-500">{exp.categoryName}</td>
                  <td className="px-4 py-4 text-xs text-slate-500">{formatDate(exp.expenseDate)}</td>
                  <td className="px-4 py-4 font-bold text-slate-900">{formatCurrency(exp.amount)}</td>
                  <td className="px-4 py-4">
                    <StatusBadge status={exp.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};
