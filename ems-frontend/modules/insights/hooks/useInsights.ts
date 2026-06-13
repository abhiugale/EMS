import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { insightQueryKeys } from '../constants/queryKeys';
import { getInsights, resolveInsight } from '../service/insightService';

export const useInsights = () => useQuery({
  queryKey: insightQueryKeys.all,
  queryFn: getInsights,
});

export const useResolveInsight = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: resolveInsight,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: insightQueryKeys.all }),
  });
};
