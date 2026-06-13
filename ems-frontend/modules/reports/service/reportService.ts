import axiosInstance from '../../../app/axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { GenerateReportInput, Report } from '../types';

export const getReports = async (): Promise<Report[]> => {
  const response = await axiosInstance.get<ApiResponse<Report[]>>('/api/v1/reports');
  return response.data.data || [];
};

export const generateReport = async ({ reportType, targetDate }: GenerateReportInput): Promise<void> => {
  await axiosInstance.post('/api/v1/reports', undefined, {
    params: { date: targetDate, reportType },
  });
};

export const getReportDownloadUrl = (filePath: string): string => (
  `/api/v1/files/download?path=${encodeURIComponent(filePath)}`
);
