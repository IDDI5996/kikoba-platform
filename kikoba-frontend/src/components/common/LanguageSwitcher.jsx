import { useTranslation } from 'react-i18next';
import { Languages } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import './LanguageSwitcher.css';

const LanguageSwitcher = () => {
  const { i18n } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const ref = useRef(null);

  const currentLang = i18n.language?.startsWith('en') ? 'en' : 'sw';

  const languages = [
    { code: 'sw', label: 'Kiswahili', short: 'SW' },
    { code: 'en', label: 'English', short: 'EN' },
  ];

  const changeLanguage = (code) => {
    i18n.changeLanguage(code);
    setIsOpen(false);
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="lang-switcher" ref={ref}>
      <button
        type="button"
        className="lang-switcher-btn"
        onClick={() => setIsOpen(!isOpen)}
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        aria-label="Change language"
      >
        <Languages size={16} aria-hidden="true" />
        <span>{languages.find(l => l.code === currentLang)?.short}</span>
      </button>
      {isOpen && (
        <ul className="lang-switcher-menu" role="listbox">
          {languages.map((lang) => (
            <li key={lang.code}>
              <button
                type="button"
                role="option"
                aria-selected={currentLang === lang.code}
                className={`lang-switcher-item ${currentLang === lang.code ? 'active' : ''}`}
                onClick={() => changeLanguage(lang.code)}
              >
                <span>{lang.label}</span>
                {currentLang === lang.code && <span aria-hidden="true">•</span>}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default LanguageSwitcher;