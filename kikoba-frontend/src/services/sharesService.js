import API from './api';

const sharesService = {
  myAll: () =>
    API.get('/shares/my').then((r) => r.data),

  myInGroup: (groupId) =>
    API.get(`/groups/${groupId}/shares/my`).then((r) => r.data),

  groupAll: (groupId) =>
    API.get(`/groups/${groupId}/shares`).then((r) => r.data),

  buy: (groupId, payload) =>
    API.post(`/groups/${groupId}/shares`, payload).then((r) => r.data),

  mySummary: (groupId) =>
    API.get(`/groups/${groupId}/shares/summary/me`).then((r) => r.data),

  groupSummary: (groupId) =>
    API.get(`/groups/${groupId}/shares/summary`).then((r) => r.data),
};

export default sharesService;