import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { expenseService } from '../../services/expenseService';
import { Card } from '../../components/common/Card';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Button } from '../../components/common/Button';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { formatCurrency, formatDate, formatDateTime } from '../../utils/formatters';
import {
  ArrowLeft,
  Calendar,
  CreditCard,
  Tag,
  User,
  Paperclip,
  Download,
  Clock,
  History,
  FileText,
} from 'lucide-react';

export const ExpenseDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [expense, setExpense] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDetails = async () => {
      try {
        const [exp, hist] = await Promise.all([
          expenseService.getExpenseById(id),
          expenseService.getExpenseHistory(id),
        ]);
        setExpense(exp);
        setHistory(hist);
      } catch (err) {
        setError('Failed to load expense details or access was denied.');
      } finally {
        setLoading(false);
      }
    };
    loadDetails();
  }, [id]);

  if (loading) return <LoadingSpinner text="Loading expense breakdown..." />;
  if (error || !expense) {
    return (
      <Card className="text-center p-8">
        <p className="text-sm font-semibold text-rose-600 mb-4">{error || 'Expense not found'}</p>
        <Button variant="secondary" onClick={() => navigate(-1)} icon={ArrowLeft}>
          Back to list
        </Button>
      </Card>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <Button variant="ghost" size="sm" onClick={() => navigate(-1)} icon={ArrowLeft}>
          Back
        </Button>
        <div className="flex items-center gap-3">
          <StatusBadge status={expense.status} />
          <span className="text-xs text-slate-400">ID #{expense.id}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Main Details (2 cols) */}
        <div className="md:col-span-2 space-y-6">
          <Card>
            <div className="border-b border-slate-100 pb-4 mb-4">
              <span className="text-xs font-semibold text-brand-600 uppercase tracking-wider">
                {expense.categoryName}
              </span>
              <h1 className="text-2xl font-bold text-slate-900 mt-1">{expense.title}</h1>
              <p className="text-3xl font-extrabold text-slate-900 mt-2">
                {formatCurrency(expense.amount, expense.currency)}
              </p>
            </div>

            <div className="space-y-4">
              <div>
                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                  Business Purpose
                </h4>
                <p className="text-sm text-slate-700 whitespace-pre-line leading-relaxed">
                  {expense.description}
                </p>
              </div>

              {expense.notes && (
                <div>
                  <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                    Submitter Notes
                  </h4>
                  <p className="text-xs text-slate-600 bg-slate-50 p-3 rounded-lg border border-slate-100">
                    {expense.notes}
                  </p>
                </div>
              )}

              {/* Receipt File Preview / Download */}
              <div className="pt-4 border-t border-slate-100">
                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                  Attached Receipt
                </h4>
                {expense.receiptUrl ? (
                  <div className="flex items-center justify-between p-3.5 rounded-xl border border-slate-200 bg-slate-50">
                    <div className="flex items-center gap-2.5 truncate">
                      <Paperclip className="w-4 h-4 text-slate-400 shrink-0" />
                      <div className="truncate">
                        <p className="text-xs font-medium text-slate-800 truncate">
                          {expense.receiptOriginalName || 'Expense_Receipt'}
                        </p>
                        <p className="text-[10px] text-slate-400">
                          {expense.receiptContentType || 'Attachment'}
                        </p>
                      </div>
                    </div>
                    <a
                      href={expenseService.getReceiptUrl(expense.id)}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="shrink-0"
                    >
                      <Button variant="secondary" size="sm" icon={Download}>
                        View Receipt
                      </Button>
                    </a>
                  </div>
                ) : (
                  <p className="text-xs text-slate-400 italic">No receipt file uploaded</p>
                )}
              </div>
            </div>
          </Card>

          {/* Chronological Status Audit Trail */}
          <Card title="Approval Audit Trail" subtitle="History of reviews and status modifications">
            <div className="relative pl-6 space-y-6 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200">
              {history.map((h, idx) => (
                <div key={h.id || idx} className="relative">
                  <div className="absolute -left-6 top-1 h-3.5 w-3.5 rounded-full border-2 border-white bg-brand-600 shadow-xs" />
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-800">
                        {h.newStatus}
                      </span>
                      <span className="text-[10px] text-slate-400">
                        • {formatDateTime(h.changedAt)}
                      </span>
                    </div>
                    <p className="text-xs text-slate-600 mt-0.5">
                      Action taken by <span className="font-semibold">{h.changedByUserName}</span>
                    </p>
                    {h.changeReason && (
                      <p className="text-xs text-slate-500 bg-slate-50 p-2.5 rounded-lg border border-slate-100 mt-1.5 italic">
                        "{h.changeReason}"
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>

        {/* Sidebar Info (1 col) */}
        <div className="space-y-4">
          <Card title="Summary Details">
            <div className="space-y-3.5 text-xs">
              <div className="flex items-center justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <User className="w-3.5 h-3.5" /> Submitter
                </span>
                <span className="font-semibold text-slate-800">{expense.submitterName}</span>
              </div>
              <div className="flex items-center justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <Calendar className="w-3.5 h-3.5" /> Expense Date
                </span>
                <span className="font-medium text-slate-800">{formatDate(expense.expenseDate)}</span>
              </div>
              <div className="flex items-center justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <CreditCard className="w-3.5 h-3.5" /> Payment Method
                </span>
                <span className="font-medium text-slate-800">{expense.paymentMethod}</span>
              </div>
              <div className="flex items-center justify-between py-1">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5" /> Submitted At
                </span>
                <span className="text-slate-600">{formatDate(expense.createdAt)}</span>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};
