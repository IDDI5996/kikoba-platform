import { useEffect, useRef } from 'react';
import { Info, BellOff, Bell, Trash2, UserX } from 'lucide-react';

const ChatMenu = ({ open, onClose, chat, onChatInfo, onToggleMute, onClearChat, muted }) => {
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    const handler = (e) => {
      if (ref.current && !ref.current.contains(e.target)) onClose();
    };
    document.addEventListener('mousedown', handler);
    document.addEventListener('touchstart', handler);
    return () => {
      document.removeEventListener('mousedown', handler);
      document.removeEventListener('touchstart', handler);
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="chat-menu" ref={ref}>
      <button type="button" className="chat-menu-item" onClick={onChatInfo}>
        <Info size={16} /> Chat info
      </button>
      <button type="button" className="chat-menu-item" onClick={onToggleMute}>
        {muted ? <Bell size={16} /> : <BellOff size={16} />}
        {muted ? 'Unmute notifications' : 'Mute notifications'}
      </button>
      <button type="button" className="chat-menu-item" onClick={onClearChat}>
        <Trash2 size={16} /> Clear chat
      </button>
      {chat?.type === 'PRIVATE' && (
        <button type="button" className="chat-menu-item danger">
          <UserX size={16} /> Block user
        </button>
      )}
    </div>
  );
};

export default ChatMenu;