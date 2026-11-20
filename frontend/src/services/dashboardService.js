import api from './api';

export const dashboardService = {
  getEmployeeDashboard: async () => {
    const res = await api.get('/dashboard/employee');
    return res.data;
  },

  getManagerDashboard: async () => {
    const res = await api.get('/dashboard/manager');
    return res.data;
  },

  getAdminDashboard: async () => {
    const res = await api.get('/dashboard/admin');
    return res.data;
  },
};
