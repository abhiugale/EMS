import React from 'react';
import { DashboardPage } from './pages/DashboardPage';

export const dashboardRoutes = [
  {
    index: true,
    element: <DashboardPage />,
    protected: true,
  },
];
