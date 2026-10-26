import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ShieldCheck } from 'lucide-react';
import Logo from '../components/common/Logo';
import LanguageSwitcher from '../components/common/LanguageSwitcher';
import Button from '../components/common/Button';
import './StaticPages.css';

const Privacy = () => {
  const { t } = useTranslation();

  return (
    <div className="static-page">
      <header className="landing-header dark-header">
        <Logo variant="full" />
        <div className="landing-header-right">
          <LanguageSwitcher />
          <Link to="/">
            <Button variant="secondary" size="md">{t('common.back')}</Button>
          </Link>
        </div>
      </header>

      <section className="static-hero">
        <div className="static-hero-content">
          <ShieldCheck size={48} style={{ marginBottom: '1rem' }} />
          <h1 className="static-hero-title">{t('privacy.title')}</h1>
          <p className="static-hero-subtitle">{t('privacy.lastUpdated')}</p>
        </div>
      </section>

      <section className="static-section legal-content">
        <div className="legal-intro">
          <p>{t('privacy.intro')}</p>
        </div>

        <div className="legal-block">
          <h2>1. {t('privacy.s1.title')}</h2>
          <p>{t('privacy.s1.text')}</p>
        </div>

        <div className="legal-block">
          <h2>2. {t('privacy.s2.title')}</h2>
          <p>{t('privacy.s2.text')}</p>
          <ul>
            <li>{t('privacy.s2.item1')}</li>
            <li>{t('privacy.s2.item2')}</li>
            <li>{t('privacy.s2.item3')}</li>
          </ul>
        </div>

        <div className="legal-block">
          <h2>3. {t('privacy.s3.title')}</h2>
          <p>{t('privacy.s3.text')}</p>
        </div>

        <div className="legal-block">
          <h2>4. {t('privacy.s4.title')}</h2>
          <p>{t('privacy.s4.text')}</p>
        </div>

        <div className="legal-block">
          <h2>5. {t('privacy.s5.title')}</h2>
          <p>{t('privacy.s5.text')}</p>
        </div>

        <div className="legal-block">
          <h2>6. {t('privacy.s6.title')}</h2>
          <p>{t('privacy.s6.text')}</p>
        </div>

        <div className="legal-footer">
          <p>{t('privacy.contactText')}</p>
          <Link to="/contact" className="legal-link">{t('contact.title')}</Link>
        </div>
      </section>
    </div>
  );
};

export default Privacy;