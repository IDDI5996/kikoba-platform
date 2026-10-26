import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Search, CheckCircle2, XCircle } from 'lucide-react';
import adminService from '../../services/adminService';
import { toast } from 'sonner';

const AdminUsers = () => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await adminService.getAllUsers(search, page, 10);
      setUsers(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, [page]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    fetchUsers();
  };

  const handleRoleChange = async (userId, newRole) => {
    const confirmMsg = newRole
      ? `Are you sure you want to make this user a ${newRole}?`
      : "Are you sure you want to remove Admin privileges from this user?";

    if (!window.confirm(confirmMsg)) return;
    try {
      await adminService.updateUserRole(userId, newRole);
      toast.success('Role updated successfully');
      fetchUsers();
    } catch (err) {
      toast.error('Failed to update role');
    }
  };

  const handleToggleStatus = async (userId) => {
    if (!window.confirm("Are you sure you want to toggle this user's active status?")) return;
    try {
      await adminService.toggleUserStatus(userId);
      toast.success('Status updated');
      fetchUsers();
    } catch (err) {
      toast.error('Failed to update status');
    }
  };

  return (
    <div>
      <div className="admin-page-header">
        <h1>User Management</h1>
        <p>View, search, and manage all users on the platform.</p>
      </div>

      <form onSubmit={handleSearch} className="admin-search-bar">
        <input
          type="text"
          placeholder="Search by name or phone number..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button type="submit"><Search size={18} /> Search</button>
      </form>

      {loading ? <p>Loading users...</p> : (
        <>
          <div className="admin-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Admin Role</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.firstName} {u.lastName}</strong></td>
                    <td>{u.phoneNumber}</td>
                    <td>
                      {u.isActive ?
                        <span className="badge-active"><CheckCircle2 size={14}/> Active</span> :
                        <span className="badge-suspended"><XCircle size={14}/> Suspended</span>
                      }
                    </td>
                    <td>
                      {user?.adminRole === 'SUPER_ADMIN' ? (
                        <select
                          value={u.adminRole || ''}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          className="admin-select"
                        >
                          <option value="">Regular User</option>
                          <option value="SUPPORT_ADMIN">Support Admin</option>
                          <option value="FINANCE_ADMIN">Finance Admin</option>
                          <option value="AUDITOR">Auditor</option>
                          <option value="SUPER_ADMIN">Super Admin</option>
                        </select>
                      ) : (
                        <span>{u.adminRole?.replace('_', ' ') || 'Regular User'}</span>
                      )}
                    </td>
                    <td>
                      <button className="admin-btn-outline" onClick={() => handleToggleStatus(u.id)}>
                        {u.isActive ? 'Suspend' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="admin-pagination">
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
            <span>Page {page + 1} of {totalPages || 1}</span>
            <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
          </div>
        </>
      )}
    </div>
  );
};

export default AdminUsers;