import React from 'react';
import { ReportsPage } from './pages/ReportsPage';

export const reportsRoutes = [
  {
    path: 'reports',
    element: <ReportsPage />,
    protected: true,
  },
];
