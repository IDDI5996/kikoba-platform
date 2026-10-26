import { useChat } from '../../context/ChatContext';
import ChatSidebar from './ChatSidebar';
import ChatWindow from './ChatWindow';
import './Chat.css';

const ChatPage = () => {
  const { activeChat } = useChat();

  return (
    <div className="chat-page">
      <div className={`chat-container ${activeChat ? 'has-active' : ''}`}>
        <ChatSidebar />
        {activeChat ? (
          <ChatWindow />
        ) : (
          <div className="chat-empty-state">
            <h2>Welcome to Kikoba Chat</h2>
            <p>Select a group or member to start messaging</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatPage;