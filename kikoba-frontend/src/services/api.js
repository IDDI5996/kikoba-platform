import axios from 'axios';

/**
 * Axios instance for KikobaConnect backend API.
 *
 * - Base URL points to the Vite proxy which forwards to Spring Boot
 *   at http://localhost:8080 (configured in vite.config.js).
 * - Auth token is injected automatically on every request via an
 *   interceptor that reads from localStorage.
 * - 401 responses are handled by clearing auth state and redirecting
 *   the user to the login page.
 */
const API = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// ---- Request interceptor ----
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('kikoba_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ---- Response interceptor ----
API.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle token expiration / unauthorized
    if (error.response?.status === 401) {
      // Clear stale auth state
      localStorage.removeItem('kikoba_token');
      localStorage.removeItem('kikoba_refresh');

      // Avoid redirect loop if already on login page
      if (!window.location.pathname.startsWith('/login')
          && !window.location.pathname.startsWith('/register')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default API;