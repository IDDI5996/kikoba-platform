import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import sw from './locales/sw.json';
import en from './locales/en.json';

const resources = {
  sw: { translation: sw },
  en: { translation: en },
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    // ---- SWAHILI IS THE DEFAULT ----
    fallbackLng: 'sw',
    supportedLngs: ['sw', 'en'],
    nonExplicitSupportedLngs: true,
    interpolation: {
      escapeValue: false,
    },
    detection: {
      // Priority: user choice > browser > default (sw)
      order: ['localStorage', 'navigator'],
      lookupLocalStorage: 'kikoba_language',
      caches: ['localStorage'],
    },
    react: {
      useSuspense: false,
    },
  });

export default i18n;