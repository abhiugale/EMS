import React from 'react';
export interface ContainerProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
}

export const Container: React.FC<ContainerProps> = ({ children, className = '', ...props }) => {
  return (
    <div
      className={`mx-auto w-full max-w-7xl p-token-md md:p-token-lg ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export default Container;
