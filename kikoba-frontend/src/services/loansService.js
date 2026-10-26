import API from './api';

const loansService = {
  eligibility: (groupId) =>
    API.get(`/groups/${groupId}/loans/eligibility`).then((r) => r.data),

  apply: (groupId, payload) =>
    API.post(`/groups/${groupId}/loans`, payload).then((r) => r.data),

  myAll: () =>
    API.get('/loans/my').then((r) => r.data),

  myInGroup: (groupId) =>
    API.get(`/groups/${groupId}/loans/my`).then((r) => r.data),

  get: (loanId) =>
    API.get(`/loans/${loanId}`).then((r) => r.data),

  groupAll: (groupId) =>
    API.get(`/groups/${groupId}/loans`).then((r) => r.data),

  groupPending: (groupId) =>
    API.get(`/groups/${groupId}/loans/pending`).then((r) => r.data),

  approve: (loanId, payload) =>
    API.put(`/loans/${loanId}/approve`, payload || {}).then((r) => r.data),

  reject: (loanId, reason) =>
    API.put(`/loans/${loanId}/reject`, { reason }).then((r) => r.data),

  disburse: (loanId) =>
    API.put(`/loans/${loanId}/disburse`).then((r) => r.data),

  cancel: (loanId) =>
    API.delete(`/loans/${loanId}`).then((r) => r.data),

  schedule: (loanId) =>
    API.get(`/loans/${loanId}/schedule`).then((r) => r.data),

  repay: (loanId, payload) =>
    API.post(`/loans/${loanId}/repay`, payload).then((r) => r.data),

  groupSummary: (groupId) =>
    API.get(`/groups/${groupId}/loans/summary`).then((r) => r.data),

  groupCapacity: (groupId) =>
    API.get(`/groups/${groupId}/loans/capacity`).then((r) => r.data),
};

export default loansService;