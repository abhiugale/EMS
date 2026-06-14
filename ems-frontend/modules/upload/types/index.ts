export type UploadStatus = 'SUCCESS' | 'PROCESSING' | 'FAILED' | string;

export interface UploadRecord {
  id: string;
  filename: string;
  rowCount?: number;
  status: UploadStatus;
}

/** Wide format: one Excel row = one machine reading at one timestamp */
export interface WideColumnMapping {
  // Required
  timestamp: string;
  machine_name: string;
  energy_kwh: string;
  // Optional
  active_kw?: string;
  apparent_kva?: string;
  reactive_kvar?: string;
  power_factor?: string;
  frequency?: string;
  voltage_r?: string;
  voltage_y?: string;
  voltage_b?: string;
  current_r?: string;
  current_y?: string;
  current_b?: string;
  parts_produced?: string;
}

/**
 * Narrow (pivot) format: Timestamp | Device_ID | Tag | Value
 * The backend auto-maps tag names to system fields.
 */
export interface NarrowColumnMapping {
  timestamp: string;    // column name that holds the timestamp
  machine_name: string; // column name that holds the device / machine ID
  tag_col: string;      // column name that holds the tag / parameter name
  value_col: string;    // column name that holds the numeric value
}

export type ColumnMapping = WideColumnMapping | NarrowColumnMapping;

export type FileFormat = 'WIDE' | 'NARROW';

export interface HeaderPreview {
  headers: string[];
  format: FileFormat;
}

export interface UploadSpreadsheetInput {
  file: File;
  timezone: string;
  mapping: ColumnMapping;
  formatType: FileFormat;
}
