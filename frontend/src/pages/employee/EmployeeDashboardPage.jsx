import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardService } from '../../services/dashboardService';
import { StatCard } from '../../components/dashboard/StatCard';
import { MonthlySpendChart } from '../../components/dashboard/MonthlySpendChart';
import { Card } from '../../components/common/Card';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Button } from '../../components/common/Button';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { formatCurrency, formatDate } from '../../utils/formatters';
import {
  Wallet,
  Clock,
  CheckCircle,
  XCircle,
  PlusCircle,
  ArrowRight,
  Receipt,
} from 'lucide-react';

export const EmployeeDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const stats = await dashboardService.getEmployeeDashboard();
        setData(stats);
      } catch (err) {
        console.error('Failed to load employee dashboard', err);
      } finally {
        setLoading(false);
      }
    };
    loadDashboard();
  }, []);

  if (loading) return <LoadingSpinner text="Loading dashboard metrics..." />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            Employee Expense Dashboard
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Track your reimbursement requests, approvals, and personal business spending
          </p>
        </div>
        <Link to="/submit-expense">
          <Button variant="primary" icon={PlusCircle}>
            Submit New Expense
          </Button>
        </Link>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Submitted"
          value={formatCurrency(data.totalAmount)}
          subtitle={`${data.totalExpenses} total requests`}
          icon={Wallet}
          color="blue"
        />
        <StatCard
          title="Pending Approval"
          value={formatCurrency(data.pendingAmount)}
          subtitle={`${data.pendingCount} under manager review`}
          icon={Clock}
          color="amber"
        />
        <StatCard
          title="Approved Spending"
          value={formatCurrency(data.approvedAmount)}
          subtitle={`${data.approvedCount} approved for reimbursement`}
          icon={CheckCircle}
          color="emerald"
        />
        <StatCard
          title="Rejected Amount"
          value={formatCurrency(data.rejectedAmount)}
          subtitle={`${data.rejectedCount} declined requests`}
          icon={XCircle}
          color="rose"
        />
      </div>

      {/* Charts & Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Spending Trend Chart */}
        <div className="lg:col-span-2">
          <Card title="Monthly Spending Trend" subtitle="Past 6 months reimbursement history">
            <MonthlySpendChart data={data.monthlyTrends} />
          </Card>
        </div>

        {/* Quick Action & Info Card */}
        <div>
          <Card title="Policy Reminders" subtitle="Company reimbursement rules">
            <div className="space-y-4 text-xs text-slate-600">
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                <span className="font-semibold text-slate-800 block mb-1">Receipts Required</span>
                All expenses over $25.00 require a readable image or PDF receipt attached.
              </div>
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                <span className="font-semibold text-slate-800 block mb-1">Submission Timeline</span>
                Expenses must be submitted within 30 days of the invoice date.
              </div>
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                <span className="font-semibold text-slate-800 block mb-1">Approval Hierarchy</span>
                Your designated manager reviews requests before accounting processing.
              </div>
            </div>
          </Card>
        </div>
      </div>

      {/* Recent Expenses Table */}
      <Card
        title="Recent Expenses"
        subtitle="Latest 5 submissions"
        action={
          <Link
            to="/my-expenses"
            className="text-xs font-semibold text-brand-600 hover:text-brand-800 flex items-center gap-1"
          >
            View all <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        }
      >
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
              <tr>
                <th className="px-4 py-3">Title</th>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Amount</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.recentExpenses.length === 0 ? (
                <tr>
                  <td colSpan="6" className="text-center py-8 text-xs text-slate-400">
                    No recent expenses found
                  </td>
                </tr>
              ) : (
                data.recentExpenses.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-3.5 font-medium text-slate-900">{exp.title}</td>
                    <td className="px-4 py-3.5 text-xs text-slate-500">{exp.categoryName}</td>
                    <td className="px-4 py-3.5 text-xs text-slate-500">{formatDate(exp.expenseDate)}</td>
                    <td className="px-4 py-3.5 font-semibold text-slate-900">{formatCurrency(exp.amount)}</td>
                    <td className="px-4 py-3.5">
                      <StatusBadge status={exp.status} />
                    </td>
                    <td className="px-4 py-3.5 text-right">
                      <Link
                        to={`/expenses/${exp.id}`}
                        className="text-xs font-medium text-brand-600 hover:text-brand-800"
                      >
                        Details →
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};
