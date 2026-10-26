import API from './api';

const announcementsService = {
  list: (groupId) =>
    API.get(`/groups/${groupId}/announcements`).then((r) => r.data),

  get: (id) =>
    API.get(`/announcements/${id}`).then((r) => r.data),

  create: (groupId, payload) =>
    API.post(`/groups/${groupId}/announcements`, payload).then((r) => r.data),

  update: (id, payload) =>
    API.put(`/announcements/${id}`, payload).then((r) => r.data),

  togglePin: (id) =>
    API.put(`/announcements/${id}/pin`).then((r) => r.data),

  delete: (id) =>
    API.delete(`/announcements/${id}`).then((r) => r.data),
};

export default announcementsService;