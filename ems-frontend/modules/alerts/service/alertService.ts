import axiosInstance from '../../../app/axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { Alert, ResolveAlertInput } from '../types';

export const getAlerts = async (): Promise<Alert[]> => {
  const response = await axiosInstance.get<ApiResponse<Alert[]>>('/api/v1/alerts');
  return response.data.data || [];
};

export const resolveAlert = async ({ id, resolutionNotes }: ResolveAlertInput): Promise<void> => {
  await axiosInstance.put(`/api/v1/alerts/${id}/resolve`, { resolutionNotes });
};
