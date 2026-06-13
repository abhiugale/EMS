import { useState, type ChangeEvent, type FormEvent } from 'react';
import { CheckCircle2, Clock, FileSpreadsheet, UploadCloud, XCircle } from 'lucide-react';
import { Page, PageHeader } from '../../../common/components/Page';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { Badge, Button, Card, Input, Notice, Select, Text } from '../../../src/design-system';
import { useUploads, useUploadSpreadsheet } from '../hooks/useUploads';

const UploadStatusBadge = ({ status }: { status: string }) => {
  if (status === 'SUCCESS') return <Badge tone="success"><CheckCircle2 className="h-3 w-3" />Success</Badge>;
  if (status === 'PROCESSING') return <Badge tone="info" className="animate-pulse"><Clock className="h-3 w-3" />Pending</Badge>;
  return <Badge tone="error"><XCircle className="h-3 w-3" />Failed</Badge>;
};

export const UploadPage = () => {
  const { data: history = [], isError } = useUploads();
  const uploadSpreadsheet = useUploadSpreadsheet();
  const [file, setFile] = useState<File | null>(null);
  const [timezone, setTimezone] = useState('Asia/Kolkata');
  const [timestampColumn, setTimestampColumn] = useState('Time');
  const [machineColumn, setMachineColumn] = useState('Machine');
  const [energyColumn, setEnergyColumn] = useState('Energy');
  const [validationError, setValidationError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFile(event.target.files?.[0] || null);
    setValidationError('');
    setSuccessMessage('');
  };

  const handleUpload = async (event: FormEvent) => {
    event.preventDefault();
    if (!file) {
      setValidationError('Please select a spreadsheet file first.');
      return;
    }

    setValidationError('');
    setSuccessMessage('');
    await uploadSpreadsheet.mutateAsync({
      file,
      timezone,
      mapping: {
        timestamp: timestampColumn,
        machine_name: machineColumn,
        energy_kwh: energyColumn,
      },
    });
    setFile(null);
    setSuccessMessage('Spreadsheet uploaded and queued for processing.');
  };

  const uploadError = uploadSpreadsheet.isError
    ? getErrorMessage(uploadSpreadsheet.error, 'Spreadsheet upload failed.')
    : validationError;

  return (
    <Page>
      <PageHeader title="Import Time-Series Telemetry" description="Ingest spreadsheet metrics matching target columns" />

      <div className="grid grid-cols-1 gap-token-lg lg:grid-cols-3">
        <Card className="p-token-lg lg:col-span-2">
          <Text variant="h3" className="mb-token-md">Ingestion Wizard</Text>
          {uploadError && <Notice tone="error" className="mb-token-md">{uploadError}</Notice>}
          {successMessage && <Notice tone="success" className="mb-token-md">{successMessage}</Notice>}

          <form onSubmit={handleUpload} className="flex flex-col gap-token-lg">
            <Card variant="outline" className="relative flex cursor-pointer flex-col items-center justify-center gap-token-sm border-dashed p-token-xxl hover:border-primary/50">
              <input
                type="file"
                accept=".xlsx,.xls"
                aria-label="Select spreadsheet"
                onChange={handleFileChange}
                className="absolute inset-0 cursor-pointer opacity-0"
              />
              <UploadCloud className="h-10 w-10 text-subtle" />
              {file ? (
                <Text variant="caption" className="flex items-center gap-token-sm font-semibold">
                  <FileSpreadsheet className="h-4 w-4 text-success" />
                  {file.name} ({(file.size / 1024).toFixed(1)} KB)
                </Text>
              ) : (
                <div className="text-center">
                  <Text variant="caption" className="font-medium">Drag and drop a sheet or browse files</Text>
                  <Text variant="caption" className="mt-token-xs text-subtle">Supports Excel Workbook (.xlsx, .xls) files</Text>
                </div>
              )}
            </Card>

            <div className="grid grid-cols-1 gap-token-md md:grid-cols-2">
              <Select label="Source Timezone" value={timezone} onChange={(event) => setTimezone(event.target.value)}>
                <option value="Asia/Kolkata">Asia/Kolkata (IST)</option>
                <option value="UTC">UTC (Greenwich Mean Time)</option>
                <option value="America/New_York">America/New_York (EST)</option>
                <option value="Europe/London">Europe/London (BST)</option>
              </Select>
              <Input label="Timestamp Excel Header" required value={timestampColumn} onChange={(event) => setTimestampColumn(event.target.value)} />
              <Input label="Machine Name Excel Header" required value={machineColumn} onChange={(event) => setMachineColumn(event.target.value)} />
              <Input label="Energy (kWh) Excel Header" required value={energyColumn} onChange={(event) => setEnergyColumn(event.target.value)} />
            </div>

            <Button type="submit" size="lg" disabled={uploadSpreadsheet.isPending || !file}>
              {uploadSpreadsheet.isPending ? 'Processing File...' : 'Start Ingestion'}
            </Button>
          </form>
        </Card>

        <Card className="flex flex-col gap-token-md p-token-lg">
          <Text variant="h3">Ingestion History Logs</Text>
          {isError && <Notice tone="error">Unable to load upload history.</Notice>}
          <div className="flex max-h-[450px] flex-col gap-token-sm overflow-y-auto">
            {history.map((record) => (
              <Card key={record.id} variant="solid" className="flex items-center justify-between gap-token-sm p-token-sm">
                <div className="overflow-hidden">
                  <Text variant="caption" className="truncate font-semibold">{record.filename}</Text>
                  <Text variant="caption" className="text-subtle">Processed {record.rowCount || 0} rows</Text>
                </div>
                <UploadStatusBadge status={record.status} />
              </Card>
            ))}
          </div>
        </Card>
      </div>
    </Page>
  );
};

export default UploadPage;
