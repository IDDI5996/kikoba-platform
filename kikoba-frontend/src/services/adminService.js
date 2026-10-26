import api from './api';

const adminService = {
  getStats: async () => {
    const { data } = await api.get('/admin/stats');
    return data;
  },

  getPendingApprovals: async () => {
    const { data } = await api.get('/admin/approvals/pending');
    return data;
  },

  approveRequest: async (requestId) => {
    const { data } = await api.post(`/admin/approvals/${requestId}/approve`);
    return data;
  },

  createApprovalRequest: async (actionType, payload) => {
    const { data } = await api.post('/admin/approvals/request', { actionType, payload });
    return data;
  },

  // --- USER MANAGEMENT ---
  getAllUsers: async (search, page = 0, size = 10) => {
    const { data } = await api.get('/admin/users', { params: { search, page, size } });
    return data;
  },

  updateUserRole: async (userId, role) => {
    // If role is empty string, pass null
    const roleParam = role ? `?role=${role}` : '';
    const { data } = await api.put(`/admin/users/${userId}/role${roleParam}`);
    return data;
  },

  toggleUserStatus: async (userId) => {
    const { data } = await api.put(`/admin/users/${userId}/status`);
    return data;
  },

  // --- AUDIT LOGS ---
  getAuditLogs: async (page = 0, size = 20) => {
    const { data } = await api.get('/admin/audit-logs', { params: { page, size } });
    return data;
  },

  // --- SYSTEM SETTINGS ---
  getSystemSettings: async () => {
    const { data } = await api.get('/admin/settings');
    return data;
  },

  updateSystemSettings: async (settings) => {
    const { data } = await api.put('/admin/settings', settings);
    return data;
  },
};

export default adminService;