import React from 'react';
import { Container, Spinner, Text } from '../../src/design-system';

export const Page: React.FC<React.PropsWithChildren> = ({ children }) => (
  <Container className="flex flex-grow flex-col gap-token-lg overflow-y-auto">{children}</Container>
);

export interface PageHeaderProps {
  title: string;
  description: string;
  action?: React.ReactNode;
}

export const PageHeader: React.FC<PageHeaderProps> = ({ title, description, action }) => (
  <div className="flex flex-col justify-between gap-token-md md:flex-row md:items-center">
    <div>
      <Text variant="h2">{title}</Text>
      <Text variant="caption" className="mt-token-xs text-muted">{description}</Text>
    </div>
    {action}
  </div>
);

export const PageLoading: React.FC = () => (
  <div className="flex min-h-[400px] flex-grow items-center justify-center">
    <Spinner label="Loading page" />
  </div>
);
