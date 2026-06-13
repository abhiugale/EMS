export interface Alert {
  id: string;
  machineName: string;
  alertType: string;
  severity: 'WARNING' | 'CRITICAL' | string;
  message: string;
  status: 'OPEN' | 'RESOLVED' | string;
  thresholdValue?: number;
  actualValue?: number;
  createdAt: string;
  resolvedAt?: string;
  resolvedByEmail?: string;
}

export interface ResolveAlertInput {
  id: string;
  resolutionNotes: string;
}
