import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { CheckCircle2, ShieldAlert } from 'lucide-react';
import adminService from '../../services/adminService';
import { toast } from 'sonner';

const AdminApprovals = () => {
  const { user } = useAuth();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchRequests = async () => {
    try {
      const data = await adminService.getPendingApprovals();
      setRequests(data);
    } catch (err) {
      toast.error('Failed to load approvals');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.adminRole === 'SUPER_ADMIN' || user?.adminRole === 'AUDITOR') {
      fetchRequests();
    } else {
      setLoading(false);
    }
  }, [user]);

  const handleApprove = async (id) => {
    if (!window.confirm("Are you sure you want to execute this sensitive action?")) return;
    try {
      await adminService.approveRequest(id);
      toast.success('Action approved and executed successfully');
      fetchRequests();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to approve request');
    }
  };

  if (loading) return <div>Loading approvals queue...</div>;

  // BLOCK UNAUTHORIZED ROLES
  if (user?.adminRole !== 'SUPER_ADMIN' && user?.adminRole !== 'AUDITOR') {
    return (
      <div style={{ textAlign: 'center', padding: '64px', background: 'white', borderRadius: '12px', border: '1px solid #fee2e2' }}>
        <ShieldAlert size={48} color="#ef4444" style={{ margin: '0 auto 16px' }} />
        <h2 style={{ color: '#991b1b', marginBottom: '8px' }}>Access Restricted</h2>
        <p style={{ color: '#b91c1c' }}>You do not have permission to view the Maker-Checker Approvals. Only Super Admins and Auditors can access this page.</p>
      </div>
    );
  }

  return (
    <div>
      <div className="admin-page-header">
        <h1>Maker-Checker Approvals</h1>
        <p>Sensitive actions requested by Finance Admins that require Super Admin approval.</p>
      </div>

      {requests.length === 0 ? (
        <div className="admin-empty-state">
          <CheckCircle2 size={48} color="#16a34a" />
          <h3>All Clear!</h3>
          <p>There are no pending approvals at the moment.</p>
        </div>
      ) : (
        <div className="admin-table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Requested By</th>
                <th>Action Type</th>
                <th>Payload (Details)</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((req) => (
                <tr key={req.id}>
                  <td>{new Date(req.createdAt).toLocaleString()}</td>
                  <td>{req.requester.firstName} {req.requester.lastName}</td>
                  <td><span className="admin-tag">{req.actionType}</span></td>
                  <td className="admin-code">{req.payload}</td>
                  <td>
                    {user?.adminRole === 'SUPER_ADMIN' ? (
                      <button className="admin-btn-approve" onClick={() => handleApprove(req.id)}>
                        <CheckCircle2 size={16} /> Approve
                      </button>
                    ) : (
                      <span style={{ fontSize: '12px', color: '#64748b' }}>Read Only (Auditor)</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default AdminApprovals;