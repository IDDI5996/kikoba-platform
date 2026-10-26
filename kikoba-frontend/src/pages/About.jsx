import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ShieldCheck, Users, Target, Lightbulb, ArrowLeft } from 'lucide-react';
import Logo from '../components/common/Logo';
import LanguageSwitcher from '../components/common/LanguageSwitcher';
import Button from '../components/common/Button';
import './StaticPages.css';

const About = () => {
  const { t } = useTranslation();

  const values = [
    { icon: ShieldCheck, key: 'transparency' },
    { icon: Target,      key: 'security' },
    { icon: Users,       key: 'community' },
    { icon: Lightbulb,   key: 'innovation' },
  ];

  return (
    <div className="static-page">
      {/* HEADER */}
      <header className="landing-header dark-header">
        <Logo variant="full" />
        <div className="landing-header-right">
          <LanguageSwitcher />
          <Link to="/">
            <Button variant="secondary" size="md">Back to Home</Button>
          </Link>
        </div>
      </header>

      {/* HERO */}
      <section className="static-hero">
        <div className="static-hero-content">
          <h1 className="static-hero-title">{t('about.title')}</h1>
          <p className="static-hero-subtitle">{t('about.heroSubtitle')}</p>
        </div>
      </section>

      {/* OUR STORY */}
      <section className="static-section">
              <div className="static-story-grid">
                <div className="static-story-content">
                  <h2 className="static-section-title">{t('about.storyTitle')}</h2>
                  <p className="static-text">{t('about.storyP1')}</p>
                  <p className="static-text">{t('about.storyP2')}</p>
                </div>
                <div className="static-story-image-wrapper">
                   {/* REPLACED WITH IMAGE */}
                   <img
                     src="/images/about-team.png"
                     alt="KikobaConnect Team and Community"
                     className="static-story-image"
                   />
                </div>
              </div>
      </section>

      {/* MISSION */}
      <section className="static-mission">
        <div className="static-mission-inner">
          <h2 className="static-section-title text-white">{t('about.missionTitle')}</h2>
          <p className="static-mission-desc">{t('about.missionDesc')}</p>
        </div>
      </section>

      {/* VALUES */}
      <section className="static-section">
        <h2 className="static-section-title center">{t('about.valuesTitle')}</h2>
        <div className="static-values-grid">
          {values.map((v) => (
            <div key={v.key} className="static-value-card">
              <div className="static-value-icon">
                <v.icon size={24} />
              </div>
              <h3>{t(`about.values.${v.key}.title`)}</h3>
              <p>{t(`about.values.${v.key}.desc`)}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default About;