import API from './api';

const dividendsService = {
  list: (groupId) =>
    API.get(`/groups/${groupId}/dividends`).then((r) => r.data),

  get: (id) =>
    API.get(`/dividends/${id}`).then((r) => r.data),

  payouts: (dividendId) =>
    API.get(`/dividends/${dividendId}/payouts`).then((r) => r.data),

  myDividends: () =>
    API.get('/dividends/my').then((r) => r.data),

  calculate: (groupId, payload) =>
    API.post(`/groups/${groupId}/dividends/calculate`, payload).then((r) => r.data),

  approve: (dividendId) =>
    API.put(`/dividends/${dividendId}/approve`).then((r) => r.data),

  distribute: (dividendId) =>
    API.put(`/dividends/${dividendId}/distribute`).then((r) => r.data),

  confirmPayout: (payoutId, reference) =>
    API.put(`/dividend-payouts/${payoutId}/confirm${reference ? `?reference=${encodeURIComponent(reference)}` : ''}`)
      .then((r) => r.data),
};

export default dividendsService;