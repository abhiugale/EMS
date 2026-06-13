import { Bell, Cpu, FileText, LayoutDashboard, Lightbulb, Settings, UploadCloud, User, Zap } from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { appRoutes } from '../../router/appRoutes';
import { Card, Text } from '../../../src/design-system';

const menuItems = [
  { name: 'Dashboard', path: appRoutes.dashboard, icon: LayoutDashboard, roles: ['ADMIN', 'ENERGY_MGR', 'SUPERVISOR', 'VIEWER'] },
  { name: 'Machines', path: appRoutes.machines, icon: Cpu, roles: ['ADMIN', 'ENERGY_MGR', 'SUPERVISOR', 'VIEWER'] },
  { name: 'Upload Data', path: appRoutes.upload, icon: UploadCloud, roles: ['ADMIN', 'ENERGY_MGR'] },
  { name: 'AI Insights', path: appRoutes.insights, icon: Lightbulb, roles: ['ADMIN', 'ENERGY_MGR', 'SUPERVISOR', 'VIEWER'] },
  { name: 'Alerts', path: appRoutes.alerts, icon: Bell, roles: ['ADMIN', 'ENERGY_MGR', 'SUPERVISOR', 'VIEWER'] },
  { name: 'Reports', path: appRoutes.reports, icon: FileText, roles: ['ADMIN', 'ENERGY_MGR', 'SUPERVISOR', 'VIEWER'] },
  { name: 'Admin Settings', path: appRoutes.admin, icon: Settings, roles: ['ADMIN'] },
];

export const Sidebar = () => {
  const { user } = useAuth();

  return (
    <aside className="glass-panel sticky top-0 hidden h-screen w-64 flex-col justify-between border-r border-border/70 md:flex">
      <div className="px-token-md py-token-lg">
        <div className="mb-token-xl flex items-center gap-token-sm px-token-sm">
          <Card variant="solid" className="flex h-8 w-8 items-center justify-center text-primary"><Zap className="h-4 w-4" /></Card>
          <Text className="font-bold tracking-wider">EMS Core</Text>
        </div>

        <nav className="flex flex-col gap-token-xs" aria-label="Primary navigation">
          {menuItems.map((item) => {
            if (user?.role && !item.roles.includes(user.role)) return null;
            return (
              <NavLink
                key={item.name}
                to={item.path}
                className={({ isActive }) => `flex items-center gap-token-sm rounded-lg px-token-md py-3 text-sm font-medium transition-colors ${
                  isActive
                    ? 'border-l-4 border-primary bg-primary/15 pl-3 text-primary'
                    : 'text-muted hover:bg-surface-raised/60 hover:text-foreground'
                }`}
              >
                <item.icon className="h-5 w-5" />
                {item.name}
              </NavLink>
            );
          })}
        </nav>
      </div>

      <div className="flex items-center gap-token-sm border-t border-border/70 p-token-md">
        <Card variant="solid" className="p-token-sm"><User className="h-5 w-5 text-muted" /></Card>
        <div className="overflow-hidden">
          <Text variant="caption" className="font-bold uppercase tracking-wider text-subtle">{user?.role}</Text>
          <Text variant="caption" className="truncate font-semibold">{user?.email}</Text>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
