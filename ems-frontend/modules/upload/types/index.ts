export type UploadStatus = 'SUCCESS' | 'PROCESSING' | 'FAILED' | string;

export interface UploadRecord {
  id: string;
  filename: string;
  rowCount?: number;
  status: UploadStatus;
}

export interface UploadSpreadsheetInput {
  file: File;
  timezone: string;
  mapping: {
    timestamp: string;
    machine_name: string;
    energy_kwh: string;
  };
}
