import API from './api';

const contributionsService = {
  make: (groupId, payload) =>
    API.post(`/groups/${groupId}/contributions`, payload).then((r) => r.data),

  myAll: () =>
    API.get('/contributions/my').then((r) => r.data),

  myInGroup: (groupId) =>
    API.get(`/groups/${groupId}/contributions/my`).then((r) => r.data),

  groupAll: (groupId) =>
    API.get(`/groups/${groupId}/contributions`).then((r) => r.data),

  groupPending: (groupId) =>
    API.get(`/groups/${groupId}/contributions/pending`).then((r) => r.data),

  get: (contributionId) =>
    API.get(`/contributions/${contributionId}`).then((r) => r.data),

  confirm: (contributionId, payload) =>
    API.put(`/contributions/${contributionId}/confirm`, payload).then((r) => r.data),

  cancel: (contributionId) =>
    API.delete(`/contributions/${contributionId}`).then((r) => r.data),

  mySummary: (groupId) =>
    API.get(`/groups/${groupId}/contributions/summary/me`).then((r) => r.data),

  groupSummary: (groupId) =>
    API.get(`/groups/${groupId}/contributions/summary`).then((r) => r.data),
};

export default contributionsService;