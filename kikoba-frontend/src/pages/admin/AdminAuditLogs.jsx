import { useState, useEffect } from 'react';
import adminService from '../../services/adminService';
import { toast } from 'sonner';

const AdminAuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLogs = async () => {
      setLoading(true);
      try {
        const data = await adminService.getAuditLogs(page, 20);
        setLogs(data.content);
        setTotalPages(data.totalPages);
      } catch (err) {
        toast.error('Failed to load audit logs');
      } finally {
        setLoading(false);
      }
    };
    fetchLogs();
  }, [page]);

  return (
    <div>
      <div className="admin-page-header">
        <h1>Audit Logs</h1>
        <p>Immutable record of all administrative actions.</p>
      </div>

      {loading ? <p>Loading logs...</p> : (
        <>
          <div className="admin-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Admin</th>
                  <th>Action</th>
                  <th>Details</th>
                  <th>IP Address</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <tr key={log.id}>
                    <td>{new Date(log.createdAt).toLocaleString()}</td>
                    <td>{log.admin?.firstName} {log.admin?.lastName}</td>
                    <td><span className="admin-tag">{log.action}</span></td>
                    <td className="admin-code">{log.details}</td>
                    <td>{log.ipAddress}</td>
                  </tr>
                ))}
                {logs.length === 0 && (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '32px' }}>No audit logs found.</td>
                  </tr>
                )}
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

export default AdminAuditLogs;