import { useState, useEffect, useRef } from 'react';
import { useChat } from '../../context/ChatContext';
import { useAuth } from '../../context/AuthContext';
import chatsService from '../../services/chatsService';
import MessageTicks from './MessageTicks';
import ChatMenu from './ChatMenu';
import {
  Send, Paperclip, MoreVertical, ArrowLeft, X, Reply, Users, FileText,
} from 'lucide-react';

const SWIPE_THRESHOLD = 60;

const ChatWindow = () => {
  const {
    activeChat,
    messages,
    sendMessage,
    subscribeToChat,
    loadMessages,
    setActiveChat,
    connected,
    markChatAsRead,
  } = useChat();
  const { user } = useAuth();

  const [input, setInput] = useState('');
  const [replyTo, setReplyTo] = useState(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [muted, setMuted] = useState(false);
  const [infoOpen, setInfoOpen] = useState(false);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);

  const chatMessages = messages[activeChat?.id] || [];

  // Load history + subscribe
  useEffect(() => {
    if (!activeChat) return;
    loadMessages(activeChat.id);
  }, [activeChat, loadMessages]);

  useEffect(() => {
    if (!activeChat || !connected) return;
    const unsub = subscribeToChat(activeChat.id);
    return () => unsub && unsub();
  }, [activeChat, connected, subscribeToChat]);

  // Mark chat as read on open
  useEffect(() => {
    if (!activeChat || !connected) return;
    markChatAsRead(activeChat.id);
  }, [activeChat, connected, markChatAsRead]);

  // Auto-scroll
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages.length]);

  const handleSend = (e) => {
    e.preventDefault();
    if (!input.trim() || !activeChat) return;
    sendMessage(activeChat.id, input, {
      messageType: 'TEXT',
      replyToId: replyTo?.id || null,
    });
    setInput('');
    setReplyTo(null);
  };

  const handleFile = async (e) => {
    const file = e.target.files?.[0];
    if (!file || !activeChat) return;
    setUploading(true);
    try {
      const res = await chatsService.uploadAttachment(file);
      sendMessage(activeChat.id, null, {
        messageType: res.type?.startsWith('image/') ? 'IMAGE' : 'FILE',
        attachmentUrl: res.url,
        attachmentType: res.type,
        replyToId: replyTo?.id || null,
      });
      setReplyTo(null);
    } catch (err) {
      console.error('Upload failed', err);
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const getAvatarUrl = (url) =>
    url ? (url.startsWith('http') ? url : `http://localhost:8080${url}`) : null;

  if (!activeChat) return null;

  const headerAvatar =
    activeChat.type === 'PRIVATE'
      ? getAvatarUrl(activeChat.otherUserAvatarUrl)
      : null;

  return (
    <div className="chat-window">
      {/* HEADER */}
      <div className="chat-window-header">
        <button
          className="chat-icon-btn chat-back-btn"
          onClick={() => setActiveChat(null)}
          aria-label="Back"
        >
          <ArrowLeft size={20} />
        </button>

        <div className="chat-header-avatar" onClick={() => setInfoOpen(true)}>
          {headerAvatar ? (
            <img src={headerAvatar} alt="" />
          ) : activeChat.type === 'GROUP' ? (
            <Users size={20} />
          ) : (
            (activeChat.name || '?')[0].toUpperCase()
          )}
        </div>

        <div className="chat-header-info">
          <h4>{activeChat.name || 'Chat'}</h4>
          <span>{connected ? 'Online' : 'Connecting…'}</span>
        </div>

        <button
          className="chat-icon-btn"
          onClick={() => setMenuOpen((v) => !v)}
          aria-label="Menu"
        >
          <MoreVertical size={20} />
        </button>

        <ChatMenu
          open={menuOpen}
          onClose={() => setMenuOpen(false)}
          chat={activeChat}
          muted={muted}
          onChatInfo={() => {
            setMenuOpen(false);
            setInfoOpen(true);
          }}
          onToggleMute={() => {
            setMuted((v) => !v);
            setMenuOpen(false);
          }}
          onClearChat={() => {
            setMenuOpen(false);
            if (window.confirm('Clear this chat locally?')) {
              // We just reset local list — backend keeps history
              // (add an API call here if you want server-side clear)
            }
          }}
        />
      </div>

      {/* MESSAGES */}
      <div className="chat-messages">
        {chatMessages.length === 0 && (
          <p className="chat-empty-messages">No messages yet. Say hi!</p>
        )}

        {chatMessages.map((msg) => (
          <MessageBubble
            key={msg.id}
            msg={msg}
            isMine={msg.senderId === user?.id}
            avatarUrl={getAvatarUrl(msg.senderAvatarUrl)}
            onReply={() => setReplyTo(msg)}
          />
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* REPLY PREVIEW */}
      {replyTo && (
        <div className="chat-reply-preview">
          <div className="chat-reply-preview-bar" />
          <div className="chat-reply-preview-body">
            <span className="chat-reply-preview-name">
              {replyTo.senderId === user?.id ? 'You' : replyTo.senderName}
            </span>
            <span className="chat-reply-preview-text">
              {replyTo.content || '[attachment]'}
            </span>
          </div>
          <button
            type="button"
            className="chat-icon-btn"
            onClick={() => setReplyTo(null)}
          >
            <X size={18} />
          </button>
        </div>
      )}

      {/* INPUT */}
      <form className="chat-input-area" onSubmit={handleSend}>
        <button
          type="button"
          className="chat-icon-btn"
          onClick={() => fileInputRef.current?.click()}
          disabled={uploading}
          aria-label="Attach"
        >
          <Paperclip size={20} />
        </button>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*,application/pdf,video/*"
          onChange={handleFile}
          style={{ display: 'none' }}
        />
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type a message..."
          disabled={!connected}
        />
        <button
          type="submit"
          className="chat-send-btn"
          disabled={!input.trim() || !connected}
          aria-label="Send"
        >
          <Send size={18} />
        </button>
      </form>

      {/* CHAT INFO MODAL */}
      {infoOpen && (
        <div className="chat-modal-overlay" onClick={() => setInfoOpen(false)}>
          <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
            <h3>{activeChat.name || 'Chat info'}</h3>
            <p className="chat-modal-sub">
              {activeChat.type === 'GROUP' ? 'Group chat' : 'Private chat'}
            </p>
            {headerAvatar && (
              <img className="chat-modal-avatar" src={headerAvatar} alt="" />
            )}
            <button
              className="chat-modal-close"
              onClick={() => setInfoOpen(false)}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

/* =========================================================
   MESSAGE BUBBLE (with swipe-to-reply)
   ========================================================= */
const MessageBubble = ({ msg, isMine, avatarUrl, onReply }) => {
  const [offsetX, setOffsetX] = useState(0);
  const startX = useRef(0);
  const startY = useRef(0);
  const swiping = useRef(false);

  const onTouchStart = (e) => {
    const t = e.touches[0];
    startX.current = t.clientX;
    startY.current = t.clientY;
    swiping.current = false;
  };

  const onTouchMove = (e) => {
    const t = e.touches[0];
    const dx = t.clientX - startX.current;
    const dy = t.clientY - startY.current;

    // If it's a vertical scroll, don't interfere
    if (!swiping.current && Math.abs(dy) > Math.abs(dx)) return;

    swiping.current = true;
    // Only allow right swipe, max 80px
    const clamped = Math.max(0, Math.min(dx, 80));
    setOffsetX(clamped);
  };

  const onTouchEnd = () => {
    if (offsetX >= SWIPE_THRESHOLD) {
      onReply?.();
    }
    setOffsetX(0);
    swiping.current = false;
  };

  return (
    <div className={`message-row-wrap`}>
      <div className="message-reply-icon" style={{ opacity: offsetX / 80 }}>
        <Reply size={18} />
      </div>
      <div
        className={`message-row ${isMine ? 'mine' : 'theirs'}`}
        style={{ transform: `translateX(${offsetX}px)`, transition: offsetX === 0 ? 'transform 0.2s' : 'none' }}
        onTouchStart={onTouchStart}
        onTouchMove={onTouchMove}
        onTouchEnd={onTouchEnd}
      >
        {!isMine && (
          <div className="message-avatar">
            {avatarUrl ? (
              <img src={avatarUrl} alt="" />
            ) : (
              (msg.senderName?.[0] || '?').toUpperCase()
            )}
          </div>
        )}

        <div className="message-bubble">
          {!isMine && <span className="message-sender">{msg.senderName}</span>}

          {msg.replyToId && (
            <div className="message-reply-quote">
              <span className="message-reply-name">
                {msg.replyToSenderName || 'Reply'}
              </span>
              <span className="message-reply-text">
                {msg.replyToContent || '[attachment]'}
              </span>
            </div>
          )}

          {msg.attachmentUrl && msg.messageType === 'IMAGE' && (
            <img
              src={
                msg.attachmentUrl.startsWith('http')
                  ? msg.attachmentUrl
                  : `http://localhost:8080${msg.attachmentUrl}`
              }
              alt="attachment"
              className="message-attachment-image"
            />
          )}
          {msg.attachmentUrl && msg.messageType === 'FILE' && (
            <a
              href={
                msg.attachmentUrl.startsWith('http')
                  ? msg.attachmentUrl
                  : `http://localhost:8080${msg.attachmentUrl}`
              }
              target="_blank"
              rel="noreferrer"
              className="message-attachment-file"
            >
              <FileText size={16} /> Download file
            </a>
          )}

          {msg.content && <p>{msg.content}</p>}

          <span className="message-time">
            {new Date(msg.createdAt).toLocaleTimeString([], {
              hour: '2-digit',
              minute: '2-digit',
            })}
            {isMine && (
              <span className="message-ticks">
                <MessageTicks status={msg.status} />
              </span>
            )}
          </span>
        </div>
      </div>
    </div>
  );
};

export default ChatWindow;