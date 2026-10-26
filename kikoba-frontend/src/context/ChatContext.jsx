import { createContext, useContext, useEffect, useState, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { useAuth } from './AuthContext';
import chatsService from '../services/chatsService';

const ChatContext = createContext(null);

export const ChatProvider = ({ children }) => {
  const auth = useAuth();
  const user = auth?.user;

  const token =
    auth?.token ||
    auth?.accessToken ||
    auth?.jwt ||
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    localStorage.getItem('access_token');

  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);
  const [activeChat, setActiveChat] = useState(null);
  const [messages, setMessages] = useState({});
  const [chats, setChats] = useState([]);
  const [loadingChats, setLoadingChats] = useState(false);
  const [directory, setDirectory] = useState({ groups: [], members: [] });
  const [loadingDirectory, setLoadingDirectory] = useState(false);
  const [typingUsers, setTypingUsers] = useState({}); // chatId -> Set of names

  const statusSubRef = useRef(null);

  // ---- WebSocket ----
  useEffect(() => {
    if (!user) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const wsUrl = token
      ? `${protocol}//${host}/ws?token=${encodeURIComponent(token)}`
      : `${protocol}//${host}/ws`;

    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      onConnect: () => {
        setConnected(true);
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => console.error('STOMP error', frame),
      onWebSocketError: (event) => console.error('WS error', event),
    });

    client.activate();
    setStompClient(client);

    return () => {
      client.deactivate();
      setConnected(false);
    };
  }, [user, token]);

  // ---- Loads ----
  const loadMyChats = useCallback(async () => {
    setLoadingChats(true);
    try {
      const data = await chatsService.getMyChats();
      setChats(data || []);
    } catch (err) {
      console.error('loadMyChats failed', err);
    } finally {
      setLoadingChats(false);
    }
  }, []);

  const loadDirectory = useCallback(async () => {
    setLoadingDirectory(true);
    try {
      const data = await chatsService.getDirectory();
      setDirectory(data || { groups: [], members: [] });
    } catch (err) {
      console.error('loadDirectory failed', err);
    } finally {
      setLoadingDirectory(false);
    }
  }, []);

  const loadMessages = useCallback(async (chatId, page = 0) => {
    try {
      const data = await chatsService.getMessages(chatId, page, 50);
      const ordered = [...(data.content || [])].reverse();
      setMessages((prev) => ({ ...prev, [chatId]: ordered }));
    } catch (err) {
      console.error('loadMessages failed', err);
    }
  }, []);

  // ---- Subscribe to chat + status ----
  const subscribeToChat = useCallback(
    (chatId) => {
      if (!stompClient || !connected) return () => {};

      const msgSub = stompClient.subscribe(`/topic/chat/${chatId}`, (payload) => {
        const raw = JSON.parse(payload.body);
        const msg = normalizeMessage(raw);

        setMessages((prev) => {
          const existing = prev[chatId] || [];
          if (existing.some((m) => m.id === msg.id)) return prev;
          return { ...prev, [chatId]: [...existing, msg] };
        });

        setChats((prev) =>
          prev.map((c) =>
            c.id === chatId
              ? { ...c, lastMessage: msg.content || '[attachment]', lastMessageAt: msg.createdAt }
              : c
          )
        );

        // Auto mark delivered if not from me
        if (msg.senderId !== user?.id) {
          stompClient.publish({
            destination: '/app/chat.markDelivered',
            body: JSON.stringify({ chatRoomId: chatId, messageId: msg.id }),
          });
          // Auto mark read if this chat is open
          if (activeChat?.id === chatId) {
            stompClient.publish({
              destination: '/app/chat.markRead',
              body: JSON.stringify({ chatRoomId: chatId }),
            });
          }
        }
      });

      const statusSub = stompClient.subscribe(
        `/topic/chat/${chatId}/status`,
        (payload) => {
          const update = JSON.parse(payload.body);
          if (update.status === 'READ' && !update.messageId) {
            setMessages((prev) => {
              const list = prev[chatId] || [];
              return {
                ...prev,
                [chatId]: list.map((m) =>
                  m.senderId === user?.id ? { ...m, status: 'READ' } : m
                ),
              };
            });
          } else if (update.messageId) {
            setMessages((prev) => {
              const list = prev[chatId] || [];
              return {
                ...prev,
                [chatId]: list.map((m) =>
                  m.id === update.messageId ? { ...m, status: update.status } : m
                ),
              };
            });
          }
        }
      );

      statusSubRef.current = statusSub;

      return () => {
        msgSub.unsubscribe();
        statusSub.unsubscribe();
      };
    },
    [stompClient, connected, user, activeChat]
  );

  // ---- Send ----
  const sendMessage = useCallback(
    (chatId, content, options = {}) => {
      if (!stompClient || !connected) return;
      stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify({
          chatRoomId: chatId,
          content,
          iv: options.iv || null,
          messageType: options.messageType || 'TEXT',
          replyToId: options.replyToId || null,
          attachmentUrl: options.attachmentUrl || null,
          attachmentType: options.attachmentType || null,
        }),
      });
    },
    [stompClient, connected]
  );

  // ---- Mark chat as read ----
  const markChatAsRead = useCallback(
    (chatId) => {
      if (!stompClient || !connected) return;
      stompClient.publish({
        destination: '/app/chat.markRead',
        body: JSON.stringify({ chatRoomId: chatId }),
      });
    },
    [stompClient, connected]
  );

  // ---- Private + group ----
  const startPrivateChat = useCallback(async (otherUserId) => {
    const chat = await chatsService.createOrGetPrivateChat(otherUserId);
    setChats((prev) => (prev.some((c) => c.id === chat.id) ? prev : [chat, ...prev]));
    setActiveChat(chat);
    return chat;
  }, []);

  const openGroupChat = useCallback(
    async (kikobaGroupId) => {
      const chat = await chatsService.createOrGetGroupChat(kikobaGroupId);
      setChats((prev) => (prev.some((c) => c.id === chat.id) ? prev : [chat, ...prev]));
      setActiveChat(chat);
      loadDirectory();
      return chat;
    },
    [loadDirectory]
  );

  const value = {
    stompClient,
    connected,
    activeChat,
    setActiveChat,
    messages,
    setMessages,
    chats,
    setChats,
    loadingChats,
    directory,
    loadingDirectory,
    typingUsers,
    loadMyChats,
    loadDirectory,
    loadMessages,
    subscribeToChat,
    sendMessage,
    markChatAsRead,
    startPrivateChat,
    openGroupChat,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
};

function normalizeMessage(raw) {
  return {
    id: raw.id,
    chatRoomId: raw.chatRoomId ?? raw.chatRoom?.id,
    senderId: raw.senderId ?? raw.sender?.id,
    senderName:
      raw.senderName ??
      (raw.sender ? `${raw.sender.firstName} ${raw.sender.lastName}` : 'Unknown'),
    senderAvatarUrl: raw.senderAvatarUrl ?? raw.sender?.profilePictureUrl ?? null,
    content: raw.content,
    iv: raw.iv,
    messageType: raw.messageType,
    replyToId: raw.replyToId ?? null,
    replyToContent: raw.replyToContent ?? null,
    replyToSenderName: raw.replyToSenderName ?? null,
    attachmentUrl: raw.attachmentUrl ?? null,
    attachmentType: raw.attachmentType ?? null,
    status: raw.status ?? 'SENT',
    createdAt: raw.createdAt,
  };
}

export const useChat = () => {
  const ctx = useContext(ChatContext);
  if (!ctx) throw new Error('useChat must be used inside <ChatProvider>');
  return ctx;
};

export default ChatContext;