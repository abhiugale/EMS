import { Activity, Bell, Flame, ShieldAlert, TrendingDown, Zap } from 'lucide-react';
import { useAuth } from '../../auth/AuthContext';
import { Badge, Button, Card, Text } from '../../../src/design-system';
import { useCeoSummary } from '../hooks/useCeoSummary';

export const Header = () => {
  const { user, logout } = useAuth();
  const { data: metrics } = useCeoSummary(user?.factoryId);

  const summaryItems = metrics ? [
    { label: "Today's Load", value: `${metrics.totalKwhToday?.toFixed(1) || 0} kWh`, icon: Zap, tone: 'text-warning' },
    { label: "Today's Cost", value: `₹${metrics.totalCostTodayInr?.toFixed(0) || 0}`, icon: Flame, tone: 'text-warning' },
    { label: 'Savings vs Yest', value: `${metrics.savingsVsYesterdayPct?.toFixed(1) || 0}%`, icon: TrendingDown, tone: 'text-success' },
    { label: 'Demand kW', value: `${metrics.currentDemandKw?.toFixed(1) || 0} kW`, icon: Activity, tone: 'text-info' },
    { label: 'Power Factor', value: metrics.powerFactor?.toFixed(3) || '1.000', icon: Bell, tone: 'text-accent' },
  ] : [];

  return (
    <header className="glass-panel z-10 flex w-full flex-col items-center justify-between gap-token-md border-b border-border/70 px-token-lg py-token-md lg:flex-row">
      <div className="flex w-full items-center gap-token-sm lg:w-auto">
        <Card variant="solid" className="flex h-9 w-9 items-center justify-center text-primary"><Zap className="h-5 w-5" /></Card>
        <div>
          <Text className="font-bold tracking-wide">EMS PORTAL</Text>
          <Text variant="caption" className="text-muted">{user?.factoryName || 'Energy Management'}</Text>
        </div>
      </div>

      {metrics && (
        <div className="grid w-full max-w-5xl grid-cols-2 gap-token-sm sm:grid-cols-3 xl:grid-cols-6">
          {summaryItems.map((item) => (
            <Card key={item.label} variant="solid" className="flex items-center gap-token-sm p-token-sm">
              <item.icon className={`h-4 w-4 ${item.tone}`} />
              <div>
                <Text variant="caption" className="text-[10px] font-semibold uppercase text-subtle">{item.label}</Text>
                <Text variant="caption" className="font-bold">{item.value}</Text>
              </div>
            </Card>
          ))}
          <Card
            variant="solid"
            className={`flex items-center gap-token-sm p-token-sm ${metrics.activeAlertCount > 0 ? 'border-error/60 bg-error/10 text-error' : 'text-muted'}`}
          >
            <ShieldAlert className="h-4 w-4" />
            <div>
              <Text variant="caption" className="text-[10px] font-semibold uppercase">Active Alerts</Text>
              <Text variant="caption" className="font-bold">{metrics.activeAlertCount || 0} Open</Text>
            </div>
          </Card>
        </div>
      )}

      <div className="flex items-center gap-token-sm">
        <Badge>{user?.role}</Badge>
        <Button variant="ghost" size="sm" onClick={logout}>Sign Out</Button>
      </div>
    </header>
  );
};

export default Header;
