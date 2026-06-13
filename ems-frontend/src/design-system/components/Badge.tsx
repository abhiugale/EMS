import React from 'react';

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  tone?: 'neutral' | 'primary' | 'info' | 'success' | 'warning' | 'error' | 'accent';
}

const toneStyles = {
  neutral: 'border-border bg-surface-raised/60 text-muted',
  primary: 'border-primary/40 bg-primary/15 text-primary',
  info: 'border-info/40 bg-info/15 text-info',
  success: 'border-success/40 bg-success/15 text-success',
  warning: 'border-warning/40 bg-warning/15 text-warning',
  error: 'border-error/40 bg-error/15 text-error',
  accent: 'border-accent/40 bg-accent/15 text-accent',
};

export const Badge: React.FC<BadgeProps> = ({ tone = 'neutral', className = '', children, ...props }) => (
  <span
    className={`inline-flex items-center gap-token-xs rounded-full border px-token-sm py-token-xs text-xs font-bold ${toneStyles[tone]} ${className}`}
    {...props}
  >
    {children}
  </span>
);

export default Badge;
