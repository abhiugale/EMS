export interface Machine {
  id: string;
  name: string;
  type: string;
  department: string;
  baselineKwh: number;
  isActive: boolean;
  status: 'RUNNING' | 'IDLE' | 'OFFLINE' | string;
}

export interface CreateMachineInput {
  name: string;
  type: string;
  department: string;
  baselineKwh: number;
  factoryId?: string;
}
