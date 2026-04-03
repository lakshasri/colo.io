import { useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useWebSocket = (topicPath, onMessageReceived) => {
  const clientRef = useRef(null);
  const subscriptionRef = useRef(null);

  useEffect(() => {
    if (!topicPath || !onMessageReceived) return;

    const socket = new SockJS('http://localhost:8080/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        subscriptionRef.current = client.subscribe(topicPath, (message) => {
          const data = JSON.parse(message.body);
          onMessageReceived(data);
        });
      },
      onDisconnect: () => {
        console.log(`Disconnected from WebSocket`);
      },
      onStompError: (frame) => {
        console.error('WebSocket error:', frame);
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
      }
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [topicPath, onMessageReceived]);

  return clientRef.current;
};
