export interface Report {
  id: string;
  reportType: string;
  status: 'PROCESSING' | 'SUCCESS' | 'FAILED' | string;
  filePath?: string;
  createdAt: string;
  completedAt?: string;
  generatedByEmail?: string;
}

export interface GenerateReportInput {
  reportType: string;
  targetDate: string;
}
