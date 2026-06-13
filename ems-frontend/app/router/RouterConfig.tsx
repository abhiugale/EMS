import { lazy, Suspense, type ReactNode } from 'react';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { MainLayout } from '../layouts/MainLayout';
import { PageLoading } from '../../common/components/Page';
import { appRoutes } from './appRoutes';

const LoginPage = lazy(() => import('../../modules/auth/pages/LoginPage'));
const DashboardPage = lazy(() => import('../../modules/dashboard/pages/DashboardPage'));
const MachinesPage = lazy(() => import('../../modules/machines/pages/MachinesPage'));
const UploadPage = lazy(() => import('../../modules/upload/pages/UploadPage'));
const InsightsPage = lazy(() => import('../../modules/insights/pages/InsightsPage'));
const AlertsPage = lazy(() => import('../../modules/alerts/pages/AlertsPage'));
const ReportsPage = lazy(() => import('../../modules/reports/pages/ReportsPage'));
const AdminPage = lazy(() => import('../../modules/admin/pages/AdminPage'));

const ProtectedRoute = () => {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return <PageLoading />;
  return isAuthenticated ? <Outlet /> : <Navigate to={appRoutes.login} replace />;
};

const RoleRoute = ({ roles, children }: { roles: string[]; children: ReactNode }) => {
  const { user } = useAuth();
  return user?.role && roles.includes(user.role) ? children : <Navigate to={appRoutes.dashboard} replace />;
};

export const RouterConfig = () => (
  <Suspense fallback={<PageLoading />}>
    <Routes>
      <Route path={appRoutes.login} element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path={appRoutes.machines.slice(1)} element={<MachinesPage />} />
          <Route path={appRoutes.upload.slice(1)} element={<RoleRoute roles={['ADMIN', 'ENERGY_MGR']}><UploadPage /></RoleRoute>} />
          <Route path={appRoutes.insights.slice(1)} element={<InsightsPage />} />
          <Route path={appRoutes.alerts.slice(1)} element={<AlertsPage />} />
          <Route path={appRoutes.reports.slice(1)} element={<ReportsPage />} />
          <Route path={appRoutes.admin.slice(1)} element={<RoleRoute roles={['ADMIN']}><AdminPage /></RoleRoute>} />
          <Route path="*" element={<Navigate to={appRoutes.dashboard} replace />} />
        </Route>
      </Route>
    </Routes>
  </Suspense>
);

export default RouterConfig;
