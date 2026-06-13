import { useState, type FormEvent } from 'react';
import { AlertTriangle, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../../../app/auth/AuthContext';
import { Page, PageHeader, PageLoading } from '../../../common/components/Page';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { Badge, Button, Card, Notice, Text, Textarea } from '../../../src/design-system';
import { useAlerts, useResolveAlert } from '../hooks/useAlerts';
import type { Alert } from '../types';

const alertTone = (alert: Alert): 'neutral' | 'warning' | 'error' => {
  if (alert.status !== 'OPEN') return 'neutral';
  return alert.severity === 'CRITICAL' ? 'error' : 'warning';
};

const alertCardStyles = (alert: Alert): string => {
  if (alert.status !== 'OPEN') return 'border-border bg-surface/30';
  return alert.severity === 'CRITICAL'
    ? 'border-error/60 bg-error/10'
    : 'border-warning/60 bg-warning/10';
};

export const AlertsPage = () => {
  const { user } = useAuth();
  const { data: alerts = [], isLoading, isError } = useAlerts(user?.factoryId);
  const resolveAlert = useResolveAlert();
  const [resolvingId, setResolvingId] = useState<string | null>(null);
  const [notes, setNotes] = useState('');
  const isEditor = user?.role === 'ADMIN' || user?.role === 'ENERGY_MGR';

  const handleResolve = async (event: FormEvent) => {
    event.preventDefault();
    if (!resolvingId || !notes.trim()) return;
    await resolveAlert.mutateAsync({ id: resolvingId, resolutionNotes: notes.trim() });
    setResolvingId(null);
    setNotes('');
  };

  if (isLoading) return <PageLoading />;

  return (
    <Page>
      <PageHeader title="Security & Operational Alerts" description="Real-time anomaly warnings, power spikes, and device errors" />
      {isError && <Notice tone="error">Unable to load operational alerts.</Notice>}

      <div className="flex flex-col gap-token-md">
        {alerts.map((alert) => (
          <Card
            key={alert.id}
            variant="outline"
            className={`flex flex-col items-start justify-between gap-token-md p-token-lg md:flex-row md:items-center ${alertCardStyles(alert)}`}
          >
            <div className="flex items-start gap-token-md">
              <Card variant="solid" className={`p-token-sm ${alert.status === 'OPEN' ? alert.severity === 'CRITICAL' ? 'text-error' : 'text-warning' : 'text-subtle'}`}>
                {alert.status === 'OPEN' ? <AlertTriangle className="h-6 w-6" /> : <CheckCircle2 className="h-6 w-6" />}
              </Card>
              <div>
                <div className="mb-token-xs flex items-center gap-token-sm">
                  <Text variant="h3" className="text-base">{alert.machineName} Anomaly</Text>
                  <Badge tone={alertTone(alert)} className="uppercase">{alert.severity}</Badge>
                </div>
                <Text className="font-medium">{alert.message}</Text>
                <Text variant="caption" className="mt-token-xs text-subtle">
                  Detected at {new Date(alert.createdAt).toLocaleString()}
                  {alert.resolvedByEmail ? ` - Resolved by ${alert.resolvedByEmail}` : ''}
                </Text>
              </div>
            </div>

            {alert.status === 'OPEN' && isEditor ? (
              <Button size="sm" className="shrink-0" onClick={() => setResolvingId(alert.id)}>Mark as Resolved</Button>
            ) : alert.status === 'RESOLVED' ? (
              <Badge tone="success" className="shrink-0"><CheckCircle2 className="h-3 w-3" />Resolved</Badge>
            ) : null}
          </Card>
        ))}
      </div>

      {resolvingId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-overlay/70 px-token-md backdrop-blur-sm">
          <Card className="w-full max-w-md p-token-lg" role="dialog" aria-modal="true" aria-labelledby="alert-dialog-title">
            <Text id="alert-dialog-title" variant="h3">Resolve Alert</Text>
            <Text variant="caption" className="mb-token-md mt-token-xs text-muted">
              Provide details on the intervention made to fix this anomaly.
            </Text>
            {resolveAlert.isError && (
              <Notice tone="error" className="mb-token-md">
                {getErrorMessage(resolveAlert.error, 'Failed to resolve alert.')}
              </Notice>
            )}
            <form onSubmit={handleResolve} className="flex flex-col gap-token-md">
              <Textarea label="Intervention Notes" required rows={3} value={notes} onChange={(event) => setNotes(event.target.value)} />
              <div className="flex justify-end gap-token-sm">
                <Button type="button" variant="secondary" onClick={() => setResolvingId(null)}>Cancel</Button>
                <Button type="submit" disabled={resolveAlert.isPending}>Save Intervention</Button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </Page>
  );
};

export default AlertsPage;
