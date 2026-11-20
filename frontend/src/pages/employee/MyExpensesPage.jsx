import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { expenseService } from '../../services/expenseService';
import { categoryService } from '../../services/categoryService';
import { Card } from '../../components/common/Card';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Button } from '../../components/common/Button';
import { Input } from '../../components/common/Input';
import { Select } from '../../components/common/Select';
import { Pagination } from '../../components/common/Pagination';
import { Modal } from '../../components/common/Modal';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { formatCurrency, formatDate } from '../../utils/formatters';
import { PlusCircle, Search, Filter, Ban, Eye, AlertTriangle } from 'lucide-react';

export const MyExpensesPage = () => {
  const [expenses, setExpenses] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 0, totalElements: 0 });
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [status, setStatus] = useState('');

  // Cancel Modal
  const [cancelTarget, setCancelTarget] = useState(null);
  const [cancelling, setCancelling] = useState(false);

  const fetchExpenses = useCallback(async (page = 0) => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 10,
        sort: 'createdAt,desc',
      };
      if (search.trim()) params.search = search.trim();
      if (categoryId) params.categoryId = categoryId;
      if (status) params.status = status;

      const data = await expenseService.getMyExpenses(params);
      setExpenses(data.content);
      setPageInfo({
        page: data.page,
        size: data.size,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
      });
    } catch (err) {
      console.error('Failed to load my expenses', err);
    } finally {
      setLoading(false);
    }
  }, [search, categoryId, status]);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const cats = await categoryService.getActiveCategories();
        setCategories(cats.map((c) => ({ value: c.id, label: c.name })));
      } catch (err) {
        console.error('Failed to load categories', err);
      }
    };
    loadCategories();
  }, []);

  useEffect(() => {
    fetchExpenses(0);
  }, [fetchExpenses]);

  const handleCancelExpense = async () => {
    if (!cancelTarget) return;
    setCancelling(true);
    try {
      await expenseService.cancelExpense(cancelTarget.id);
      setCancelTarget(null);
      fetchExpenses(pageInfo.page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel expense');
    } finally {
      setCancelling(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">My Expenses</h1>
          <p className="text-sm text-slate-500 mt-1">
            Review and track approval status of all your submitted reimbursement requests
          </p>
        </div>
        <Link to="/submit-expense">
          <Button variant="primary" icon={PlusCircle}>
            Submit Expense
          </Button>
        </Link>
      </div>

      {/* Filter Toolbar */}
      <Card>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Input
            placeholder="Search by title or description..."
            icon={Search}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

          <Select
            placeholder="All Categories"
            options={categories}
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
          />

          <Select
            placeholder="All Statuses"
            options={[
              { value: 'PENDING', label: 'Pending' },
              { value: 'APPROVED', label: 'Approved' },
              { value: 'REJECTED', label: 'Rejected' },
              { value: 'CANCELLED', label: 'Cancelled' },
            ]}
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          />
        </div>
      </Card>

      {/* Expenses Table */}
      <Card>
        {loading ? (
          <LoadingSpinner text="Loading expenses..." />
        ) : expenses.length === 0 ? (
          <EmptyState
            title="No expense requests found"
            description="You have not submitted any expenses matching the current filters."
            actionLabel="Submit New Expense"
            onAction={() => window.location.href = '/submit-expense'}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
                <tr>
                  <th className="px-4 py-3">Expense Details</th>
                  <th className="px-4 py-3">Category</th>
                  <th className="px-4 py-3">Date</th>
                  <th className="px-4 py-3">Amount</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {expenses.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-4">
                      <p className="font-semibold text-slate-900">{exp.title}</p>
                      <p className="text-xs text-slate-400 mt-0.5 line-clamp-1">{exp.description}</p>
                    </td>
                    <td className="px-4 py-4 text-xs font-medium text-slate-600">
                      {exp.categoryName}
                    </td>
                    <td className="px-4 py-4 text-xs text-slate-500">
                      {formatDate(exp.expenseDate)}
                    </td>
                    <td className="px-4 py-4 font-bold text-slate-900">
                      {formatCurrency(exp.amount)}
                    </td>
                    <td className="px-4 py-4">
                      <StatusBadge status={exp.status} />
                    </td>
                    <td className="px-4 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Link to={`/expenses/${exp.id}`}>
                          <Button variant="ghost" size="sm" icon={Eye}>
                            Details
                          </Button>
                        </Link>
                        {exp.status === 'PENDING' && (
                          <Button
                            variant="secondary"
                            size="sm"
                            className="text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                            onClick={() => setCancelTarget(exp)}
                            icon={Ban}
                          >
                            Cancel
                          </Button>
                        )}
                      </div>
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
          onPageChange={(newPage) => fetchExpenses(newPage)}
        />
      </Card>

      {/* Confirmation Modal */}
      <Modal
        isOpen={!!cancelTarget}
        onClose={() => setCancelTarget(null)}
        title="Confirm Cancellation"
      >
        <div className="space-y-4">
          <div className="flex items-center gap-3 p-3.5 rounded-xl bg-amber-50 text-amber-800 text-xs border border-amber-200">
            <AlertTriangle className="w-5 h-5 shrink-0 text-amber-600" />
            <span>
              Are you sure you want to cancel the request: <strong>{cancelTarget?.title}</strong>? Once cancelled, it cannot be submitted for manager approval again.
            </span>
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" onClick={() => setCancelTarget(null)}>
              Keep Pending
            </Button>
            <Button
              variant="danger"
              loading={cancelling}
              onClick={handleCancelExpense}
            >
              Yes, Cancel Request
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
