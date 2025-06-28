import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function useKhqrWebSocket(md5: string, onMessage: (status: string) => void) {
  const stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    reconnectDelay: 5000,
    debug: str => console.log('[STOMP]', str),
    onConnect: () => {
      console.log('✅ Connected to WebSocket');

      stompClient.subscribe('/topic/payment-status', message => {
        const data = JSON.parse(message.body);
        if (data.md5 === md5) {
          onMessage(data.status);
        }
      });
    },
    onStompError: frame => {
      console.error('❌ Broker error:', frame.headers['message']);
    }
  });

  const connect = () => stompClient.activate();
  const disconnect = () => {
    if (stompClient && stompClient.active) {
      stompClient.deactivate();
      console.log('🛑 WebSocket disconnected');
    }
  };

  return { connect, disconnect };
}
