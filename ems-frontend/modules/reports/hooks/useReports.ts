import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { reportQueryKeys } from '../constants/queryKeys';
import { generateReport, getReports } from '../service/reportService';

export const useReports = () => useQuery({
  queryKey: reportQueryKeys.all,
  queryFn: getReports,
  refetchInterval: 10_000,
});

export const useGenerateReport = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: generateReport,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: reportQueryKeys.all }),
  });
};
