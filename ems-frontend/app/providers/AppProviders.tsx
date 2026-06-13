import React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../auth/AuthContext';
import { queryClient } from './queryClient';

export const AppProviders: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        {children}
      </AuthProvider>
    </QueryClientProvider>
  );
};

export default AppProviders;
