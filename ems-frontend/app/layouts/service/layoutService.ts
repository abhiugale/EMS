import axiosInstance from '../../axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { CeoSummary } from '../types';

export const getCeoSummary = async (): Promise<CeoSummary> => {
  const response = await axiosInstance.get<ApiResponse<CeoSummary>>('/api/v1/energy/ceo-summary');
  return response.data.data;
};
