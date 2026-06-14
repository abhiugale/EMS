import { useState, useCallback, type ChangeEvent, type FormEvent } from 'react';
import {
  CheckCircle2, Clock, FileSpreadsheet, Info,
  RefreshCw, UploadCloud, XCircle,
} from 'lucide-react';
import { Page, PageHeader } from '../../../common/components/Page';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { Badge, Button, Card, Notice, Select, Text } from '../../../src/design-system';
import { useUploads, useUploadSpreadsheet } from '../hooks/useUploads';
import { previewHeaders } from '../service/uploadService';
import type { FileFormat, NarrowColumnMapping, WideColumnMapping } from '../types';

/* ─────────────────────────────────────────────────────────────
   Wide format — system field definitions
───────────────────────────────────────────────────────────── */
const WIDE_REQUIRED: { key: keyof WideColumnMapping; label: string }[] = [
  { key: 'timestamp',    label: 'Timestamp *' },
  { key: 'machine_name', label: 'Machine Name *' },
  { key: 'energy_kwh',  label: 'Energy (kWh) *' },
];

const WIDE_OPTIONAL: { key: keyof WideColumnMapping; label: string }[] = [
  { key: 'active_kw',     label: 'Active Power (kW)' },
  { key: 'apparent_kva',  label: 'Apparent Power (kVA)' },
  { key: 'reactive_kvar', label: 'Reactive Power (kVAR)' },
  { key: 'power_factor',  label: 'Power Factor' },
  { key: 'frequency',     label: 'Frequency (Hz)' },
  { key: 'voltage_r',     label: 'Voltage Phase R' },
  { key: 'voltage_y',     label: 'Voltage Phase Y' },
  { key: 'voltage_b',     label: 'Voltage Phase B' },
  { key: 'current_r',     label: 'Current Phase R' },
  { key: 'current_y',     label: 'Current Phase Y' },
  { key: 'current_b',     label: 'Current Phase B' },
  { key: 'parts_produced', label: 'Parts Produced' },
];

/* ─────────────────────────────────────────────────────────────
   Narrow format — only 4 columns to map
───────────────────────────────────────────────────────────── */
const NARROW_FIELDS: { key: keyof NarrowColumnMapping; label: string; hint: string }[] = [
  { key: 'timestamp',    label: 'Timestamp Column *',      hint: 'e.g. "Timestamp", "Date"' },
  { key: 'machine_name', label: 'Device / Machine ID Col *', hint: 'e.g. "Device_ID", "Meter"' },
  { key: 'tag_col',      label: 'Tag / Parameter Name Col *', hint: 'e.g. "Tag", "Parameter"' },
  { key: 'value_col',    label: 'Value Column *',           hint: 'e.g. "Value", "Reading"' },
];

/* ─────────────────────────────────────────────────────────────
   Helpers
───────────────────────────────────────────────────────────── */
const UploadStatusBadge = ({ status }: { status: string }) => {
  if (status === 'SUCCESS')    return <Badge tone="success"><CheckCircle2 className="h-3 w-3" />Success</Badge>;
  if (status === 'PROCESSING') return <Badge tone="info" className="animate-pulse"><Clock className="h-3 w-3" />Pending</Badge>;
  return <Badge tone="error"><XCircle className="h-3 w-3" />Failed</Badge>;
};

function autoGuessWide(headers: string[]): Partial<WideColumnMapping> {
  const lower = (s: string) => s.toLowerCase().replace(/[^a-z0-9]/g, '');
  const find = (...kw: string[]) => headers.find(h => kw.some(k => lower(h).includes(k))) ?? '';
  return {
    timestamp:     find('time', 'date', 'timestamp', 'recorded'),
    machine_name:  find('machine', 'device', 'equipment', 'unit'),
    energy_kwh:    find('kwh', 'energy', 'consumption'),
    active_kw:     find('activekw', 'activepow', 'kw'),
    apparent_kva:  find('kva', 'apparent'),
    reactive_kvar: find('kvar', 'reactive'),
    power_factor:  find('pf', 'powerfactor', 'factor'),
    frequency:     find('freq', 'hz'),
    voltage_r:     find('voltager', 'vr'),
    voltage_y:     find('voltagey', 'vy'),
    voltage_b:     find('voltageb', 'vb'),
    current_r:     find('currentr', 'ir'),
    current_y:     find('currenty', 'iy'),
    current_b:     find('currentb', 'ib'),
    parts_produced: find('parts', 'produced', 'output', 'qty'),
  };
}

function autoGuessNarrow(headers: string[]): Partial<NarrowColumnMapping> {
  const lower = (s: string) => s.toLowerCase().replace(/[^a-z0-9]/g, '');
  const find = (...kw: string[]) => headers.find(h => kw.some(k => lower(h).includes(k))) ?? '';
  return {
    timestamp:    find('time', 'date', 'timestamp'),
    machine_name: find('device', 'meter', 'machine', 'unit', 'id'),
    tag_col:      find('tag', 'parameter', 'metric', 'measurement'),
    value_col:    find('value', 'reading', 'data'),
  };
}

const FORMAT_LABELS: Record<FileFormat, string> = {
  WIDE:   'Wide (one row per machine reading)',
  NARROW: 'Narrow / Pivot (Timestamp | Device | Tag | Value)',
};

/* ─────────────────────────────────────────────────────────────
   Page component
───────────────────────────────────────────────────────────── */
export const UploadPage = () => {
  const { data: history = [], isError } = useUploads();
  const uploadMutation = useUploadSpreadsheet();

  /* ── State ── */
  const [step, setStep]                   = useState<'select' | 'map' | 'done'>('select');
  const [file, setFile]                   = useState<File | null>(null);
  const [timezone, setTimezone]           = useState('Asia/Kolkata');
  const [detectedHeaders, setDetectedHeaders] = useState<string[]>([]);
  const [formatType, setFormatType]       = useState<FileFormat>('WIDE');
  const [wideMapping, setWideMapping]     = useState<Partial<WideColumnMapping>>({});
  const [narrowMapping, setNarrowMapping] = useState<Partial<NarrowColumnMapping>>({});
  const [loadingHeaders, setLoadingHeaders] = useState(false);
  const [error, setError]                 = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  /* ── Step 1: file selected → fetch headers & detect format ── */
  const handleFileChange = useCallback(async (event: ChangeEvent<HTMLInputElement>) => {
    const selected = event.target.files?.[0] ?? null;
    setFile(selected);
    setError('');
    setSuccessMessage('');
    if (!selected) return;

    setLoadingHeaders(true);
    try {
      const preview = await previewHeaders(selected);
      setDetectedHeaders(preview.headers);
      setFormatType(preview.format);
      if (preview.format === 'NARROW') {
        setNarrowMapping(autoGuessNarrow(preview.headers));
        setWideMapping({});
      } else {
        setWideMapping(autoGuessWide(preview.headers));
        setNarrowMapping({});
      }
      setStep('map');
    } catch (e) {
      setError(getErrorMessage(e, 'Could not read file headers. Make sure the file is a valid Excel workbook.'));
    } finally {
      setLoadingHeaders(false);
    }
  }, []);

  /* ── Step 2: submit ── */
  const handleUpload = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setSuccessMessage('');

    if (formatType === 'WIDE') {
      const { timestamp, machine_name, energy_kwh } = wideMapping as WideColumnMapping;
      if (!timestamp || !machine_name || !energy_kwh) {
        setError('Please map all required fields (Timestamp, Machine Name, Energy kWh) before uploading.');
        return;
      }
      const cleanMapping = Object.fromEntries(
        Object.entries(wideMapping).filter(([, v]) => v && v !== ''),
      ) as WideColumnMapping;

      await uploadMutation.mutateAsync({ file: file!, timezone, mapping: cleanMapping, formatType });
    } else {
      const { timestamp, machine_name, tag_col, value_col } = narrowMapping as NarrowColumnMapping;
      if (!timestamp || !machine_name || !tag_col || !value_col) {
        setError('Please map all four columns: Timestamp, Device/Machine ID, Tag Name, and Value.');
        return;
      }
      await uploadMutation.mutateAsync({
        file: file!,
        timezone,
        mapping: narrowMapping as NarrowColumnMapping,
        formatType,
      });
    }

    setSuccessMessage('Spreadsheet uploaded and queued for processing.');
    setStep('done');
    setFile(null);
    setDetectedHeaders([]);
    setWideMapping({});
    setNarrowMapping({});
  };

  const reset = () => {
    setStep('select');
    setFile(null);
    setDetectedHeaders([]);
    setWideMapping({});
    setNarrowMapping({});
    setError('');
    setSuccessMessage('');
    uploadMutation.reset();
  };

  const uploadError = uploadMutation.isError
    ? getErrorMessage(uploadMutation.error, 'Spreadsheet upload failed.')
    : error;

  /* ── Reusable column selector ── */
  const ColSelect = ({
    label,
    hint,
    value,
    onChange,
    required,
  }: {
    label: string;
    hint?: string;
    value: string;
    onChange: (v: string) => void;
    required?: boolean;
  }) => (
    <div className="flex flex-col gap-1">
      <label className="text-xs font-semibold text-muted">{label}</label>
      {hint && <span className="text-xs text-subtle">{hint}</span>}
      <select
        className="rounded-md border border-border bg-surface px-3 py-2 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50"
        value={value}
        onChange={e => onChange(e.target.value)}
        required={required}
      >
        {required
          ? <option value="">— Select column —</option>
          : <option value="">— Not in file —</option>}
        {detectedHeaders.map(h => <option key={h} value={h}>{h}</option>)}
      </select>
    </div>
  );

  return (
    <Page>
      <PageHeader
        title="Import Time-Series Telemetry"
        description="Upload an Excel file and map its columns to system fields"
      />

      <div className="grid grid-cols-1 gap-token-lg lg:grid-cols-3">
        {/* ── Main wizard card ── */}
        <Card className="p-token-lg lg:col-span-2">
          {uploadError && <Notice tone="error" className="mb-token-md">{uploadError}</Notice>}
          {successMessage && <Notice tone="success" className="mb-token-md">{successMessage}</Notice>}

          {/* ── STEP 1: File drop ── */}
          {(step === 'select' || step === 'done') && (
            <div className="flex flex-col gap-token-lg">
              <Text variant="h3">{step === 'done' ? 'Upload Another File' : 'Select Spreadsheet'}</Text>

              <Card
                variant="outline"
                className="relative flex cursor-pointer flex-col items-center justify-center gap-token-sm border-dashed p-token-xxl hover:border-primary/50"
              >
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  aria-label="Select spreadsheet"
                  onChange={handleFileChange}
                  className="absolute inset-0 cursor-pointer opacity-0"
                  disabled={loadingHeaders}
                />
                {loadingHeaders ? (
                  <>
                    <RefreshCw className="h-10 w-10 animate-spin text-primary" />
                    <Text variant="caption" className="font-medium">Detecting format &amp; reading headers…</Text>
                  </>
                ) : (
                  <>
                    <UploadCloud className="h-10 w-10 text-subtle" />
                    <div className="text-center">
                      <Text variant="caption" className="font-medium">Drag and drop a sheet or browse files</Text>
                      <Text variant="caption" className="mt-token-xs text-subtle">Supports Excel Workbook (.xlsx, .xls)</Text>
                    </div>
                  </>
                )}
              </Card>

              <Select label="Source Timezone" value={timezone} onChange={e => setTimezone(e.target.value)}>
                <option value="Asia/Kolkata">Asia/Kolkata (IST)</option>
                <option value="UTC">UTC (Greenwich Mean Time)</option>
                <option value="America/New_York">America/New_York (EST)</option>
                <option value="Europe/London">Europe/London (BST)</option>
              </Select>
            </div>
          )}

          {/* ── STEP 2: Column mapping ── */}
          {step === 'map' && file && (
            <form onSubmit={handleUpload} className="flex flex-col gap-token-lg">
              <div className="flex items-center justify-between">
                <Text variant="h3">Map Columns</Text>
                <button type="button" onClick={reset} className="text-xs text-muted underline hover:text-foreground">
                  ← Change file
                </button>
              </div>

              {/* File info + format badge */}
              <Card variant="solid" className="flex items-center gap-token-sm p-token-sm">
                <FileSpreadsheet className="h-5 w-5 text-success" />
                <div className="flex-1 overflow-hidden">
                  <Text variant="caption" className="truncate font-semibold">{file.name}</Text>
                  <Text variant="caption" className="text-subtle">
                    {detectedHeaders.length} columns detected
                  </Text>
                </div>
                <span
                  className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                    formatType === 'NARROW'
                      ? 'bg-purple-100 text-purple-700'
                      : 'bg-blue-100 text-blue-700'
                  }`}
                >
                  {formatType === 'NARROW' ? 'Narrow / Pivot' : 'Wide'}
                </span>
              </Card>

              {/* Format override — user can switch if auto-detect was wrong */}
              <div className="flex flex-col gap-1">
                <label className="text-xs font-semibold text-muted">File Format</label>
                <div className="flex gap-token-sm">
                  {(['WIDE', 'NARROW'] as FileFormat[]).map(f => (
                    <button
                      key={f}
                      type="button"
                      onClick={() => {
                        setFormatType(f);
                        if (f === 'NARROW') setNarrowMapping(autoGuessNarrow(detectedHeaders));
                        else setWideMapping(autoGuessWide(detectedHeaders));
                      }}
                      className={`flex-1 rounded-md border px-3 py-2 text-xs font-medium transition-colors ${
                        formatType === f
                          ? 'border-primary bg-primary/10 text-primary'
                          : 'border-border bg-surface text-muted hover:border-primary/50'
                      }`}
                    >
                      {FORMAT_LABELS[f]}
                    </button>
                  ))}
                </div>
              </div>

              {/* Format-specific help banner */}
              {formatType === 'NARROW' && (
                <Card variant="outline" className="flex gap-token-sm border-purple-200 bg-purple-50 p-token-sm text-xs text-purple-800">
                  <Info className="mt-0.5 h-4 w-4 shrink-0" />
                  <span>
                    <strong>Narrow / Pivot format detected.</strong> Your file has one row per
                    measurement (Tag + Value). Map the four columns below — the system will
                    automatically pivot tag names like <em>kW, kVAR, kVA, PF, Frequency,
                    Energy_kWh, Voltage_R/Y/B, Current_R/Y/B, Part_Count</em> into the
                    correct fields.
                  </span>
                </Card>
              )}

              <Select label="Source Timezone" value={timezone} onChange={e => setTimezone(e.target.value)}>
                <option value="Asia/Kolkata">Asia/Kolkata (IST)</option>
                <option value="UTC">UTC (Greenwich Mean Time)</option>
                <option value="America/New_York">America/New_York (EST)</option>
                <option value="Europe/London">Europe/London (BST)</option>
              </Select>

              {/* ── Wide format mapping ── */}
              {formatType === 'WIDE' && (
                <>
                  <div>
                    <Text variant="caption" className="mb-token-sm font-semibold uppercase tracking-wider text-muted">
                      Required Fields
                    </Text>
                    <div className="grid grid-cols-1 gap-token-md sm:grid-cols-3">
                      {WIDE_REQUIRED.map(f => (
                        <ColSelect
                          key={f.key}
                          label={f.label}
                          value={(wideMapping[f.key] as string) ?? ''}
                          onChange={v => setWideMapping(prev => ({ ...prev, [f.key]: v }))}
                          required
                        />
                      ))}
                    </div>
                  </div>
                  <div>
                    <Text variant="caption" className="mb-token-sm font-semibold uppercase tracking-wider text-muted">
                      Optional Fields
                    </Text>
                    <div className="grid grid-cols-1 gap-token-md sm:grid-cols-3">
                      {WIDE_OPTIONAL.map(f => (
                        <ColSelect
                          key={f.key}
                          label={f.label}
                          value={(wideMapping[f.key] as string) ?? ''}
                          onChange={v => setWideMapping(prev => ({ ...prev, [f.key]: v }))}
                        />
                      ))}
                    </div>
                  </div>
                </>
              )}

              {/* ── Narrow format mapping ── */}
              {formatType === 'NARROW' && (
                <div>
                  <Text variant="caption" className="mb-token-sm font-semibold uppercase tracking-wider text-muted">
                    Column Mapping (All Required)
                  </Text>
                  <div className="grid grid-cols-1 gap-token-md sm:grid-cols-2">
                    {NARROW_FIELDS.map(f => (
                      <ColSelect
                        key={f.key}
                        label={f.label}
                        hint={f.hint}
                        value={(narrowMapping[f.key] as string) ?? ''}
                        onChange={v => setNarrowMapping(prev => ({ ...prev, [f.key]: v }))}
                        required
                      />
                    ))}
                  </div>
                </div>
              )}

              <Button
                type="submit"
                size="lg"
                className="mt-token-sm w-full"
                disabled={uploadMutation.isPending}
              >
                {uploadMutation.isPending ? 'Processing…' : 'Start Ingestion'}
              </Button>
            </form>
          )}
        </Card>

        {/* ── History sidebar ── */}
        <Card className="flex flex-col gap-token-md p-token-lg">
          <Text variant="h3">Ingestion History</Text>
          {isError && <Notice tone="error">Unable to load upload history.</Notice>}
          <div className="flex max-h-[500px] flex-col gap-token-sm overflow-y-auto">
            {history.map(record => (
              <Card key={record.id} variant="solid" className="flex items-center justify-between gap-token-sm p-token-sm">
                <div className="overflow-hidden">
                  <Text variant="caption" className="truncate font-semibold">{record.filename}</Text>
                  <Text variant="caption" className="text-subtle">{record.rowCount ?? 0} rows</Text>
                </div>
                <UploadStatusBadge status={record.status} />
              </Card>
            ))}
            {history.length === 0 && (
              <Text variant="caption" className="text-center text-subtle">No uploads yet.</Text>
            )}
          </div>
        </Card>
      </div>
    </Page>
  );
};

export default UploadPage;
