import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';
import type { RealtimeEnergy } from '../types';

type ConnectionStatus = 'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'ERROR';

interface UseRealtimeOptions {
  /** The factory UUID to subscribe to. Hook is a no-op when undefined. */
  factoryId: string | undefined;
  /** Called on every incoming realtime message. */
  onMessage?: (data: RealtimeEnergy) => void;
}

interface UseRealtimeResult {
  /** Latest realtime snapshot received via WebSocket. */
  realtimeWs: RealtimeEnergy | null;
  status: ConnectionStatus;
}

const WS_URL = `ws://${window.location.hostname}:8080/ws`;

/**
 * Connects to the Spring STOMP broker via a native WebSocket (no SockJS).
 * Subscribes to /topic/realtime/{factoryId} and keeps the connection alive
 * with heartbeats.
 *
 * The hook cleans up the client on unmount or when factoryId changes.
 */
export function useRealtimeWebSocket({ factoryId, onMessage }: UseRealtimeOptions): UseRealtimeResult {
  const [realtimeWs, setRealtimeWs] = useState<RealtimeEnergy | null>(null);
  const [status, setStatus] = useState<ConnectionStatus>('DISCONNECTED');
  const clientRef = useRef<Client | null>(null);
  const subscriptionRef = useRef<StompSubscription | null>(null);

  const handleMessage = useCallback(
    (frame: IMessage) => {
      try {
        const data: RealtimeEnergy = JSON.parse(frame.body);
        setRealtimeWs(data);
        onMessage?.(data);
      } catch {
        // Malformed JSON — ignore
      }
    },
    [onMessage],
  );

  useEffect(() => {
    if (!factoryId) return;

    setStatus('CONNECTING');

    const client = new Client({
      brokerURL: WS_URL,
      reconnectDelay: 5000,      // auto-reconnect after 5 s
      heartbeatIncoming: 10000,  // expect server heartbeat every 10 s
      heartbeatOutgoing: 10000,

      onConnect: () => {
        setStatus('CONNECTED');
        subscriptionRef.current = client.subscribe(
          `/topic/realtime/${factoryId}`,
          handleMessage,
        );
      },

      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame.headers['message']);
        setStatus('ERROR');
      },

      onDisconnect: () => setStatus('DISCONNECTED'),
      onWebSocketError: (evt) => {
        console.error('[WS] WebSocket error:', evt);
        setStatus('ERROR');
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      subscriptionRef.current?.unsubscribe();
      void client.deactivate();
      clientRef.current = null;
      setStatus('DISCONNECTED');
    };
  }, [factoryId, handleMessage]);

  return { realtimeWs, status };
}
