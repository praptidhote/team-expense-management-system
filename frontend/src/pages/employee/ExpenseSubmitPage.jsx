import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { expenseService } from '../../services/expenseService';
import { categoryService } from '../../services/categoryService';
import { Card } from '../../components/common/Card';
import { Input } from '../../components/common/Input';
import { Select } from '../../components/common/Select';
import { Button } from '../../components/common/Button';
import { PAYMENT_METHODS } from '../../utils/constants';
import { Upload, Calendar, DollarSign, FileText, CheckCircle2, AlertCircle } from 'lucide-react';

export const ExpenseSubmitPage = () => {
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    categoryId: '',
    amount: '',
    expenseDate: new Date().toISOString().split('T')[0],
    paymentMethod: 'PERSONAL_CREDIT_CARD',
    notes: '',
  });

  const [receiptFile, setReceiptFile] = useState(null);
  const [receiptPreview, setReceiptPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const cats = await categoryService.getActiveCategories();
        setCategories(cats.map((c) => ({ value: c.id, label: c.name })));
        if (cats.length > 0) {
          setFormData((prev) => ({ ...prev, categoryId: cats[0].id }));
        }
      } catch (err) {
        console.error('Failed to load categories', err);
      }
    };
    loadCategories();
  }, []);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('Receipt file size must be less than 5MB');
        return;
      }
      setReceiptFile(file);
      if (file.type.startsWith('image/')) {
        setReceiptPreview(URL.createObjectURL(file));
      } else {
        setReceiptPreview(null);
      }
      setError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (Number(formData.amount) <= 0) {
      setError('Amount must be greater than zero');
      return;
    }

    setLoading(true);
    try {
      const data = new FormData();
      data.append('title', formData.title.trim());
      data.append('description', formData.description.trim());
      data.append('categoryId', formData.categoryId);
      data.append('amount', formData.amount);
      data.append('currency', 'USD');
      data.append('expenseDate', formData.expenseDate);
      data.append('paymentMethod', formData.paymentMethod);
      if (formData.notes) data.append('notes', formData.notes.trim());
      if (receiptFile) data.append('receipt', receiptFile);

      await expenseService.createExpense(data);
      setSuccess('Expense submitted successfully! Forwarded to manager for review.');
      setTimeout(() => navigate('/my-expenses'), 1200);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to submit expense. Please check your inputs.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Submit Expense</h1>
        <p className="text-sm text-slate-500 mt-1">
          Complete the form below and attach an itemized receipt for company reimbursement
        </p>
      </div>

      <Card>
        {error && (
          <div className="mb-5 flex items-center gap-2 p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="mb-5 flex items-center gap-2 p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-medium">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{success}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <Input
            label="Expense Title"
            name="title"
            placeholder="e.g., Client Dinner at Downtown Bistro"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            required
            helperText="Clear and concise summary of the transaction"
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Select
              label="Expense Category"
              name="categoryId"
              options={categories}
              value={formData.categoryId}
              onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
              required
            />

            <Select
              label="Payment Method"
              name="paymentMethod"
              options={PAYMENT_METHODS}
              value={formData.paymentMethod}
              onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Amount ($)"
              name="amount"
              type="number"
              step="0.01"
              min="0.01"
              placeholder="0.00"
              icon={DollarSign}
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              required
            />

            <Input
              label="Expense Date"
              name="expenseDate"
              type="date"
              max={new Date().toISOString().split('T')[0]}
              icon={Calendar}
              value={formData.expenseDate}
              onChange={(e) => setFormData({ ...formData, expenseDate: e.target.value })}
              required
              helperText="Cannot be in the future"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Detailed Business Justification <span className="text-rose-500">*</span>
            </label>
            <textarea
              name="description"
              rows={3}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              required
              placeholder="State the business objective, attendees, or context for this expense..."
              className="block w-full rounded-lg border border-slate-300 text-sm p-3 focus:outline-none focus:ring-2 focus:border-brand-500 focus:ring-brand-200"
            />
          </div>

          {/* Receipt Upload Section */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Receipt Attachment (Optional but recommended)
            </label>
            <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-slate-300 border-dashed rounded-xl hover:border-brand-400 transition-colors bg-slate-50/50">
              <div className="space-y-2 text-center">
                {receiptPreview ? (
                  <div className="flex flex-col items-center">
                    <img
                      src={receiptPreview}
                      alt="Preview"
                      className="h-28 w-auto rounded-lg object-contain shadow-xs border border-slate-200 mb-2"
                    />
                    <span className="text-xs text-emerald-600 font-semibold flex items-center gap-1">
                      <CheckCircle2 className="w-3.5 h-3.5" /> {receiptFile?.name}
                    </span>
                  </div>
                ) : (
                  <>
                    <Upload className="mx-auto h-10 w-10 text-slate-400" />
                    <div className="flex text-sm text-slate-600 justify-center">
                      <label
                        htmlFor="receipt-upload"
                        className="relative cursor-pointer font-semibold text-brand-600 hover:text-brand-500 focus-within:outline-none"
                      >
                        <span>Upload a file</span>
                        <input
                          id="receipt-upload"
                          name="receipt"
                          type="file"
                          accept=".jpg,.jpeg,.png,.pdf"
                          className="sr-only"
                          onChange={handleFileChange}
                        />
                      </label>
                      <p className="pl-1">or drag and drop</p>
                    </div>
                    <p className="text-xs text-slate-400">PNG, JPG, PDF up to 5MB</p>
                    {receiptFile && (
                      <p className="text-xs font-semibold text-brand-700">
                        Selected: {receiptFile.name}
                      </p>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Additional Notes (Optional)
            </label>
            <input
              name="notes"
              type="text"
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              placeholder="e.g., Client billable project code: ACME-2026"
              className="block w-full rounded-lg border border-slate-300 text-sm px-3 py-2 focus:outline-none focus:ring-2 focus:border-brand-500 focus:ring-brand-200"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate('/my-expenses')}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="md"
              loading={loading}
              icon={CheckCircle2}
            >
              Submit Expense
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
