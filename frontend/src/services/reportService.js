import api from './api';

export const reportService = {
  getExpensesReport: async (params = {}) => {
    const res = await api.get('/reports/expenses', { params });
    return res.data;
  },

  getReportSummary: async (params = {}) => {
    const res = await api.get('/reports/summary', { params });
    return res.data;
  },

  downloadCsv: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const token = localStorage.getItem('token');
    const response = await fetch(`/api/reports/export/csv?${query}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) throw new Error('Download failed');

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `expense-report-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  },
};
