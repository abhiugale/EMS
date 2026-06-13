import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AppProviders } from '../app/providers/AppProviders';
import { RouterConfig } from '../app/router/RouterConfig';
import { RouteErrorBoundary } from '../app/router/RouteErrorBoundary';

export const App: React.FC = () => {
  return (
    <AppProviders>
      <Router>
        <RouteErrorBoundary>
          <RouterConfig />
        </RouteErrorBoundary>
      </Router>
    </AppProviders>
  );
};

export default App;
