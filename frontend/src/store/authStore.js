import { create } from 'zustand';
import axios from 'axios';

const API = '/api/v1';

export const useAuthStore = create((set, get) => ({
  user: null,
  token: localStorage.getItem('token') || null,
  loading: false,
  error: null,

  login: async (username, password) => {
    set({ loading: true, error: null });
    try {
      const { data } = await axios.post(`${API}/auth/login`, { username, password });
      localStorage.setItem('token', data.token || data.data?.token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${data.token || data.data?.token}`;
      set({ user: data.user || data.data?.user, token: data.token || data.data?.token, loading: false });
      return true;
    } catch (err) {
      set({ error: err.response?.data?.message || 'Login failed', loading: false });
      return false;
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    delete axios.defaults.headers.common['Authorization'];
    set({ user: null, token: null });
  },

  fetchProfile: async () => {
    const { token } = get();
    if (!token) return;
    try {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      const { data } = await axios.get(`${API}/auth/profile`);
      set({ user: data.user || data.data?.user });
    } catch {
      get().logout();
    }
  },

  isAuthenticated: () => !!get().token,
}));
