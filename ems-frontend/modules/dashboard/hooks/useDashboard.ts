import { useQuery } from '@tanstack/react-query';
import { dashboardQueryKeys } from '../constants/queryKeys';
import { getDashboardData } from '../service/dashboardService';

export const useDashboard = () => useQuery({
  queryKey: dashboardQueryKeys.all,
  queryFn: getDashboardData,
  refetchInterval: 10_000,
});
