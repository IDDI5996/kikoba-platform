import { useEffect, useState } from 'react';
import { useChat } from '../../context/ChatContext';
import { useAuth } from '../../context/AuthContext';
import { Search, Users, User, Loader2 } from 'lucide-react';

const TABS = {
  CHATS: 'chats',
  GROUPS: 'groups',
  PEOPLE: 'people',
};

const ChatSidebar = () => {
  const {
    chats,
    loadingChats,
    loadMyChats,
    setActiveChat,
    activeChat,
    directory,
    loadingDirectory,
    loadDirectory,
    startPrivateChat,
    openGroupChat,
  } = useChat();
  const { user } = useAuth();

  const [tab, setTab] = useState(TABS.CHATS);
  const [search, setSearch] = useState('');
  const [creating, setCreating] = useState(null);

  useEffect(() => {
    loadMyChats();
    loadDirectory();
  }, [loadMyChats, loadDirectory]);

  const getAvatarUrl = (url) =>
    url ? (url.startsWith('http') ? url : `http://localhost:8080${url}`) : null;

  // ---- Chats tab ----
  const filteredChats = chats.filter((c) => {
    const name = c.name || '';
    return name.toLowerCase().includes(search.toLowerCase());
  });

  // ---- Groups tab ----
  const filteredGroups = (directory.groups || []).filter((g) =>
    (g.name || '').toLowerCase().includes(search.toLowerCase())
  );

  // ---- People tab ----
  const filteredMembers = (directory.members || []).filter((m) =>
    `${m.firstName} ${m.lastName}`.toLowerCase().includes(search.toLowerCase())
  );

  const handleOpenGroup = async (group) => {
    setCreating(`group-${group.id}`);
    try {
      await openGroupChat(group.id);
      setTab(TABS.CHATS);
    } catch (e) {
      console.error(e);
    } finally {
      setCreating(null);
    }
  };

  const handleStartPrivate = async (member) => {
    setCreating(`user-${member.id}`);
    try {
      await startPrivateChat(member.id);
      setTab(TABS.CHATS);
    } catch (e) {
      console.error(e);
    } finally {
      setCreating(null);
    }
  };

  return (
    <div className="chat-sidebar">
      <div className="chat-sidebar-header">
        <h3>Chats</h3>

        <div className="chat-tabs">
          <button
            className={tab === TABS.CHATS ? 'active' : ''}
            onClick={() => setTab(TABS.CHATS)}
          >
            Chats
          </button>
          <button
            className={tab === TABS.GROUPS ? 'active' : ''}
            onClick={() => setTab(TABS.GROUPS)}
          >
            Groups
          </button>
          <button
            className={tab === TABS.PEOPLE ? 'active' : ''}
            onClick={() => setTab(TABS.PEOPLE)}
          >
            People
          </button>
        </div>

        <div className="chat-search">
          <Search size={16} />
          <input
            placeholder="Search..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      <div className="chat-list">
        {/* CHATS TAB */}
        {tab === TABS.CHATS && (
          <>
            {loadingChats && (
              <div className="chat-loading">
                <Loader2 className="spin" size={18} /> Loading chats…
              </div>
            )}
            {!loadingChats && filteredChats.length === 0 && (
              <p className="chat-empty">
                No conversations yet. Start one from <b>Groups</b> or <b>People</b>.
              </p>
            )}
            {filteredChats.map((chat) => {
              const avatar = getAvatarUrl(chat.otherUserAvatarUrl);
              return (
                <div
                  key={chat.id}
                  className={`chat-list-item ${
                    activeChat?.id === chat.id ? 'active' : ''
                  }`}
                  onClick={() => setActiveChat(chat)}
                >
                  <div className="chat-avatar">
                    {avatar ? (
                      <img src={avatar} alt="" />
                    ) : chat.type === 'GROUP' ? (
                      <Users size={20} />
                    ) : (
                      <User size={20} />
                    )}
                  </div>
                  <div className="chat-info">
                    <span className="chat-name">
                      {chat.type === 'GROUP'
                        ? chat.name || 'Group chat'
                        : chat.name || 'Private chat'}
                    </span>
                    <span className="chat-preview">
                      {chat.lastMessage || 'No messages yet'}
                    </span>
                  </div>
                </div>
              );
            })}
          </>
        )}

        {/* GROUPS TAB */}
        {tab === TABS.GROUPS && (
          <>
            {loadingDirectory && (
              <div className="chat-loading">
                <Loader2 className="spin" size={18} /> Loading groups…
              </div>
            )}
            {!loadingDirectory && filteredGroups.length === 0 && (
              <p className="chat-empty">
                You're not a member of any Kikoba group yet.
              </p>
            )}
            {filteredGroups.map((group) => (
              <div
                key={group.id}
                className="chat-list-item"
                onClick={() => handleOpenGroup(group)}
              >
                <div className="chat-avatar group">
                  <Users size={20} />
                </div>
                <div className="chat-info">
                  <span className="chat-name">{group.name}</span>
                  <span className="chat-preview">
                    {group.memberCount} member
                    {group.memberCount === 1 ? '' : 's'}
                  </span>
                </div>
                {creating === `group-${group.id}` && (
                  <Loader2 className="spin" size={16} />
                )}
              </div>
            ))}
          </>
        )}

        {/* PEOPLE TAB */}
        {tab === TABS.PEOPLE && (
          <>
            {loadingDirectory && (
              <div className="chat-loading">
                <Loader2 className="spin" size={18} /> Loading members…
              </div>
            )}
            {!loadingDirectory && filteredMembers.length === 0 && (
              <p className="chat-empty">
                No one shares a group with you yet.
              </p>
            )}
            {filteredMembers.map((member) => {
              const avatar = getAvatarUrl(member.profilePictureUrl);
              return (
                <div
                  key={member.id}
                  className="chat-list-item"
                  onClick={() => handleStartPrivate(member)}
                >
                  <div className="chat-avatar">
                    {avatar ? (
                      <img src={avatar} alt="" />
                    ) : (
                      `${member.firstName?.[0] || ''}${member.lastName?.[0] || ''}`.toUpperCase()
                    )}
                  </div>
                  <div className="chat-info">
                    <span className="chat-name">
                      {member.firstName} {member.lastName}
                    </span>
                    <span className="chat-preview">{member.phoneNumber}</span>
                  </div>
                  {creating === `user-${member.id}` && (
                    <Loader2 className="spin" size={16} />
                  )}
                </div>
              );
            })}
          </>
        )}
      </div>
    </div>
  );
};

export default ChatSidebar;