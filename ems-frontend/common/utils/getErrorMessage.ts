import axios from 'axios';
import type { ApiErrorResponse } from '../types/api';

export const getErrorMessage = (error: unknown, fallback: string): string => {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.message || fallback;
  }

  return error instanceof Error ? error.message : fallback;
};
