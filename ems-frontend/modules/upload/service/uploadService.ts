import axiosInstance from '../../../app/axios/instance';
import type { ApiResponse } from '../../../common/types/api';
import type { HeaderPreview, UploadRecord, UploadSpreadsheetInput } from '../types';

export const getUploads = async (): Promise<UploadRecord[]> => {
  const response = await axiosInstance.get<ApiResponse<UploadRecord[]>>('/api/v1/uploads');
  return response.data.data || [];
};

/**
 * Sends the file to the backend to extract its column headers and detect format type.
 * Returns { headers: string[], format: 'WIDE' | 'NARROW' }
 */
export const previewHeaders = async (file: File): Promise<HeaderPreview> => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await axiosInstance.post<ApiResponse<HeaderPreview>>(
    '/api/v1/uploads/preview',
    formData,
  );
  return response.data.data ?? { headers: [], format: 'WIDE' };
};

export const uploadSpreadsheet = async ({
  file,
  timezone,
  mapping,
  formatType,
}: UploadSpreadsheetInput): Promise<void> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('timezone', timezone);
  formData.append('mapping', JSON.stringify(mapping));
  formData.append('formatType', formatType);
  await axiosInstance.post('/api/v1/uploads', formData);
};
