import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import API from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem('kikoba_token'));
  const [loading, setLoading] = useState(true);

  // Set the token in axios defaults whenever it changes
  useEffect(() => {
    if (token) {
      API.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete API.defaults.headers.common['Authorization'];
    }
  }, [token]);

  // Load user on mount
  useEffect(() => {
    const bootstrap = async () => {
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        const { data } = await API.get('/users/me');
        setUser(data);
      } catch {
        localStorage.removeItem('kikoba_token');
        localStorage.removeItem('kikoba_refresh');
        setToken(null);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    bootstrap();
  }, [token]);

  const login = useCallback(async (identifier, password) => {
    const { data } = await API.post('/auth/login', { identifier, password });
    localStorage.setItem('kikoba_token', data.accessToken);
    localStorage.setItem('kikoba_refresh', data.refreshToken);
    setToken(data.accessToken);
    setUser(data.user);
    return data.user;
  }, []);

  const register = useCallback(async (payload) => {
    const { data } = await API.post('/auth/register', payload);
    localStorage.setItem('kikoba_token', data.accessToken);
    localStorage.setItem('kikoba_refresh', data.refreshToken);
    setToken(data.accessToken);
    setUser(data.user);
    return data.user;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('kikoba_token');
    localStorage.removeItem('kikoba_refresh');
    setToken(null);
    setUser(null);
  }, []);

  const value = { user, token, loading, login, register, logout, setUser };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};