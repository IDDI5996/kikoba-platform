import API from './api';

const meetingsService = {
  list: (groupId) =>
    API.get(`/groups/${groupId}/meetings`).then((r) => r.data),

  upcoming: (groupId) =>
    API.get(`/groups/${groupId}/meetings/upcoming`).then((r) => r.data),

  get: (meetingId) =>
    API.get(`/meetings/${meetingId}`).then((r) => r.data),

  schedule: (groupId, payload) =>
    API.post(`/groups/${groupId}/meetings`, payload).then((r) => r.data),

  start: (meetingId) =>
    API.put(`/meetings/${meetingId}/start`).then((r) => r.data),

  complete: (meetingId, minutes) =>
    API.put(`/meetings/${meetingId}/complete`, { minutes }).then((r) => r.data),

  cancel: (meetingId, reason) =>
    API.put(`/meetings/${meetingId}/cancel?reason=${encodeURIComponent(reason)}`).then((r) => r.data),

  updateMinutes: (meetingId, minutes) =>
    API.put(`/meetings/${meetingId}/minutes`, { minutes }).then((r) => r.data),

  attendance: (meetingId) =>
    API.get(`/meetings/${meetingId}/attendance`).then((r) => r.data),

  markSelf: (meetingId, status) =>
    API.put(`/meetings/${meetingId}/attendance/me?status=${status}`).then((r) => r.data),

  updateAttendance: (meetingId, memberId, status) =>
    API.put(`/meetings/${meetingId}/attendance/${memberId}?status=${status}`).then((r) => r.data),

  join: (meetingId) =>
    API.post(`/meetings/${meetingId}/join`).then((r) => r.data),

  heartbeat: (meetingId) =>
    API.post(`/meetings/${meetingId}/heartbeat`).then((r) => r.data),

  leave: (meetingId) =>
    API.post(`/meetings/${meetingId}/leave`).then((r) => r.data),

  myAttendance: (meetingId) =>
    API.get(`/meetings/${meetingId}/attendance/me`).then((r) => r.data),
};

export default meetingsService;