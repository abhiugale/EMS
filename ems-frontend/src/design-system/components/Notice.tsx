import React from 'react';

export interface NoticeProps extends React.HTMLAttributes<HTMLDivElement> {
  tone?: 'info' | 'success' | 'warning' | 'error';
}

const toneStyles = {
  info: 'border-info/40 bg-info/10 text-info',
  success: 'border-success/40 bg-success/10 text-success',
  warning: 'border-warning/40 bg-warning/10 text-warning',
  error: 'border-error/40 bg-error/10 text-error',
};

export const Notice: React.FC<NoticeProps> = ({ tone = 'info', className = '', children, ...props }) => (
  <div className={`rounded-lg border p-token-md text-sm ${toneStyles[tone]} ${className}`} {...props}>
    {children}
  </div>
);

export default Notice;
