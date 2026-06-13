import { useState, type FormEvent } from 'react';
import { DollarSign, Zap } from 'lucide-react';
import { useAuth } from '../../../app/auth/AuthContext';
import { Page, PageHeader, PageLoading } from '../../../common/components/Page';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { Badge, Button, Card, Notice, Text, Textarea } from '../../../src/design-system';
import { useInsights, useResolveInsight } from '../hooks/useInsights';

export const InsightsPage = () => {
  const { user } = useAuth();
  const { data: insights = [], isLoading, isError } = useInsights();
  const resolveInsight = useResolveInsight();
  const [resolvingId, setResolvingId] = useState<string | null>(null);
  const [notes, setNotes] = useState('');
  const isEditor = user?.role === 'ADMIN' || user?.role === 'ENERGY_MGR';

  const handleResolve = async (event: FormEvent) => {
    event.preventDefault();
    if (!resolvingId || !notes.trim()) return;
    await resolveInsight.mutateAsync({ id: resolvingId, resolutionNotes: notes.trim() });
    setResolvingId(null);
    setNotes('');
  };

  if (isLoading) return <PageLoading />;

  return (
    <Page>
      <PageHeader title="AI Energy Insights" description="Smart peak-shaving recommendations and carbon footprint optimizations" />
      {isError && <Notice tone="error">Unable to load energy insights.</Notice>}

      <div className="grid grid-cols-1 gap-token-lg md:grid-cols-2">
        {insights.map((insight) => (
          <Card key={insight.id} className="flex flex-col justify-between p-token-lg">
            <div>
              <div className="mb-token-md flex items-center justify-between">
                <Badge tone="accent" className="uppercase">{insight.insightType}</Badge>
                <Badge tone={insight.status === 'RESOLVED' ? 'success' : 'error'}>{insight.status}</Badge>
              </div>
              <Text className="mb-token-md font-semibold">{insight.message}</Text>

              <Card variant="solid" className="mb-token-md grid grid-cols-2 gap-token-md p-token-sm">
                <div className="flex items-center gap-token-sm">
                  <Zap className="h-4 w-4 text-warning" />
                  <div>
                    <Text variant="caption" className="font-semibold uppercase text-subtle">kWh Savings</Text>
                    <Text variant="caption" className="font-bold">{insight.savingsPotentialKwh?.toFixed(1) || 0} kWh</Text>
                  </div>
                </div>
                <div className="flex items-center gap-token-sm">
                  <DollarSign className="h-4 w-4 text-success" />
                  <div>
                    <Text variant="caption" className="font-semibold uppercase text-subtle">INR Savings</Text>
                    <Text variant="caption" className="font-bold text-success">₹{insight.savingsPotentialInr?.toFixed(0) || 0}</Text>
                  </div>
                </div>
              </Card>

              {insight.resolutionNotes && (
                <Notice>
                  <Text variant="caption" className="font-bold">Resolution Details</Text>
                  <Text variant="caption">{insight.resolutionNotes}</Text>
                </Notice>
              )}
            </div>

            {insight.status !== 'RESOLVED' && isEditor && (
              <Button className="mt-token-md w-full" onClick={() => setResolvingId(insight.id)}>Mark as Actioned</Button>
            )}
          </Card>
        ))}
      </div>

      {resolvingId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-overlay/70 px-token-md backdrop-blur-sm">
          <Card className="w-full max-w-md p-token-lg" role="dialog" aria-modal="true" aria-labelledby="insight-dialog-title">
            <Text id="insight-dialog-title" variant="h3">Confirm Recommendation Action</Text>
            <Text variant="caption" className="mb-token-md mt-token-xs text-muted">
              Describe the scheduling or maintenance actions taken to address this advice.
            </Text>
            {resolveInsight.isError && (
              <Notice tone="error" className="mb-token-md">
                {getErrorMessage(resolveInsight.error, 'Failed to resolve recommendation.')}
              </Notice>
            )}
            <form onSubmit={handleResolve} className="flex flex-col gap-token-md">
              <Textarea label="Resolution Notes" required rows={3} value={notes} onChange={(event) => setNotes(event.target.value)} />
              <div className="flex justify-end gap-token-sm">
                <Button type="button" variant="secondary" onClick={() => setResolvingId(null)}>Cancel</Button>
                <Button type="submit" disabled={resolveInsight.isPending}>Confirm Action</Button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </Page>
  );
};

export default InsightsPage;
