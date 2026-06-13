import axiosInstance from '../../../app/axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { Insight, ResolveInsightInput } from '../types';

export const getInsights = async (): Promise<Insight[]> => {
  const response = await axiosInstance.get<ApiResponse<Insight[]>>('/api/v1/insights');
  return response.data.data || [];
};

export const resolveInsight = async ({ id, resolutionNotes }: ResolveInsightInput): Promise<void> => {
  await axiosInstance.put(`/api/v1/insights/${id}/resolve`, { resolutionNotes });
};
