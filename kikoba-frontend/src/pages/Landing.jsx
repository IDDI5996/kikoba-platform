import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Wallet, HandCoins, Users, MessageCircle, Vote, Calendar,
  ShieldCheck, TrendingUp, BadgeCheck, Lock, Bell, UserCheck,
  CheckCircle2, ArrowRight, Smartphone, Globe,
} from 'lucide-react';
import Logo from '../components/common/Logo';
import LanguageSwitcher from '../components/common/LanguageSwitcher';
import Button from '../components/common/Button';
import './Landing.css';

const Landing = () => {
  const { t } = useTranslation();

  const coreFeatures = [
    { icon: Wallet,        key: 'savings' },
    { icon: HandCoins,     key: 'loans' },
    { icon: Users,         key: 'members' },
    { icon: MessageCircle, key: 'chat' },
    { icon: Vote,          key: 'polls' },
    { icon: Calendar,      key: 'meetings' },
    { icon: TrendingUp,    key: 'shares' },
    { icon: ShieldCheck,   key: 'security' },
  ];

  const showcases = [
    { id: 'savings', image: '/images/landing/feature-savings.png', reverse: false },
    { id: 'loans',   image: '/images/landing/feature-loans.png',   reverse: true  },
    { id: 'chat',    image: '/images/landing/feature-chat.png',    reverse: false },
    { id: 'roles',   image: '/images/landing/feature-roles.png',   reverse: true  },
  ];

  const stepKeys = ['create', 'join', 'save'];

  const stepIcons = {
    create: UserCheck,
    join: Users,
    save: Wallet,
  };

  const securityItems = [
    { icon: BadgeCheck, key: 'kyc' },
    { icon: Lock,       key: 'mfa' },
    { icon: ShieldCheck,key: 'encryption' },
    { icon: Bell,       key: 'notifications' },
  ];

  return (
    <div className="landing">

      {/* ================= TOP WRAPPER (DARK BACKGROUND) ================= */}
      <div className="landing-top-wrapper">
        {/* HEADER */}
        <header className="landing-header">
          <Logo variant="full" />
          <div className="landing-header-right">
            <LanguageSwitcher />
            <Link to="/login">
              <Button variant="secondary" size="md">{t('auth.login')}</Button>
            </Link>
          </div>
        </header>

        {/* HERO (Centered) */}
        <section className="landing-hero">
          <div className="landing-hero-content">
            <div className="landing-hero-badge">
              <Globe size={14} />
              <span>{t('landing.hero.badge')}</span>
            </div>

            <h1 className="landing-hero-title">{t('app.tagline')}</h1>
            <p className="landing-hero-subtitle">{t('landing.hero.subtitle')}</p>

            <div className="landing-hero-actions">
              <Link to="/register">
                <Button variant="primary" size="lg">
                  {t('auth.getStarted')} <ArrowRight size={18} />
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="secondary" size="lg">{t('auth.login')}</Button>
              </Link>
            </div>

            <div className="landing-hero-hint">
              <Smartphone size={14} />
              <span>{t('landing.hero.hint')}</span>
            </div>
          </div>
        </section>

        {/* TRUST BAR */}
        <section className="landing-trust">
          <p className="landing-trust-label">{t('landing.trust.label')}</p>
          <div className="landing-trust-logos">
            <div className="landing-partner">Apex Bank</div>
            <div className="landing-partner">Trust Bank</div>
            <div className="landing-partner">TCB</div>
            <div className="landing-partner">NMB</div>
            <div className="landing-partner">CRDB</div>
          </div>
        </section>
      </div>
      {/* ================= END TOP WRAPPER ================= */}

      {/* CORE FEATURES */}
      <section className="landing-section">
        <div className="landing-section-header">
          <h2 className="landing-section-title">{t('landing.features.title')}</h2>
          <p className="landing-section-subtitle">{t('landing.features.subtitle')}</p>
        </div>

        <div className="landing-features">
          {coreFeatures.map((f) => (
            <div key={f.key} className="landing-feature-card">
              <div className="landing-feature-icon">
                <f.icon size={24} aria-hidden="true" />
              </div>
              <h3 className="landing-feature-title">
                {t(`landing.features.items.${f.key}.title`)}
              </h3>
              <p className="landing-feature-desc">
                {t(`landing.features.items.${f.key}.desc`)}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* FEATURE SHOWCASES */}
      {showcases.map((s) => {
        const bullets = t(`landing.showcases.${s.id}.bullets`, { returnObjects: true });
        return (
          <section
            key={s.id}
            className={`landing-showcase ${s.reverse ? 'reverse' : ''}`}
          >
            <div className="landing-showcase-image">
              <img
                src={s.image}
                alt={t(`landing.showcases.${s.id}.imageAlt`)}
              />
            </div>
            <div className="landing-showcase-content">
              <span className="landing-showcase-eyebrow">
                {t(`landing.showcases.${s.id}.eyebrow`)}
              </span>
              <h2 className="landing-showcase-title">
                {t(`landing.showcases.${s.id}.title`)}
              </h2>
              <p className="landing-showcase-desc">
                {t(`landing.showcases.${s.id}.desc`)}
              </p>
              <ul className="landing-showcase-bullets">
                {Array.isArray(bullets) && bullets.map((b) => (
                  <li key={b}>
                    <CheckCircle2 size={18} />
                    <span>{b}</span>
                  </li>
                ))}
              </ul>
            </div>
          </section>
        );
      })}

      {/* HOW IT WORKS */}
      <section className="landing-section landing-how">
        <div className="landing-section-header">
          <h2 className="landing-section-title">{t('landing.howItWorks.title')}</h2>
          <p className="landing-section-subtitle">{t('landing.howItWorks.subtitle')}</p>
        </div>

        <div className="landing-steps">
          {stepKeys.map((key, i) => {
            const Icon = stepIcons[key];
            return (
              <div key={key} className="landing-step">
                <div className="landing-step-number">{i + 1}</div>
                <div className="landing-step-icon">
                  <Icon size={28} />
                </div>
                <h3 className="landing-step-title">
                  {t(`landing.howItWorks.steps.${key}.title`)}
                </h3>
                <p className="landing-step-desc">
                  {t(`landing.howItWorks.steps.${key}.desc`)}
                </p>
              </div>
            );
          })}
        </div>
      </section>

      {/* SECURITY */}
      <section className="landing-section landing-security">
        <div className="landing-section-header">
          <h2 className="landing-section-title">{t('landing.security.title')}</h2>
          <p className="landing-section-subtitle">{t('landing.security.subtitle')}</p>
        </div>

        <div className="landing-security-grid">
          {securityItems.map((f) => (
            <div key={f.key} className="landing-security-card">
              <div className="landing-security-icon">
                <f.icon size={22} />
              </div>
              <h3>{t(`landing.security.items.${f.key}.title`)}</h3>
              <p>{t(`landing.security.items.${f.key}.desc`)}</p>
            </div>
          ))}
        </div>
      </section>

      {/* FINAL CTA */}
      <section className="landing-cta">
        <div className="landing-cta-inner">
          <Globe size={32} className="landing-cta-icon" />
          <h2 className="landing-cta-title">{t('landing.cta.title')}</h2>
          <p className="landing-cta-subtitle">{t('landing.cta.subtitle')}</p>
          <div className="landing-cta-actions">
            <Link to="/register">
              <Button variant="primary" size="lg">
                {t('landing.cta.createAccount')} <ArrowRight size={18} />
              </Button>
            </Link>
            <Link to="/login">
              <Button variant="secondary" size="lg">{t('landing.cta.signIn')}</Button>
            </Link>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="landing-footer">
        <div className="landing-footer-inner">
          <div className="landing-footer-brand">
            <Logo variant="full" />
            <p className="landing-footer-tagline">{t('app.tagline')}</p>
          </div>
          <div className="landing-footer-links">
            <div className="landing-footer-col">
              <h4>{t('landing.footer.product')}</h4>
              <Link to="/register">{t('landing.footer.signUp')}</Link>
              <Link to="/login">{t('landing.footer.signIn')}</Link>
            </div>
            <div className="landing-footer-col">
              <h4>{t('landing.footer.company')}</h4>
              {/* UPDATED TO LINK */}
              <Link to="/about">{t('landing.footer.about')}</Link>
              <Link to="/contact">{t('landing.footer.contact')}</Link>
            </div>
            <div className="landing-footer-col">
              <h4>{t('landing.footer.legal')}</h4>
              {/* UPDATED TO LINK */}
              <Link to="/terms">{t('landing.footer.terms')}</Link>
              <Link to="/privacy">{t('landing.footer.privacy')}</Link>
            </div>
          </div>
        </div>
        <div className="landing-footer-bottom">
          <p>{t('landing.copyright', { year: new Date().getFullYear() })}</p>
        </div>
      </footer>
    </div>
  );
};

export default Landing;