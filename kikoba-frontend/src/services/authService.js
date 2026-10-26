import api from './api';

const authService = {
  login: async (credentials) => {
    const { data } = await api.post('/auth/login', credentials);
    return data;
  },

  register: async (userData) => {
    const { data } = await api.post('/auth/register', userData);
    return data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  forgotPassword: async (phoneNumber) => {
    const { data } = await api.post('/auth/forgot-password', { phoneNumber });
    return data;
  },

  resetPassword: async (phoneNumber, otp, newPassword) => {
    const { data } = await api.post('/auth/reset-password', {
      phoneNumber,
      otp,
      newPassword
    });
    return data;
  },
};

export default authService;