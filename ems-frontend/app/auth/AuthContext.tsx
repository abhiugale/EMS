import React, { createContext, useContext, useState, useEffect } from 'react';
import { loginRequest, logoutRequest, type AuthUser } from './authService';
import { queryClient } from '../providers/queryClient';

export type User = AuthUser;

export interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser) as User);
      } catch {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const payload = await loginRequest(email, password);
    const jwtToken = payload.accessToken;
    const loggedUser = payload.user;

    localStorage.setItem('token', jwtToken);
    localStorage.setItem('refreshToken', payload.refreshToken);
    localStorage.setItem('user', JSON.stringify(loggedUser));

    setToken(jwtToken);
    setUser(loggedUser);
  };

  const logout = () => {
    void logoutRequest().catch(() => undefined);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    queryClient.clear();
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
