export const theme = {
  colors: {
    background: '#030712',
    surface: '#111827',
    surfaceRaised: '#1f2937',
    foreground: '#f3f4f6',
    muted: '#9ca3af',
    subtle: '#6b7280',
    primary: '#3b82f6',
    primaryHover: '#2563eb',
    primarySoft: 'rgba(59, 130, 246, 0.18)',
    primaryForeground: '#ffffff',
    secondary: '#1f2937',
    secondaryHover: '#374151',
    accent: '#a78bfa',
    info: '#60a5fa',
    error: '#ef4444',
    errorSoft: 'rgba(127, 29, 29, 0.4)',
    success: '#10b981',
    successSoft: 'rgba(6, 78, 59, 0.4)',
    warning: '#f59e0b',
    warningSoft: 'rgba(120, 53, 15, 0.4)',
    border: 'rgba(255, 255, 255, 0.08)',
    borderStrong: '#374151',
    cardBg: 'rgba(17, 24, 39, 0.7)',
    cardHoverBorder: 'rgba(59, 130, 246, 0.25)',
    overlay: 'rgba(0, 0, 0, 0.65)',
    chartSecondary: '#ec4899',
  },
  spacing: {
    none: '0',
    xs: '4px',
    sm: '8px',
    md: '16px',
    lg: '24px',
    xl: '32px',
    xxl: '48px',
  },
  typography: {
    h1: {
      fontSize: '2.25rem',
      fontWeight: '700',
      lineHeight: '2.5rem',
    },
    h2: {
      fontSize: '1.5rem',
      fontWeight: '700',
      lineHeight: '2rem',
    },
    h3: {
      fontSize: '1.25rem',
      fontWeight: '600',
      lineHeight: '1.75rem',
    },
    body: {
      fontSize: '1rem',
      fontWeight: '400',
      lineHeight: '1.5rem',
    },
    caption: {
      fontSize: '0.875rem',
      fontWeight: '400',
      lineHeight: '1.25rem',
    },
  },
};

export type Theme = typeof theme;
export default theme;
