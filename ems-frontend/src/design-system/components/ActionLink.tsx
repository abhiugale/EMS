import React from 'react';

export interface ActionLinkProps extends React.AnchorHTMLAttributes<HTMLAnchorElement> {
  size?: 'sm' | 'md' | 'icon';
}

const sizeStyles = {
  sm: 'px-token-sm py-token-xs text-xs',
  md: 'px-token-md py-token-sm text-sm',
  icon: 'h-9 w-9 p-token-sm',
};

export const ActionLink: React.FC<ActionLinkProps> = ({
  size = 'md',
  className = '',
  children,
  ...props
}) => (
  <a
    className={`inline-flex items-center justify-center gap-token-sm rounded-lg bg-primary font-semibold text-primary-foreground transition-colors hover:bg-primary-hover focus:outline-none focus:ring-2 focus:ring-primary/40 ${sizeStyles[size]} ${className}`}
    {...props}
  >
    {children}
  </a>
);

export default ActionLink;
