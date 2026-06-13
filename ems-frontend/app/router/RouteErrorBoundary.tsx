import React from 'react';
import { Button, Card, Text } from '../../src/design-system';

interface RouteErrorBoundaryState {
  hasError: boolean;
}

export class RouteErrorBoundary extends React.Component<React.PropsWithChildren, RouteErrorBoundaryState> {
  state: RouteErrorBoundaryState = { hasError: false };

  static getDerivedStateFromError(): RouteErrorBoundaryState {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="flex min-h-screen items-center justify-center bg-background p-token-lg">
          <Card className="max-w-md p-token-xl text-center">
            <Text variant="h2">This page could not be rendered</Text>
            <Text className="my-token-md text-muted">Reload the application to try again.</Text>
            <Button onClick={() => window.location.reload()}>Reload</Button>
          </Card>
        </main>
      );
    }

    return this.props.children;
  }
}
