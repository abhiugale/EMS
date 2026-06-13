import { useState, type FormEvent } from 'react';
import { Loader2, Zap } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../app/auth/AuthContext';
import { appRoutes } from '../../../app/router/appRoutes';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { Button, Card, Input, Notice, Text } from '../../../src/design-system';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate(appRoutes.dashboard, { replace: true });
    } catch (loginError) {
      setError(getErrorMessage(loginError, 'Invalid email or password.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-background px-token-md">
      <div className="absolute left-0 top-0 h-1/2 w-1/2 rounded-full bg-primary/10 blur-[120px]" aria-hidden="true" />
      <div className="absolute bottom-0 right-0 h-1/2 w-1/2 rounded-full bg-accent/10 blur-[120px]" aria-hidden="true" />

      <Card className="relative z-10 w-full max-w-md p-token-xl">
        <div className="mb-token-xl flex flex-col items-center">
          <Card variant="solid" className="mb-token-sm flex h-12 w-12 items-center justify-center text-primary">
            <Zap className="h-6 w-6" />
          </Card>
          <Text variant="h2">Welcome Back</Text>
          <Text variant="caption" className="mt-token-xs text-muted">Sign in to monitor energy consumption</Text>
        </div>

        {error && <Notice tone="error" className="mb-token-lg">{error}</Notice>}

        <form onSubmit={handleSubmit} className="flex flex-col gap-token-md">
          <Input label="Email Address" type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.target.value)} />
          <Input label="Password" type="password" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.target.value)} />
          <Button type="submit" size="lg" className="mt-token-sm w-full" disabled={loading}>
            {loading && <Loader2 className="h-4 w-4 animate-spin" />}
            {loading ? 'Signing In...' : 'Sign In'}
          </Button>
        </form>
      </Card>
    </main>
  );
};

export default LoginPage;
