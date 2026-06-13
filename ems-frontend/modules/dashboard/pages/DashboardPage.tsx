import ReactECharts from 'echarts-for-react';
import { Activity, Award, Cpu } from 'lucide-react';
import { Page, PageHeader, PageLoading } from '../../../common/components/Page';
import { Card, Notice, Text, useTheme } from '../../../src/design-system';
import { useDashboard } from '../hooks/useDashboard';

export const DashboardPage = () => {
  const theme = useTheme();
  const { data, isLoading, isError } = useDashboard();

  if (isLoading) return <PageLoading />;
  if (isError || !data) {
    return <Page><Notice tone="error">Unable to load dashboard telemetry.</Notice></Page>;
  }

  const { summary, realtime, machines } = data;
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

  const activeCount = machines.filter((machine) => machine.status === 'RUNNING').length;
  const idleCount = machines.filter((machine) => machine.status === 'IDLE').length;
  const offlineCount = machines.filter((machine) => machine.status === 'OFFLINE').length;

  const overview = [
    { label: 'Active Machines', value: `${activeCount} / ${machines.length}`, tone: 'text-success' },
    { label: 'Idle Machines', value: idleCount, tone: 'text-warning' },
    { label: 'Offline Machines', value: offlineCount, tone: 'text-error' },
  ];

  return (
    <Page>
      <PageHeader title="Live Monitoring Dashboard" description="Real-time status updates and telemetry feeds" />

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
            <Text variant="h2" className="mt-token-xs">{realtime.powerFactor?.toFixed(3) || '1.000'}</Text>
          </div>
          <Award className="h-8 w-8 text-primary" aria-hidden="true" />
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-token-lg lg:grid-cols-3">
        <Card className="p-token-lg lg:col-span-2">
          <Text variant="h3" className="mb-token-md">Load Profile Curve (Today)</Text>
          <div className="h-80">
            {/* ECharts requires a runtime size on its generated wrapper. */}
            <ReactECharts option={loadCurveOption} style={{ height: '100%', width: '100%' }} />
          </div>
        </Card>

        <Card className="flex flex-col justify-between p-token-lg">
          <div>
            <Text variant="h3" className="mb-token-md">Aggregated Telemetry Feed</Text>
            <div className="flex flex-col gap-token-md">
              {[
                ['System Voltage', `${realtime.voltage?.toFixed(1) || 415.0} V`],
                ['System Frequency', `${realtime.frequency?.toFixed(2) || 50.0} Hz`],
                ['Aggregated Current', `${realtime.current?.toFixed(1) || 0.0} A`],
                ['Apparent Power', `${realtime.apparentKva?.toFixed(1) || 0.0} kVA`],
                ['Active Power Load', `${realtime.activeKw?.toFixed(1) || 0.0} kW`],
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
              <Text variant="caption" className="font-bold uppercase text-subtle">Latest Update</Text>
              <Text variant="caption">{realtime.timestamp ? new Date(realtime.timestamp).toLocaleTimeString() : 'N/A'}</Text>
            </div>
          </Card>
        </Card>
      </div>
    </Page>
  );
};

export default DashboardPage;
