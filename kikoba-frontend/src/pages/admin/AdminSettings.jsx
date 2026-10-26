import { useState, useEffect } from 'react';
import adminService from '../../services/adminService';
import { toast } from 'sonner';
import { Save } from 'lucide-react';

const AdminSettings = () => {
  const [settings, setSettings] = useState({ maintenanceMode: false, globalAnnouncement: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    adminService.getSystemSettings()
      .then(data => setSettings(data))
      .catch(() => toast.error('Failed to load settings'))
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    if (!window.confirm("Are you sure you want to update system-wide settings?")) return;

    setSaving(true);
    try {
      await adminService.updateSystemSettings(settings);
      toast.success('System settings updated');
    } catch (err) {
      toast.error('Failed to update settings');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p>Loading settings...</p>;

  return (
    <div>
      <div className="admin-page-header">
        <h1>System Settings</h1>
        <p>Manage global platform configuration.</p>
      </div>

      <div className="admin-settings-card">
        <form onSubmit={handleSave}>
          <div className="admin-form-group">
            <label className="admin-toggle-label">
              <input
                type="checkbox"
                checked={settings.maintenanceMode}
                onChange={(e) => setSettings({ ...settings, maintenanceMode: e.target.checked })}
              />
              <span className="admin-toggle-text">
                <strong>Maintenance Mode</strong>
                <small>If enabled, regular users will be locked out of the app.</small>
              </span>
            </label>
          </div>

          <div className="admin-form-group">
            <label>Global Announcement Banner</label>
            <textarea
              rows={4}
              placeholder="Enter a message to display to all users on their dashboard..."
              value={settings.globalAnnouncement || ''}
              onChange={(e) => setSettings({ ...settings, globalAnnouncement: e.target.value })}
            />
          </div>

          <button type="submit" className="admin-btn-approve" disabled={saving}>
            <Save size={16} /> {saving ? 'Saving...' : 'Save Settings'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AdminSettings;