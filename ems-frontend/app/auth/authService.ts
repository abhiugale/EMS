import axiosInstance from '../axios/instance';
import type { ApiResponse } from '../../common/types/api';

export interface AuthUser {
  id: string;
  email: string;
  role: string;
  firstName?: string;
  lastName?: string;
  factoryId?: string;
  factoryName?: string;
}

interface LoginPayload {
  accessToken: string;
  refreshToken: string;
  user: AuthUser;
}

export const loginRequest = async (email: string, password: string): Promise<LoginPayload> => {
  const response = await axiosInstance.post<ApiResponse<LoginPayload>>('/api/v1/auth/login', { email, password });
  return response.data.data;
};

export const logoutRequest = async (): Promise<void> => {
  await axiosInstance.post('/api/v1/auth/logout');
};
