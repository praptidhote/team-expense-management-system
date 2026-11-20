import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardService } from '../../services/dashboardService';
import { approvalService } from '../../services/approvalService';
import { StatCard } from '../../components/dashboard/StatCard';
import { Card } from '../../components/common/Card';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Button } from '../../components/common/Button';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { ApprovalReviewModal } from './ApprovalReviewModal';
import { formatCurrency, formatDate } from '../../utils/formatters';
import {
  Clock,
  CheckCircle2,
  XCircle,
  Users,
  CheckSquare,
  ArrowRight,
  TrendingUp,
} from 'lucide-react';

export const ManagerDashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeReviewExpense, setActiveReviewExpense] = useState(null);

  const loadData = async () => {
    try {
      const stats = await dashboardService.getManagerDashboard();
      setData(stats);
    } catch (err) {
      console.error('Failed to load manager dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleReviewSubmit = async (expenseId, decisionData) => {
    await approvalService.reviewExpense(expenseId, decisionData);
    loadData();
  };

  if (loading) return <LoadingSpinner text="Loading manager metrics..." />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            Team Management Dashboard
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Review pending direct report expense requests, monitor team spending, and enforce budgets
          </p>
        </div>
        <Link to="/approvals">
          <Button variant="primary" icon={CheckSquare}>
            Review Pending Queue ({data.pendingCount})
          </Button>
        </Link>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Action Required"
          value={data.pendingCount}
          subtitle="Pending direct report expenses"
          icon={Clock}
          color="amber"
        />
        <StatCard
          title="Team Total Spend"
          value={formatCurrency(data.totalAmount)}
          subtitle={`${data.totalExpenses} overall team requests`}
          icon={TrendingUp}
          color="blue"
        />
        <StatCard
          title="Approved Requests"
          value={data.approvedCount}
          subtitle="Requests signed off"
          icon={CheckCircle2}
          color="emerald"
        />
        <StatCard
          title="Rejected Requests"
          value={data.rejectedCount}
          subtitle="Requests returned with comments"
          icon={XCircle}
          color="rose"
        />
      </div>

      {/* Pending Action Table */}
      <Card
        title="Pending Approval Requests"
        subtitle="Expenses awaiting your managerial review"
        action={
          <Link
            to="/approvals"
            className="text-xs font-semibold text-brand-600 hover:text-brand-800 flex items-center gap-1"
          >
            View all pending <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        }
      >
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
              <tr>
                <th className="px-4 py-3">Employee</th>
                <th className="px-4 py-3">Expense Details</th>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Amount</th>
                <th className="px-4 py-3 text-right">Quick Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.recentExpenses.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-center py-8 text-xs text-slate-400">
                    🎉 Inbox Zero! No pending approvals for your direct reports.
                  </td>
                </tr>
              ) : (
                data.recentExpenses.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-4">
                      <p className="font-semibold text-slate-900">{exp.submitterName}</p>
                      <p className="text-xs text-slate-400">{exp.submitterEmail}</p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="font-semibold text-slate-900">{exp.title}</p>
                      <span className="text-[11px] text-slate-500">{exp.categoryName}</span>
                    </td>
                    <td className="px-4 py-4 text-xs text-slate-500">{formatDate(exp.expenseDate)}</td>
                    <td className="px-4 py-4 font-bold text-slate-900">{formatCurrency(exp.amount)}</td>
                    <td className="px-4 py-4 text-right">
                      <Button
                        size="sm"
                        variant="primary"
                        onClick={() => setActiveReviewExpense(exp)}
                      >
                        Review Request
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>

      <ApprovalReviewModal
        isOpen={!!activeReviewExpense}
        expense={activeReviewExpense}
        onClose={() => setActiveReviewExpense(null)}
        onSubmitReview={handleReviewSubmit}
      />
    </div>
  );
};
