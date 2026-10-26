import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, Phone, MapPin, Send } from 'lucide-react';
import Logo from '../components/common/Logo';
import LanguageSwitcher from '../components/common/LanguageSwitcher';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import api from '../services/api';
import { toast } from 'sonner';
import './StaticPages.css';

const Contact = () => {
  const { t } = useTranslation();
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      // Real API call to Spring Boot
      await api.post('/contact', formData);
      toast.success(t('contact.form.success'));
      setFormData({ name: '', email: '', subject: '', message: '' });
    } catch (err) {
      console.error(err);
      toast.error(t('contact.form.error'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="static-page">
      <header className="landing-header dark-header">
        <Logo variant="full" />
        <div className="landing-header-right">
          <LanguageSwitcher />
          <Link to="/">
            <Button variant="secondary" size="md">Back to Home</Button>
          </Link>
        </div>
      </header>

      <section className="static-hero">
        <div className="static-hero-content">
          <h1 className="static-hero-title">{t('contact.title')}</h1>
          <p className="static-hero-subtitle">{t('contact.subtitle')}</p>
        </div>
      </section>

      <section className="static-section">
        <div className="contact-grid">

          <div className="contact-card">
            <form onSubmit={handleSubmit} className="contact-form">
              <div className="form-row">
                <Input
                  label={t('contact.form.name')}
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
                <Input
                  label={t('contact.form.email')}
                  type="email"
                  required
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                />
              </div>
              <Input
                label={t('contact.form.subject')}
                required
                value={formData.subject}
                onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
              />

              <div className="input-group">
                <label className="input-label">{t('contact.form.message')}</label>
                <textarea
                  className="contact-textarea"
                  required
                  rows={5}
                  value={formData.message}
                  onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                />
              </div>

              <Button type="submit" variant="primary" loading={submitting} className="contact-submit-btn">
                {t('contact.form.send')} <Send size={16} style={{ marginLeft: '8px' }} />
              </Button>
            </form>
          </div>

          <div className="contact-info-wrapper">
            <h3 className="contact-info-title">{t('contact.info.title')}</h3>

            <div className="contact-info-items">
              <div className="contact-info-item">
                <div className="contact-info-icon"><Mail size={20} /></div>
                <div>
                  <p className="contact-info-label">Email</p>
                  <a href={`mailto:${t('contact.info.email')}`}>{t('contact.info.email')}</a>
                </div>
              </div>

              <div className="contact-info-item">
                <div className="contact-info-icon"><Phone size={20} /></div>
                <div>
                  <p className="contact-info-label">Phone</p>
                  <a href={`tel:${t('contact.info.phone')}`}>{t('contact.info.phone')}</a>
                </div>
              </div>

              <div className="contact-info-item">
                <div className="contact-info-icon"><MapPin size={20} /></div>
                <div>
                  <p className="contact-info-label">{t('contact.info.addressTitle')}</p>
                  <p>{t('contact.info.address')}</p>
                </div>
              </div>
            </div>
          </div>

        </div>
      </section>
    </div>
  );
};

export default Contact;