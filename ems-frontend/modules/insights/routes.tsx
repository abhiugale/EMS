import React from 'react';
import { InsightsPage } from './pages/InsightsPage';

export const insightsRoutes = [
  {
    path: 'insights',
    element: <InsightsPage />,
    protected: true,
  },
];
