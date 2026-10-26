import { createContext, useContext, useState } from 'react';

const RegistrationContext = createContext(null);

export const RegistrationProvider = ({ children }) => {
  const [pending, setPending] = useState(null);
  // pending = {
  //   phoneNumber, maskedPhone, email, maskedEmail,
  //   emailRequired, status, nextStep, codeExpiresInSeconds
  // }

  const clear = () => setPending(null);

  return (
    <RegistrationContext.Provider value={{ pending, setPending, clear }}>
      {children}
    </RegistrationContext.Provider>
  );
};

export const useRegistration = () => {
  const ctx = useContext(RegistrationContext);
  if (!ctx) throw new Error('useRegistration must be used within RegistrationProvider');
  return ctx;
};