import React from 'react';
export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'glass' | 'solid' | 'outline';
  children: React.ReactNode;
}

const variantStyles = {
  glass: 'glass-card',
  solid: 'border border-border/70 bg-surface/80',
  outline: 'border border-border bg-transparent',
};

export const Card: React.FC<CardProps> = ({ variant = 'glass', children, className = '', ...props }) => {

  return (
    <div
      className={`rounded-xl transition-all duration-300 ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export default Card;
