import React from 'react';
export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg' | 'icon';
  children: React.ReactNode;
}

const variantStyles = {
  primary: 'bg-primary text-primary-foreground hover:bg-primary-hover',
  secondary: 'border border-border bg-surface-raised text-foreground hover:bg-border',
  danger: 'bg-error text-primary-foreground hover:bg-error/80',
  ghost: 'bg-transparent text-muted hover:bg-surface-raised hover:text-foreground',
};

const sizeStyles = {
  sm: 'px-token-sm py-token-xs text-xs',
  md: 'px-token-md py-token-sm text-sm',
  lg: 'px-token-lg py-3 text-base',
  icon: 'h-9 w-9 p-token-sm',
};

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  children,
  className = '',
  ...props
}) => {

  return (
    <button
      className={`inline-flex items-center justify-center gap-token-sm rounded-lg font-semibold transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary/40 disabled:cursor-not-allowed disabled:opacity-50 ${variantStyles[variant]} ${sizeStyles[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
