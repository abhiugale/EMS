import { useState, type FormEvent } from 'react';
import { CheckCircle2, Shield, UserPlus } from 'lucide-react';
import { useAuth } from '../../../app/auth/AuthContext';
import { Page, PageHeader } from '../../../common/components/Page';
import { getErrorMessage } from '../../../common/utils/getErrorMessage';
import { Badge, Button, Card, Input, Notice, Select, Text } from '../../../src/design-system';
import { useRegisterUser } from '../hooks/useAdmin';

export const AdminPage = () => {
  const { user } = useAuth();
  const registerUser = useRegisterUser();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('VIEWER');
  const [successMessage, setSuccessMessage] = useState('');

  const handleRegisterUser = async (event: FormEvent) => {
    event.preventDefault();
    if (!user?.factoryId) return;
    setSuccessMessage('');
    await registerUser.mutateAsync({ firstName, lastName, email, password, role, factoryId: user.factoryId });
    setSuccessMessage('User account registered successfully.');
    setFirstName('');
    setLastName('');
    setEmail('');
    setPassword('');
  };

  return (
    <Page>
      <PageHeader title="System Administration" description="Manage access credentials and operator roles" />

      <div className="grid grid-cols-1 gap-token-lg lg:grid-cols-3">
        <Card className="h-fit p-token-lg">
          <div className="mb-token-md flex items-center gap-token-sm">
            <Shield className="h-5 w-5 text-primary" />
            <Text variant="h3">Administrator Context</Text>
          </div>
          <div className="flex flex-col gap-token-md">
            <div className="border-b border-border/70 pb-token-sm">
              <Text variant="caption" className="text-muted">Signed in as</Text>
              <Text className="font-semibold">{user?.email}</Text>
            </div>
            <div className="border-b border-border/70 pb-token-sm">
              <Text variant="caption" className="text-muted">System Role</Text>
              <Badge tone="primary" className="mt-token-xs">{user?.role}</Badge>
            </div>
            <div>
              <Text variant="caption" className="text-muted">Factory ID</Text>
              <Text variant="caption" className="mt-token-xs break-all font-semibold">{user?.factoryId || 'Not assigned'}</Text>
            </div>
          </div>
        </Card>

        <Card className="p-token-lg lg:col-span-2">
          <div className="mb-token-md flex items-center gap-token-sm">
            <UserPlus className="h-5 w-5 text-primary" />
            <Text variant="h3">Provision User Account</Text>
          </div>

          {successMessage && (
            <Notice tone="success" className="mb-token-md flex items-center gap-token-sm">
              <CheckCircle2 className="h-4 w-4" />{successMessage}
            </Notice>
          )}
          {registerUser.isError && (
            <Notice tone="error" className="mb-token-md">
              {getErrorMessage(registerUser.error, 'Failed to register user.')}
            </Notice>
          )}
          {!user?.factoryId && <Notice tone="warning" className="mb-token-md">Your account is not assigned to a factory.</Notice>}

          <form onSubmit={handleRegisterUser} className="grid grid-cols-1 gap-token-md md:grid-cols-2">
            <Input label="First Name" required value={firstName} onChange={(event) => setFirstName(event.target.value)} />
            <Input label="Last Name" required value={lastName} onChange={(event) => setLastName(event.target.value)} />
            <Input label="Email Address" type="email" required value={email} onChange={(event) => setEmail(event.target.value)} />
            <Input label="Access Password" type="password" minLength={8} required value={password} onChange={(event) => setPassword(event.target.value)} />
            <Select label="System Role" value={role} onChange={(event) => setRole(event.target.value)}>
              <option value="VIEWER">VIEWER</option>
              <option value="SUPERVISOR">SUPERVISOR</option>
              <option value="ENERGY_MGR">ENERGY_MGR</option>
              <option value="ADMIN">ADMIN</option>
            </Select>
            <div className="flex items-end">
              <Button type="submit" className="w-full" disabled={registerUser.isPending || !user?.factoryId}>
                Register Account
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </Page>
  );
};

export default AdminPage;
