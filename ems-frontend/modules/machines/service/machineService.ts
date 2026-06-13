import axiosInstance from '../../../app/axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { CreateMachineInput, Machine } from '../types';

export const getMachines = async (): Promise<Machine[]> => {
  const response = await axiosInstance.get<ApiResponse<Machine[]>>('/api/v1/machines');
  return response.data.data || [];
};

export const createMachine = async (input: CreateMachineInput): Promise<Machine> => {
  const response = await axiosInstance.post<ApiResponse<Machine>>('/api/v1/machines', input);
  return response.data.data;
};
