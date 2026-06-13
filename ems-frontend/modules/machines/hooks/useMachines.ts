import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { machineQueryKeys } from '../constants/queryKeys';
import { createMachine, getMachines } from '../service/machineService';

export const useMachines = () => useQuery({
  queryKey: machineQueryKeys.all,
  queryFn: getMachines,
});

export const useCreateMachine = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createMachine,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: machineQueryKeys.all }),
  });
};
