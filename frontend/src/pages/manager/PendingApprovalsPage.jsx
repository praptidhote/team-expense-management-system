import React, { useState, useEffect, useCallback } from 'react';
import { approvalService } from '../../services/approvalService';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Pagination } from '../../components/common/Pagination';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { ApprovalReviewModal } from './ApprovalReviewModal';
import { formatCurrency, formatDate } from '../../utils/formatters';
import { CheckSquare, Eye, CheckCircle2 } from 'lucide-react';

export const PendingApprovalsPage = () => {
  const [pendingList, setPendingList] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [activeExpense, setActiveExpense] = useState(null);

  const fetchPending = useCallback(async (page = 0) => {
    setLoading(true);
    try {
      const data = await approvalService.getPendingApprovals({ page, size: 10 });
      setPendingList(data.content);
      setPageInfo({
        page: data.page,
        size: data.size,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
      });
    } catch (err) {
      console.error('Failed to load pending queue', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPending(0);
  }, [fetchPending]);

  const handleReviewSubmit = async (expenseId, decisionData) => {
    await approvalService.reviewExpense(expenseId, decisionData);
    fetchPending(pageInfo.page);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Pending Approvals Queue</h1>
        <p className="text-sm text-slate-500 mt-1">
          Review, approve, or reject expense submissions from your assigned employees
        </p>
      </div>

      <Card>
        {loading ? (
          <LoadingSpinner text="Fetching approval queue..." />
        ) : pendingList.length === 0 ? (
          <EmptyState
            icon={CheckCircle2}
            title="All caught up!"
            description="There are currently no pending expense requests requiring your review."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
                <tr>
                  <th className="px-4 py-3">Submitter</th>
                  <th className="px-4 py-3">Expense Title</th>
                  <th className="px-4 py-3">Category</th>
                  <th className="px-4 py-3">Expense Date</th>
                  <th className="px-4 py-3">Amount</th>
                  <th className="px-4 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {pendingList.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-4">
                      <p className="font-semibold text-slate-900">{exp.submitterName}</p>
                      <p className="text-xs text-slate-400">{exp.submitterEmail}</p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="font-semibold text-slate-900">{exp.title}</p>
                      <p className="text-xs text-slate-400 line-clamp-1">{exp.description}</p>
                    </td>
                    <td className="px-4 py-4 text-xs font-medium text-slate-600">
                      {exp.categoryName}
                    </td>
                    <td className="px-4 py-4 text-xs text-slate-500">
                      {formatDate(exp.expenseDate)}
                    </td>
                    <td className="px-4 py-4 font-bold text-slate-900">
                      {formatCurrency(exp.amount, exp.currency)}
                    </td>
                    <td className="px-4 py-4 text-right">
                      <Button
                        size="sm"
                        variant="primary"
                        onClick={() => setActiveExpense(exp)}
                        icon={CheckSquare}
                      >
                        Review
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          page={pageInfo.page}
          size={pageInfo.size}
          totalPages={pageInfo.totalPages}
          totalElements={pageInfo.totalElements}
          onPageChange={(p) => fetchPending(p)}
        />
      </Card>

      <ApprovalReviewModal
        isOpen={!!activeExpense}
        expense={activeExpense}
        onClose={() => setActiveExpense(null)}
        onSubmitReview={handleReviewSubmit}
      />
    </div>
  );
};
