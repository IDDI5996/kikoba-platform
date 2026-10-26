import API from './api';

const usersService = {
  me: () => API.get('/users/me').then((r) => r.data),

  updateProfile: (payload) =>
    API.put('/users/me', payload).then((r) => r.data),

  uploadAvatar: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return API.post('/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((r) => r.data);
  },

  enableMfa: () =>
    API.post('/users/me/mfa/enable').then((r) => r.data),

  disableMfa: () =>
    API.post('/users/me/mfa/disable').then((r) => r.data),

  submitKyc: (documentUrl) =>
    API.post(`/users/me/kyc?documentUrl=${encodeURIComponent(documentUrl)}`).then((r) => r.data),
};

export default usersService;