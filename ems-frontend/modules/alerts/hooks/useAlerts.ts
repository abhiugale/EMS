import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { alertQueryKeys } from '../constants/queryKeys';
import { getAlerts, resolveAlert } from '../service/alertService';
import type { Alert } from '../types';

export const useAlerts = (factoryId?: string) => {
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: alertQueryKeys.all, queryFn: getAlerts });

  useEffect(() => {
    if (!factoryId) return undefined;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const client = new Client({
      brokerURL: `${protocol}//${window.location.host}/ws`,
      reconnectDelay: 5_000,
      onConnect: () => {
        client.subscribe(`/topic/alerts/${factoryId}`, (message) => {
          const incomingAlert = JSON.parse(message.body) as Alert;
          queryClient.setQueryData<Alert[]>(alertQueryKeys.all, (current = []) => {
            const exists = current.some((alert) => alert.id === incomingAlert.id);
            return exists
              ? current.map((alert) => alert.id === incomingAlert.id ? incomingAlert : alert)
              : [incomingAlert, ...current];
          });
        });
      },
    });

    client.activate();
    return () => { void client.deactivate(); };
  }, [factoryId, queryClient]);

  return query;
};

export const useResolveAlert = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: resolveAlert,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: alertQueryKeys.all }),
  });
};
