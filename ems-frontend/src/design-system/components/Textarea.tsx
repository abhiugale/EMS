import React from 'react';

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
}

export const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, error, className = '', ...props }, ref) => (
    <div className="flex w-full flex-col gap-token-xs">
      {label && <label className="text-sm font-semibold text-muted">{label}</label>}
      <textarea
        ref={ref}
        aria-invalid={Boolean(error)}
        className={`resize-none rounded-lg border bg-surface-raised/50 px-token-md py-token-sm text-foreground outline-none transition-all placeholder:text-subtle focus:border-primary focus:ring-2 focus:ring-primary/30 ${error ? 'border-error' : 'border-border'} ${className}`}
        {...props}
      />
      {error && <span className="text-xs text-error">{error}</span>}
    </div>
  ),
);

Textarea.displayName = 'Textarea';

export default Textarea;
