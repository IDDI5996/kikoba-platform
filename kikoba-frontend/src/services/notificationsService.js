import API from './api';

const notificationsService = {
  list: (page = 0, size = 20) =>
    API.get(`/notifications?page=${page}&size=${size}`).then((r) => r.data),

  unread: () =>
    API.get('/notifications/unread').then((r) => r.data),

  unreadCount: () =>
    API.get('/notifications/unread/count').then((r) => r.data),

  summary: () =>
    API.get('/notifications/summary').then((r) => r.data),

  get: (id) =>
    API.get(`/notifications/${id}`).then((r) => r.data),

  markAsRead: (id) =>
    API.put(`/notifications/${id}/read`).then((r) => r.data),

  markAllRead: () =>
    API.put('/notifications/read-all').then((r) => r.data),

  delete: (id) =>
    API.delete(`/notifications/${id}`).then((r) => r.data),

  preferences: () =>
    API.get('/notification-preferences').then((r) => r.data),

  updatePreference: (payload) =>
    API.put('/notification-preferences', payload).then((r) => r.data),

  resetPreferences: () =>
    API.post('/notification-preferences/reset').then((r) => r.data),
};

export default notificationsService;