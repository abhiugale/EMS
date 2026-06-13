import { useState, type FormEvent } from 'react';
import { CheckCircle2, Clock, Download, FileText, Loader2, XCircle } from 'lucide-react';
import { useAuth } from '../../../app/auth/AuthContext';
import { Page, PageHeader, PageLoading } from '../../../common/components/Page';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { ActionLink, Badge, Button, Card, Input, Notice, Select, Text } from '../../../src/design-system';
import { useGenerateReport, useReports } from '../hooks/useReports';
import { getReportDownloadUrl } from '../service/reportService';

const ReportStatusBadge = ({ status }: { status: string }) => {
  if (status === 'SUCCESS') return <Badge tone="success"><CheckCircle2 className="h-3 w-3" />Ready</Badge>;
  if (status === 'PROCESSING') return <Badge tone="info" className="animate-pulse"><Clock className="h-3 w-3" />Building</Badge>;
  return <Badge tone="error"><XCircle className="h-3 w-3" />Failed</Badge>;
};

export const ReportsPage = () => {
  const { user } = useAuth();
  const { data: reports = [], isLoading, isError } = useReports();
  const generateReport = useGenerateReport();
  const [reportType, setReportType] = useState('DAILY');
  const [targetDate, setTargetDate] = useState(new Date().toISOString().split('T')[0]);
  const isEditor = user?.role === 'ADMIN' || user?.role === 'ENERGY_MGR';

  const handleGenerate = async (event: FormEvent) => {
    event.preventDefault();
    await generateReport.mutateAsync({ reportType, targetDate });
  };

  if (isLoading) return <PageLoading />;

  return (
    <Page>
      <PageHeader title="Analytical Reports" description="Compile PDFs covering peak loads, shifts, and departments" />
      {isError && <Notice tone="error">Unable to load generated reports.</Notice>}

      <div className="grid grid-cols-1 gap-token-lg lg:grid-cols-3">
        <Card className="h-fit p-token-lg">
          <Text variant="h3" className="mb-token-md">Generate Report</Text>
          {generateReport.isError && (
            <Notice tone="error" className="mb-token-md">
              {getErrorMessage(generateReport.error, 'Failed to generate report.')}
            </Notice>
          )}
          <form onSubmit={handleGenerate} className="flex flex-col gap-token-md">
            <Select label="Report Interval" value={reportType} onChange={(event) => setReportType(event.target.value)}>
              <option value="DAILY">Daily Report</option>
              <option value="WEEKLY">Weekly Report</option>
              <option value="MONTHLY">Monthly Report</option>
            </Select>
            <Input label="Target Date" type="date" required value={targetDate} onChange={(event) => setTargetDate(event.target.value)} />

            {isEditor ? (
              <Button type="submit" disabled={generateReport.isPending}>
                {generateReport.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                {generateReport.isPending ? 'Triggering Build...' : 'Build PDF Report'}
              </Button>
            ) : (
              <Notice tone="warning">Only Energy Managers and Admins can build reports.</Notice>
            )}
          </form>
        </Card>

        <Card className="flex flex-col gap-token-md p-token-lg lg:col-span-2">
          <Text variant="h3">Generated Reports List</Text>
          <div className="flex max-h-[500px] flex-col gap-token-sm overflow-y-auto">
            {reports.map((report) => (
              <Card key={report.id} variant="solid" className="flex items-center justify-between gap-token-md p-token-md">
                <div className="flex items-center gap-token-sm">
                  <Card variant="outline" className="p-token-sm text-primary"><FileText className="h-5 w-5" /></Card>
                  <div>
                    <Text variant="caption" className="font-bold">{report.reportType} REPORT</Text>
                    <Text variant="caption" className="text-subtle">
                      Created {new Date(report.createdAt).toLocaleString()}
                      {report.generatedByEmail ? ` by ${report.generatedByEmail}` : ''}
                    </Text>
                  </div>
                </div>

                <div className="flex items-center gap-token-sm">
                  <ReportStatusBadge status={report.status} />
                  {report.status === 'SUCCESS' && report.filePath && (
                    <ActionLink
                      href={getReportDownloadUrl(report.filePath)}
                      target="_blank"
                      rel="noreferrer"
                      size="icon"
                      aria-label="Download report"
                    >
                      <Download className="h-4 w-4" />
                    </ActionLink>
                  )}
                </div>
              </Card>
            ))}
          </div>
        </Card>
      </div>
    </Page>
  );
};

export default ReportsPage;
