import { useState, type FormEvent } from 'react';
import { Cpu, Plus } from 'lucide-react';
import { useAuth } from '../../../app/auth/AuthContext';
import { Page, PageHeader, PageLoading } from '../../../common/components/Page';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { Badge, Button, Card, Input, Notice, Text } from '../../../src/design-system';
import { useCreateMachine, useMachines } from '../hooks/useMachines';

const statusTone = (status: string): 'success' | 'warning' | 'error' => {
  if (status === 'RUNNING') return 'success';
  if (status === 'IDLE') return 'warning';
  return 'error';
};

export const MachinesPage = () => {
  const { user } = useAuth();
  const { data: machines = [], isLoading, isError } = useMachines();
  const createMachine = useCreateMachine();
  const [showModal, setShowModal] = useState(false);
  const [name, setName] = useState('');
  const [type, setType] = useState('');
  const [department, setDepartment] = useState('');
  const [baselineKwh, setBaselineKwh] = useState(10);

  const isEditor = user?.role === 'ADMIN' || user?.role === 'ENERGY_MGR';

  const handleCreate = async (event: FormEvent) => {
    event.preventDefault();
    await createMachine.mutateAsync({ name, type, department, baselineKwh, factoryId: user?.factoryId });
    setShowModal(false);
    setName('');
    setType('');
    setDepartment('');
    setBaselineKwh(10);
  };

  if (isLoading) return <PageLoading />;

  return (
    <Page>
      <PageHeader
        title="Machine Directory"
        description="Configure factory lines, departments, and baseline limits"
        action={isEditor ? (
          <Button onClick={() => setShowModal(true)}><Plus className="h-4 w-4" />Add Machine</Button>
        ) : undefined}
      />

      {isError && <Notice tone="error">Unable to load machines.</Notice>}
      {!isError && machines.length === 0 && <Notice>No machines have been registered yet.</Notice>}

      <div className="grid grid-cols-1 gap-token-lg md:grid-cols-2 lg:grid-cols-3">
        {machines.map((machine) => (
          <Card key={machine.id} className="flex flex-col justify-between p-token-lg">
            <div>
              <div className="mb-token-md flex items-start justify-between">
                <Card variant="solid" className="p-token-sm"><Cpu className="h-6 w-6 text-primary" /></Card>
                <Badge tone={statusTone(machine.status)}>{machine.status || 'OFFLINE'}</Badge>
              </div>
              <Text variant="h3">{machine.name}</Text>
              <Text variant="caption" className="mb-token-md mt-token-xs font-medium uppercase text-subtle">
                {machine.type} | {machine.department}
              </Text>
              <div className="flex items-center justify-between border-t border-border/70 pt-token-sm">
                <Text variant="caption" className="text-muted">Baseline Target</Text>
                <Text variant="caption" className="font-bold">{machine.baselineKwh} kWh</Text>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-overlay/70 px-token-md backdrop-blur-sm">
          <Card className="w-full max-w-md p-token-lg" role="dialog" aria-modal="true" aria-labelledby="machine-dialog-title">
            <Text id="machine-dialog-title" variant="h3" className="mb-token-md">Register Machine</Text>
            {createMachine.isError && (
              <Notice tone="error" className="mb-token-md">
                {getErrorMessage(createMachine.error, 'Failed to create machine.')}
              </Notice>
            )}
            <form onSubmit={handleCreate} className="flex flex-col gap-token-md">
              <Input label="Machine Name" required value={name} onChange={(event) => setName(event.target.value)} placeholder="CNC-01" />
              <Input label="Machine Type" required value={type} onChange={(event) => setType(event.target.value)} placeholder="Milling machine" />
              <Input label="Department" required value={department} onChange={(event) => setDepartment(event.target.value)} placeholder="Assembly Line A" />
              <Input
                label="Baseline target (kWh)"
                type="number"
                required
                value={baselineKwh}
                onChange={(event) => setBaselineKwh(Number(event.target.value))}
              />
              <div className="mt-token-sm flex justify-end gap-token-sm">
                <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>Cancel</Button>
                <Button type="submit" disabled={createMachine.isPending}>Save</Button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </Page>
  );
};

export default MachinesPage;
