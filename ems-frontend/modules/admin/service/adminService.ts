import axiosInstance from '../../../app/axios/instance';
import type { RegisterUserInput } from '../types';

export const registerUser = async (input: RegisterUserInput): Promise<void> => {
  await axiosInstance.post('/api/v1/auth/register', input);
};
