import axiosInstance from '../../../app/axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { DashboardData, DashboardMachine, EnergySummary, RealtimeEnergy } from '../types';

export const getDashboardData = async (): Promise<DashboardData> => {
  const [summaryResponse, realtimeResponse, machinesResponse] = await Promise.all([
    axiosInstance.get<ApiResponse<EnergySummary>>('/api/v1/energy/summary'),
    axiosInstance.get<ApiResponse<RealtimeEnergy>>('/api/v1/energy/realtime'),
    axiosInstance.get<ApiResponse<DashboardMachine[]>>('/api/v1/machines'),
  ]);

  return {
    summary: summaryResponse.data.data,
    realtime: realtimeResponse.data.data,
    machines: machinesResponse.data.data || [],
  };
};
