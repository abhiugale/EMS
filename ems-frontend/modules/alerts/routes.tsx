import React from 'react';
import { AlertsPage } from './pages/AlertsPage';

export const alertsRoutes = [
  {
    path: 'alerts',
    element: <AlertsPage />,
    protected: true,
  },
];
