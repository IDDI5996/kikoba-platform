import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Users, FileText, Wallet, ShieldAlert } from 'lucide-react';
import adminService from '../../services/adminService';
import { toast } from 'sonner';

const AdminDashboard = () => {
  const { t } = useTranslation();
  const [stats, setStats] = useState({ totalUsers: 0, totalGroups: 0, totalTransactions: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await adminService.getStats();
        setStats(data);
      } catch (err) {
        toast.error('Failed to load admin statistics');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) return <div className="admin-loading">Loading secure data...</div>;

  return (
    <div>
      <div className="admin-page-header">
        <h1>{t('admin.systemOverview')}</h1>
        <p>{t('admin.systemOverviewDesc')}</p>
      </div>

      <div className="admin-stats-grid">
        <div className="admin-stat-card">
          <div className="admin-stat-icon blue"><Users size={24} /></div>
          <div>
            <p className="admin-stat-label">{t('admin.totalUsers')}</p>
            <h3 className="admin-stat-value">{stats.totalUsers.toLocaleString()}</h3>
          </div>
        </div>

        <div className="admin-stat-card">
          <div className="admin-stat-icon green"><FileText size={24} /></div>
          <div>
            <p className="admin-stat-label">{t('admin.activeGroups')}</p>
            <h3 className="admin-stat-value">{stats.totalGroups.toLocaleString()}</h3>
          </div>
        </div>

        <div className="admin-stat-card">
          <div className="admin-stat-icon orange"><Wallet size={24} /></div>
          <div>
            <p className="admin-stat-label">{t('admin.totalTx')}</p>
            <h3 className="admin-stat-value">{stats.totalTransactions.toLocaleString()}</h3>
          </div>
        </div>
      </div>

      <div className="admin-security-alert">
        <ShieldAlert size={32} color="#d97706" />
        <div>
          <h3>{t('admin.securityNotice')}</h3>
          <p>{t('admin.securityNoticeDesc')}</p>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;