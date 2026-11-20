import api from './api';

export const userService = {
  getAllUsers: async (params = {}) => {
    const res = await api.get('/users', { params });
    return res.data;
  },

  getActiveManagers: async () => {
    const res = await api.get('/users/managers');
    return res.data;
  },

  updateUserStatus: async (id, active) => {
    const res = await api.put(`/users/${id}/status`, { active });
    return res.data;
  },

  assignManager: async (id, managerId) => {
    const res = await api.put(`/users/${id}/assign-manager`, { managerId });
    return res.data;
  },
};
