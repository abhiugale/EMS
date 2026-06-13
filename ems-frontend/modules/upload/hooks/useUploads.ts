import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { uploadQueryKeys } from '../constants/queryKeys';
import { getUploads, uploadSpreadsheet } from '../service/uploadService';

export const useUploads = () => useQuery({
  queryKey: uploadQueryKeys.all,
  queryFn: getUploads,
  refetchInterval: 10_000,
});

export const useUploadSpreadsheet = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: uploadSpreadsheet,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: uploadQueryKeys.all }),
  });
};
