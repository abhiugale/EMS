import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { getCeoSummary } from '../service/layoutService';
import type { CeoSummary } from '../types';

const ceoSummaryQueryKey = ['layout', 'ceo-summary'] as const;

export const useCeoSummary = (factoryId?: string) => {
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ceoSummaryQueryKey,
    queryFn: getCeoSummary,
    refetchInterval: 15_000,
  });

  useEffect(() => {
    if (!factoryId) return undefined;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const client = new Client({
      brokerURL: `${protocol}//${window.location.host}/ws`,
      reconnectDelay: 5_000,
      onConnect: () => {
        client.subscribe(`/topic/alerts/${factoryId}`, (message) => {
          const alert = JSON.parse(message.body) as { status?: string };
          queryClient.setQueryData<CeoSummary>(ceoSummaryQueryKey, (current) => {
            if (!current) return current;
            const change = alert.status === 'OPEN' ? 1 : alert.status === 'RESOLVED' ? -1 : 0;
            return { ...current, activeAlertCount: Math.max(0, current.activeAlertCount + change) };
          });
        });
      },
    });

    client.activate();
    return () => { void client.deactivate(); };
  }, [factoryId, queryClient]);

  return query;
};
