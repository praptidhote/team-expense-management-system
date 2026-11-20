import React, { useState } from 'react';
import { Modal } from '../../components/common/Modal';
import { Button } from '../../components/common/Button';
import { StatusBadge } from '../../components/common/StatusBadge';
import { formatCurrency, formatDate } from '../../utils/formatters';
import { expenseService } from '../../services/expenseService';
import { CheckCircle2, XCircle, Download, AlertCircle, FileText } from 'lucide-react';

export const ApprovalReviewModal = ({ expense, isOpen, onClose, onSubmitReview }) => {
  const [decision, setDecision] = useState('APPROVED');
  const [comments, setComments] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!expense) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (decision === 'REJECTED' && !comments.trim()) {
      setError('A rejection reason is mandatory when declining an expense request');
      return;
    }

    setLoading(true);
    try {
      await onSubmitReview(expense.id, {
        decision,
        comments: comments.trim(),
      });
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit review');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Review Expense Request" maxWidth="max-w-2xl">
      <form onSubmit={handleSubmit} className="space-y-5">
        {error && (
          <div className="flex items-center gap-2 p-3 rounded-xl bg-rose-50 text-rose-700 text-xs border border-rose-200">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Expense Summary Grid */}
        <div className="bg-slate-50 p-4 rounded-xl border border-slate-200/80 space-y-3">
          <div className="flex items-start justify-between">
            <div>
              <span className="text-[11px] font-bold text-brand-700 uppercase">
                {expense.categoryName}
              </span>
              <h3 className="font-bold text-slate-900 text-base mt-0.5">{expense.title}</h3>
              <p className="text-xs text-slate-500">
                Submitted by <strong className="text-slate-800">{expense.submitterName}</strong> ({expense.submitterEmail})
              </p>
            </div>
            <p className="text-2xl font-extrabold text-slate-900">
              {formatCurrency(expense.amount, expense.currency)}
            </p>
          </div>

          <div className="pt-2 border-t border-slate-200/60 text-xs text-slate-600">
            <p className="font-semibold text-slate-700 mb-0.5">Business Justification:</p>
            <p className="italic leading-relaxed">{expense.description}</p>
          </div>

          <div className="flex items-center justify-between pt-2 border-t border-slate-200/60 text-xs text-slate-500">
            <span>Date: <strong>{formatDate(expense.expenseDate)}</strong></span>
            <span>Payment: <strong>{expense.paymentMethod}</strong></span>
            {expense.receiptUrl && (
              <a
                href={expenseService.getReceiptUrl(expense.id)}
                target="_blank"
                rel="noreferrer"
                className="text-brand-600 font-medium hover:underline flex items-center gap-1"
              >
                <Download className="w-3.5 h-3.5" /> View Receipt
              </a>
            )}
          </div>
        </div>

        {/* Decision Toggle */}
        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-2">
            Your Decision <span className="text-rose-500">*</span>
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => { setDecision('APPROVED'); setError(''); }}
              className={`flex items-center justify-center gap-2 p-3 rounded-xl border text-sm font-semibold transition-all ${
                decision === 'APPROVED'
                  ? 'border-emerald-500 bg-emerald-50 text-emerald-800 ring-2 ring-emerald-400/30'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              }`}
            >
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              Approve Request
            </button>
            <button
              type="button"
              onClick={() => setDecision('REJECTED')}
              className={`flex items-center justify-center gap-2 p-3 rounded-xl border text-sm font-semibold transition-all ${
                decision === 'REJECTED'
                  ? 'border-rose-500 bg-rose-50 text-rose-800 ring-2 ring-rose-400/30'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              }`}
            >
              <XCircle className="w-4 h-4 text-rose-600" />
              Reject Request
            </button>
          </div>
        </div>

        {/* Review Comments */}
        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">
            Review Comments {decision === 'REJECTED' && <span className="text-rose-500">(Required)</span>}
          </label>
          <textarea
            rows={3}
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            placeholder={
              decision === 'REJECTED'
                ? 'Mandatory: Provide clear feedback to the employee on why this expense is rejected...'
                : 'Optional approval notes or cost center allocation instructions...'
            }
            className={`block w-full rounded-xl border text-sm p-3 focus:outline-none focus:ring-2 ${
              decision === 'REJECTED' && !comments.trim()
                ? 'border-rose-300 focus:border-rose-500 focus:ring-rose-200'
                : 'border-slate-300 focus:border-brand-500 focus:ring-brand-200'
            }`}
          />
        </div>

        {/* Modal Actions */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant={decision === 'APPROVED' ? 'success' : 'danger'}
            loading={loading}
          >
            {decision === 'APPROVED' ? 'Confirm Approval' : 'Confirm Rejection'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
