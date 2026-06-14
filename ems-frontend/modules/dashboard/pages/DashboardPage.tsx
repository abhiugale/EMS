import ReactECharts from 'echarts-for-react';
import { Activity, Award, Cpu, Wifi, WifiOff } from 'lucide-react';
import { Page, PageHeader, PageLoading } from '../../../common/components/Page';
import { Card, Notice, Text, useTheme } from '../../../src/design-system';
import { useAuth } from '../../../app/auth/AuthContext';
import { useDashboard } from '../hooks/useDashboard';
import { useRealtimeWebSocket } from '../hooks/useRealtimeWebSocket';

/* ─────────────────────────────────────────────────────────────
   Connection status indicator
   
───────────────────────────────────────────────────────────── */
const WsStatusDot = ({ status }: { status: string }) => {
  const styles: Record<string, string> = {
    CONNECTED:    'bg-green-400',
    CONNECTING:   'bg-yellow-400 animate-pulse',
    DISCONNECTED: 'bg-gray-400',
    ERROR:        'bg-red-500',
  };
  const labels: Record<string, string> = {
    CONNECTED: 'Live', CONNECTING: 'Connecting…', DISCONNECTED: 'Offline', ERROR: 'WS Error',
  };
  return (
    <span className="flex items-center gap-1.5 text-xs text-muted">
      <span className={`h-2 w-2 rounded-full ${styles[status] ?? 'bg-gray-400'}`} />
      {labels[status] ?? status}
    </span>
  );
};

export const DashboardPage = () => {
  const theme = useTheme();
  const { user } = useAuth();
  const { data, isLoading, isError } = useDashboard();

  // WebSocket — supplements REST poll with live push every 5 s
  const { realtimeWs, status: wsStatus } = useRealtimeWebSocket({
    factoryId: user?.factoryId,
  });

  if (isLoading) return <PageLoading />;
  if (isError || !data) {
    return <Page><Notice tone="error">Unable to load dashboard telemetry.</Notice></Page>;
  }

  const { summary, realtime: restRealtime, machines } = data;

  // Prefer the WebSocket push; fall back to REST snapshot until WS delivers
  const realtime = realtimeWs ?? restRealtime;

  const hours = summary.hourlyCurve.map((point) => `${point.hour}:00`);
  const loadCurveOption = {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: theme.colors.muted } },
    xAxis: {
      type: 'category',
      data: hours,
      axisLabel: { color: theme.colors.muted },
      axisLine: { lineStyle: { color: theme.colors.borderStrong } },
    },
    yAxis: {
      type: 'value',
      name: 'kW',
      nameTextStyle: { color: theme.colors.muted },
      axisLabel: { color: theme.colors.muted },
      splitLine: { lineStyle: { color: theme.colors.secondary } },
    },
    series: [
      {
        name: 'Average kW',
        type: 'line',
        smooth: true,
        data: summary.hourlyCurve.map((point) => point.avgKw),
        lineStyle: { width: 3, color: theme.colors.primary },
        itemStyle: { color: theme.colors.primary },
      },
      {
        name: 'Max Peak kW',
        type: 'line',
        smooth: true,
        data: summary.hourlyCurve.map((point) => point.maxKw),
        lineStyle: { width: 2, color: theme.colors.chartSecondary, type: 'dashed' },
        itemStyle: { color: theme.colors.chartSecondary },
      },
    ],
  };

  const activeCount  = machines.filter((m) => m.status === 'RUNNING').length;
  const idleCount    = machines.filter((m) => m.status === 'IDLE').length;
  const offlineCount = machines.filter((m) => m.status === 'OFFLINE').length;

  const overview = [
    { label: 'Active Machines',  value: `${activeCount} / ${machines.length}`, tone: 'text-success' },
    { label: 'Idle Machines',    value: idleCount,    tone: 'text-warning' },
    { label: 'Offline Machines', value: offlineCount, tone: 'text-error' },
  ];

  return (
    <Page>
      <PageHeader
        title="Live Monitoring Dashboard"
        description="Real-time status updates and telemetry feeds"
      />

      {/* Status row */}
      <div className="mb-token-sm flex items-center justify-end gap-token-sm">
        {wsStatus === 'CONNECTED'
          ? <Wifi className="h-3.5 w-3.5 text-green-400" />
          : <WifiOff className="h-3.5 w-3.5 text-gray-400" />}
        <WsStatusDot status={wsStatus} />
      </div>

      <div className="grid grid-cols-1 gap-token-md md:grid-cols-4">
        {overview.map((item) => (
          <Card key={item.label} className="flex items-center justify-between p-token-md">
            <div>
              <Text variant="caption" className="font-semibold uppercase text-muted">{item.label}</Text>
              <Text variant="h2" className="mt-token-xs">{item.value}</Text>
            </div>
            <Cpu className={`h-8 w-8 ${item.tone}`} aria-hidden="true" />
          </Card>
        ))}
        <Card className="flex items-center justify-between p-token-md">
          <div>
            <Text variant="caption" className="font-semibold uppercase text-muted">System Power Factor</Text>
            <Text variant="h2" className="mt-token-xs">{realtime?.powerFactor?.toFixed(3) ?? '—'}</Text>
          </div>
          <Award className="h-8 w-8 text-primary" aria-hidden="true" />
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-token-lg lg:grid-cols-3">
        <Card className="p-token-lg lg:col-span-2">
          <Text variant="h3" className="mb-token-md">Load Profile Curve (Today)</Text>
          <div className="h-80">
            <ReactECharts option={loadCurveOption} style={{ height: '100%', width: '100%' }} />
          </div>
        </Card>

        <Card className="flex flex-col justify-between p-token-lg">
          <div>
            <Text variant="h3" className="mb-token-md">Aggregated Telemetry Feed</Text>
            <div className="flex flex-col gap-token-md">
              {[
                ['System Voltage',    `${realtime?.voltage?.toFixed(1)       ?? '—'} V`],
                ['System Frequency',  `${realtime?.frequency?.toFixed(2)     ?? '—'} Hz`],
                ['Aggregated Current',`${realtime?.current?.toFixed(1)       ?? '—'} A`],
                ['Apparent Power',    `${realtime?.apparentKva?.toFixed(1)   ?? '—'} kVA`],
                ['Active Power Load', `${realtime?.activeKw?.toFixed(1)      ?? '—'} kW`],
              ].map(([label, value]) => (
                <div key={label} className="flex items-center justify-between border-b border-border/70 pb-token-sm last:border-0">
                  <Text variant="caption" className="text-muted">{label}</Text>
                  <Text variant="caption" className="font-semibold">{value}</Text>
                </div>
              ))}
            </div>
          </div>

          <Card variant="solid" className="mt-token-md flex items-center gap-token-sm p-token-md">
            <Activity className="h-5 w-5 animate-pulse text-primary" aria-hidden="true" />
            <div>
              <Text variant="caption" className="font-bold uppercase text-subtle">
                {wsStatus === 'CONNECTED' ? 'WebSocket · Live' : 'REST Poll · 10 s'}
              </Text>
              <Text variant="caption">
                {realtime?.timestamp
                  ? new Date(realtime.timestamp).toLocaleTimeString()
                  : 'Waiting for data…'}
              </Text>
            </div>
          </Card>
        </Card>
      </div>
    </Page>
  );
};

export default DashboardPage;
