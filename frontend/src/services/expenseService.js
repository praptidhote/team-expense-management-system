import api from './api';

export const expenseService = {
  createExpense: async (formData) => {
    const res = await api.post('/expenses', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  updateExpense: async (id, formData) => {
    const res = await api.put(`/expenses/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  getMyExpenses: async (params = {}) => {
    const res = await api.get('/expenses/my', { params });
    return res.data;
  },

  getExpenseById: async (id) => {
    const res = await api.get(`/expenses/${id}`);
    return res.data;
  },

  getExpenseHistory: async (id) => {
    const res = await api.get(`/expenses/${id}/history`);
    return res.data;
  },

  cancelExpense: async (id) => {
    const res = await api.patch(`/expenses/${id}/cancel`);
    return res.data;
  },

  getReceiptUrl: (id) => {
    return `/api/expenses/${id}/receipt`;
  },
};
