import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import adminService from '../../services/adminService';
import { toast } from 'sonner';
import { ShieldAlert, Send } from 'lucide-react';

const AdminFinanceRequests = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState({
    actionType: 'MANUAL_BALANCE_ADJUSTMENT',
    targetUser: '',
    amount: '',
    reason: '',
  });

  // Only FINANCE_ADMIN should be able to access this page
  if (user?.adminRole !== 'FINANCE_ADMIN' && user?.adminRole !== 'SUPER_ADMIN') {
    return (
      <div style={{ textAlign: 'center', padding: '64px', background: 'white', borderRadius: '12px', border: '1px solid #fee2e2' }}>
        <ShieldAlert size={48} color="#ef4444" style={{ margin: '0 auto 16px' }} />
        <h2 style={{ color: '#991b1b', marginBottom: '8px' }}>Access Restricted</h2>
        <p style={{ color: '#b91c1c' }}>Only Finance Admins can create sensitive financial requests.</p>
      </div>
    );
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.targetUser || !formData.amount || !formData.reason) {
      toast.error('Please fill all fields');
      return;
    }

    if (!window.confirm("Are you sure you want to submit this request? A Super Admin must approve it.")) {
      return;
    }

    setLoading(true);
    try {
      // The backend expects a JSON string for the payload
      const payload = JSON.stringify({
        targetUser: formData.targetUser,
        amount: formData.amount,
        reason: formData.reason,
      });

      await adminService.createApprovalRequest(formData.actionType, payload);

      toast.success('Request submitted successfully. Awaiting Super Admin approval.');

      // Reset form
      setFormData({
        actionType: 'MANUAL_BALANCE_ADJUSTMENT',
        targetUser: '',
        amount: '',
        reason: '',
      });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="admin-page-header">
        <h1>Create Financial Request</h1>
        <p>Submit sensitive financial actions for Super Admin approval.</p>
      </div>

      <div className="admin-settings-card" style={{ maxWidth: '700px' }}>
        <div className="admin-security-alert" style={{ marginBottom: '24px' }}>
          <ShieldAlert size={24} color="#d97706" />
          <div>
            <h3 style={{ fontSize: '14px' }}>Maker-Checker Policy</h3>
            <p style={{ fontSize: '13px' }}>This action will not be executed immediately. It will be sent to the Super Admin queue for review and approval.</p>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="admin-form-group">
            <label>Action Type</label>
            <select
              className="admin-input"
              value={formData.actionType}
              onChange={(e) => setFormData({ ...formData, actionType: e.target.value })}
            >
              <option value="MANUAL_BALANCE_ADJUSTMENT">Manual Balance Adjustment</option>
              <option value="REFUND">Refund User</option>
              <option value="LOAN_CANCELLATION">Loan Cancellation</option>
            </select>
          </div>

          <div className="admin-form-group">
            <label>Target User (Phone Number or ID)</label>
            <input
              type="text"
              className="admin-input"
              placeholder="+255 712 345 678"
              value={formData.targetUser}
              onChange={(e) => setFormData({ ...formData, targetUser: e.target.value })}
              required
            />
          </div>

          <div className="admin-form-group">
            <label>Amount (TZS)</label>
            <input
              type="number"
              className="admin-input"
              placeholder="5000"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              required
            />
          </div>

          <div className="admin-form-group">
            <label>Reason for Request</label>
            <textarea
              className="admin-input"
              rows={4}
              placeholder="e.g., M-Pesa transaction failed but user was charged..."
              value={formData.reason}
              onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
              required
            />
          </div>

          <button type="submit" className="admin-btn-approve" disabled={loading}>
            <Send size={16} /> {loading ? 'Submitting...' : 'Submit Request to Super Admin'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AdminFinanceRequests;