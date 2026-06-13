export interface HourlyLoadPoint {
  hour: number;
  maxKw: number;
  avgKw: number;
}

export interface EnergySummary {
  totalKwh: number;
  totalCost: number;
  averagePowerFactor: number;
  hourlyCurve: HourlyLoadPoint[];
}

export interface RealtimeEnergy {
  timestamp: string;
  activeKw: number;
  apparentKva: number;
  powerFactor: number;
  frequency: number;
  voltage: number;
  current: number;
}

export interface DashboardMachine {
  id: string;
  status: 'RUNNING' | 'IDLE' | 'OFFLINE' | string;
}

export interface DashboardData {
  summary: EnergySummary;
  realtime: RealtimeEnergy;
  machines: DashboardMachine[];
}
