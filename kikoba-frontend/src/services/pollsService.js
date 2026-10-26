import API from './api';

const pollsService = {
  list: (groupId) =>
    API.get(`/groups/${groupId}/polls`).then((r) => r.data),

  active: (groupId) =>
    API.get(`/groups/${groupId}/polls/active`).then((r) => r.data),

  get: (pollId) =>
    API.get(`/polls/${pollId}`).then((r) => r.data),

  create: (groupId, payload) =>
    API.post(`/groups/${groupId}/polls`, payload).then((r) => r.data),

  vote: (pollId, choice) =>
    API.post(`/polls/${pollId}/vote`, { choice }).then((r) => r.data),

  close: (pollId) =>
    API.put(`/polls/${pollId}/close`).then((r) => r.data),

  cancel: (pollId) =>
    API.put(`/polls/${pollId}/cancel`).then((r) => r.data),

  myVotes: (pollId) =>
    API.get(`/polls/${pollId}/my-votes`).then((r) => r.data),
};

export default pollsService;