import React from 'react';

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, className = '', children, ...props }, ref) => (
    <div className="flex w-full flex-col gap-token-xs">
      {label && <label className="text-sm font-semibold text-muted">{label}</label>}
      <select
        ref={ref}
        aria-invalid={Boolean(error)}
        className={`rounded-lg border bg-surface-raised/50 px-token-md py-token-sm text-foreground outline-none transition-all focus:border-primary focus:ring-2 focus:ring-primary/30 ${error ? 'border-error' : 'border-border'} ${className}`}
        {...props}
      >
        {children}
      </select>
      {error && <span className="text-xs text-error">{error}</span>}
    </div>
  ),
);

Select.displayName = 'Select';

export default Select;
