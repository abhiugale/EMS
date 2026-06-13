import React from 'react';
import { AdminPage } from './pages/AdminPage';

export const adminRoutes = [
  {
    path: 'admin',
    element: <AdminPage />,
    protected: true,
  },
];
