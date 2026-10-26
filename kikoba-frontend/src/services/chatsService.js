import api from './api';

const chatsService = {
  getMyChats: async () => {
    const { data } = await api.get('/chats');
    return data;
  },

  getMessages: async (chatId, page = 0, size = 50) => {
    const { data } = await api.get(`/chats/${chatId}/messages`, {
      params: { page, size },
    });
    return data;
  },

  getDirectory: async () => {
    const { data } = await api.get('/chats/directory');
    return data;
  },

  createOrGetPrivateChat: async (otherUserId) => {
    const { data } = await api.post('/chats/private', { otherUserId });
    return data;
  },

  createOrGetGroupChat: async (kikobaGroupId) => {
    const { data } = await api.post(`/chats/group/${kikobaGroupId}`);
    return data;
  },

    uploadAttachment: async (file) => {
      const formData = new FormData();
      formData.append('file', file);
      const { data } = await api.post('/chats/attachments', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      return data; // { url, type }
    },
};

export default chatsService;