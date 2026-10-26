import API from './api';

const groupsService = {
  list: () =>
    API.get('/groups').then((r) => r.data),

  get: (groupId) =>
    API.get(`/groups/${groupId}`).then((r) => r.data),

  create: (payload) =>
    API.post('/groups', payload).then((r) => r.data),

  join: (invitationCode) =>
    API.post('/groups/join', { invitationCode }).then((r) => r.data),

  leave: (groupId) =>
    API.delete(`/groups/${groupId}/leave`).then((r) => r.data),

  getMembers: (groupId) =>
    API.get(`/groups/${groupId}/members`).then((r) => r.data),

  updateRole: (groupId, memberId, role, reason) =>
    API.put(`/groups/${groupId}/members/${memberId}/role`, { role, reason }).then((r) => r.data),

  removeMember: (groupId, memberId) =>
    API.delete(`/groups/${groupId}/members/${memberId}`).then((r) => r.data),

  deactivate: (groupId) =>
    API.delete(`/groups/${groupId}`).then((r) => r.data),
};

export default groupsService;