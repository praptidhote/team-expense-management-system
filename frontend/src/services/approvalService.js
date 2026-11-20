import api from './api';

export const approvalService = {
  getPendingApprovals: async (params = {}) => {
    const res = await api.get('/approvals/pending', { params });
    return res.data;
  },

  reviewExpense: async (id, decisionData) => {
    const res = await api.post(`/approvals/${id}/review`, decisionData);
    return res.data;
  },

  getReviewHistory: async (params = {}) => {
    const res = await api.get('/approvals/history', { params });
    return res.data;
  },
};
