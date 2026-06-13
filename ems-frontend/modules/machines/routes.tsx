import React from 'react';
import { MachinesPage } from './pages/MachinesPage';

export const machinesRoutes = [
  {
    path: 'machines',
    element: <MachinesPage />,
    protected: true,
  },
];
