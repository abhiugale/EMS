import React from 'react';

export interface TextProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'h1' | 'h2' | 'h3' | 'body' | 'caption';
  as?: 'h1' | 'h2' | 'h3' | 'p' | 'span';
  children: React.ReactNode;
}

const variantStyles = {
  h1: 'text-4xl font-bold leading-10',
  h2: 'text-2xl font-bold leading-8',
  h3: 'text-xl font-semibold leading-7',
  body: 'text-base font-normal leading-6',
  caption: 'text-sm font-normal leading-5',
};

export const Text: React.FC<TextProps> = ({ variant = 'body', as, children, className = '', ...props }) => {
  const Component = as || (variant === 'h1' ? 'h1' : variant === 'h2' ? 'h2' : variant === 'h3' ? 'h3' : variant === 'body' ? 'p' : 'span');

  return (
    <Component
      className={`text-foreground ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {children}
    </Component>
  );
};

export default Text;
