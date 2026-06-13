export interface Insight {
  id: string;
  machineName: string;
  insightType: string;
  message: string;
  savingsPotentialKwh: number;
  savingsPotentialInr: number;
  status: 'OPEN' | 'RESOLVED' | string;
  resolutionNotes?: string;
  createdAt: string;
}

export interface ResolveInsightInput {
  id: string;
  resolutionNotes: string;
}
