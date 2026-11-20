import React, { useState, useEffect, useCallback } from 'react';
import { reportService } from '../../services/reportService';
import { categoryService } from '../../services/categoryService';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Select } from '../../components/common/Select';
import { Input } from '../../components/common/Input';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Pagination } from '../../components/common/Pagination';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { formatCurrency, formatDate } from '../../utils/formatters';
import {
  FileSpreadsheet,
  Download,
  Printer,
  Filter,
  DollarSign,
  CheckCircle,
  Clock,
  XCircle,
} from 'lucide-react';

export const ExpenseReportsPage = () => {
  const [expenses, setExpenses] = useState([]);
  const [summary, setSummary] = useState(null);
  const [categories, setCategories] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 20, totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);

  // Filters
  const [categoryId, setCategoryId] = useState('');
  const [status, setStatus] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const loadData = useCallback(async (page = 0) => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 20,
      };
      if (categoryId) params.categoryId = categoryId;
      if (status) params.status = status;
      if (startDate) params.startDate = startDate;
      if (endDate) params.endDate = endDate;

      const [pagedData, summaryData] = await Promise.all([
        reportService.getExpensesReport(params),
        reportService.getReportSummary(params),
      ]);

      setExpenses(pagedData.content);
      setSummary(summaryData);
      setPageInfo({
        page: pagedData.page,
        size: pagedData.size,
        totalPages: pagedData.totalPages,
        totalElements: pagedData.totalElements,
      });
    } catch (err) {
      console.error('Failed to load expense report', err);
    } finally {
      setLoading(false);
    }
  }, [categoryId, status, startDate, endDate]);

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
    loadData(0);
  }, [loadData]);

  const handleExportCsv = async () => {
    setDownloading(true);
    try {
      const params = {};
      if (categoryId) params.categoryId = categoryId;
      if (status) params.status = status;
      if (startDate) params.startDate = startDate;
      if (endDate) params.endDate = endDate;
      await reportService.downloadCsv(params);
    } catch (err) {
      alert('Failed to export CSV report');
    } finally {
      setDownloading(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 print:hidden">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Expense Reports</h1>
          <p className="text-sm text-slate-500 mt-1">
            Filter, summarize, print, and export official financial expense records
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="secondary" icon={Printer} onClick={handlePrint}>
            Print Report
          </Button>
          <Button
            variant="primary"
            icon={Download}
            loading={downloading}
            onClick={handleExportCsv}
          >
            Export to CSV
          </Button>
        </div>
      </div>

      {/* Filter Toolbar (Hidden on Print) */}
      <Card className="print:hidden">
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
          <Select
            placeholder="All Categories"
            options={categories}
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
          />

          <Select
            placeholder="All Statuses"
            options={[
              { value: 'APPROVED', label: 'Approved Only' },
              { value: 'PENDING', label: 'Pending Only' },
              { value: 'REJECTED', label: 'Rejected Only' },
              { value: 'CANCELLED', label: 'Cancelled Only' },
            ]}
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          />

          <Input
            type="date"
            placeholder="From Date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
          />

          <Input
            type="date"
            placeholder="To Date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
          />
        </div>
      </Card>

      {/* Aggregate Financial Summary Cards */}
      {summary && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl bg-white border border-slate-200/80 shadow-xs">
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider block mb-1">
              Total Filtered Volume
            </span>
            <p className="text-xl font-bold text-slate-900">
              {formatCurrency(summary.totalAmount)}
            </p>
            <span className="text-xs text-slate-500 mt-0.5 block">
              {summary.totalCount} records
            </span>
          </div>

          <div className="p-4 rounded-xl bg-white border border-slate-200/80 shadow-xs">
            <span className="text-[11px] font-semibold text-emerald-600 uppercase tracking-wider block mb-1">
              Approved Claims
            </span>
            <p className="text-xl font-bold text-emerald-700">
              {summary.approvedCount}
            </p>
            <span className="text-xs text-slate-500 mt-0.5 block">Signed off</span>
          </div>

          <div className="p-4 rounded-xl bg-white border border-slate-200/80 shadow-xs">
            <span className="text-[11px] font-semibold text-amber-600 uppercase tracking-wider block mb-1">
              Pending Review
            </span>
            <p className="text-xl font-bold text-amber-700">
              {summary.pendingCount}
            </p>
            <span className="text-xs text-slate-500 mt-0.5 block">Awaiting decision</span>
          </div>

          <div className="p-4 rounded-xl bg-white border border-slate-200/80 shadow-xs">
            <span className="text-[11px] font-semibold text-rose-600 uppercase tracking-wider block mb-1">
              Rejected Requests
            </span>
            <p className="text-xl font-bold text-rose-700">
              {summary.rejectedCount}
            </p>
            <span className="text-xs text-slate-500 mt-0.5 block">Declined claims</span>
          </div>
        </div>
      )}

      {/* Printable Report Header */}
      <div className="hidden print:block border-b-2 border-slate-800 pb-4 mb-6">
        <h2 className="text-xl font-bold text-slate-900">Company Expense Report</h2>
        <p className="text-xs text-slate-500">
          Generated on {new Date().toLocaleDateString()} | Filtered by:{' '}
          {status || 'All Statuses'}, Category: {categoryId ? 'Selected' : 'All'}
        </p>
      </div>

      {/* Main Records Table */}
      <Card>
        {loading ? (
          <LoadingSpinner text="Compiling financial report..." />
        ) : expenses.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            No expenses found matching the selected report parameters.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">Submitter</th>
                  <th className="px-4 py-3">Title & Category</th>
                  <th className="px-4 py-3">Expense Date</th>
                  <th className="px-4 py-3">Payment</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {expenses.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-3.5 font-mono text-slate-400">#{exp.id}</td>
                    <td className="px-4 py-3.5">
                      <p className="font-semibold text-slate-800">{exp.submitterName}</p>
                      <p className="text-[10px] text-slate-400">{exp.submitterEmail}</p>
                    </td>
                    <td className="px-4 py-3.5">
                      <p className="font-medium text-slate-800">{exp.title}</p>
                      <span className="text-[10px] text-slate-500">{exp.categoryName}</span>
                    </td>
                    <td className="px-4 py-3.5 text-slate-500">{formatDate(exp.expenseDate)}</td>
                    <td className="px-4 py-3.5 text-slate-500">{exp.paymentMethod}</td>
                    <td className="px-4 py-3.5">
                      <StatusBadge status={exp.status} />
                    </td>
                    <td className="px-4 py-3.5 text-right font-bold text-slate-900">
                      {formatCurrency(exp.amount, exp.currency)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="print:hidden">
          <Pagination
            page={pageInfo.page}
            size={pageInfo.size}
            totalPages={pageInfo.totalPages}
            totalElements={pageInfo.totalElements}
            onPageChange={(p) => loadData(p)}
          />
        </div>
      </Card>
    </div>
  );
};
