import React from 'react';
import { UploadPage } from './pages/UploadPage';

export const uploadRoutes = [
  {
    path: 'upload',
    element: <UploadPage />,
    protected: true,
  },
];
