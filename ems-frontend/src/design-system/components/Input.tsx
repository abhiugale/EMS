import React from 'react';
export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, className = '', ...props }, ref) => {
    return (
      <div className="flex w-full flex-col gap-token-xs">
        {label && (
          <label className="text-sm font-semibold text-muted">
            {label}
          </label>
        )}
        <input
          ref={ref}
          aria-invalid={Boolean(error)}
          className={`rounded-lg border bg-surface-raised/50 px-token-md py-token-sm text-foreground outline-none transition-all placeholder:text-subtle focus:border-primary focus:ring-2 focus:ring-primary/30 ${error ? 'border-error' : 'border-border'} ${className}`}
          {...props}
        />
        {error && (
          <span className="text-xs text-error">
            {error}
          </span>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
export default Input;
