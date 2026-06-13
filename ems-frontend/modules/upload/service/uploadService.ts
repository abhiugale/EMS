import axiosInstance from '../../../app/axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { UploadRecord, UploadSpreadsheetInput } from '../types';

export const getUploads = async (): Promise<UploadRecord[]> => {
  const response = await axiosInstance.get<ApiResponse<UploadRecord[]>>('/api/v1/uploads');
  return response.data.data || [];
};

export const uploadSpreadsheet = async ({ file, timezone, mapping }: UploadSpreadsheetInput): Promise<void> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('timezone', timezone);
  formData.append('mapping', JSON.stringify(mapping));
  await axiosInstance.post('/api/v1/uploads', formData);
};
