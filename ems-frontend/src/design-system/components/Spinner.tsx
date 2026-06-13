import React from 'react';

export interface SpinnerProps {
  label?: string;
  size?: 'sm' | 'md' | 'lg';
}

const sizeStyles = {
  sm: 'h-4 w-4 border-2',
  md: 'h-8 w-8 border-4',
  lg: 'h-10 w-10 border-4',
};

export const Spinner: React.FC<SpinnerProps> = ({ label = 'Loading', size = 'md' }) => (
  <span className="inline-flex items-center gap-token-sm text-muted" role="status">
    <span className={`animate-spin rounded-full border-primary border-t-transparent ${sizeStyles[size]}`} />
    <span className="sr-only">{label}</span>
  </span>
);

export default Spinner;
