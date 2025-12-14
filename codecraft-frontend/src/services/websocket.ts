import { Client, IMessage } from '@stomp/stompjs';

export class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, any> = new Map();

  connect(onConnect?: () => void) {
    if (this.client?.connected) {
      if (onConnect) onConnect();
      return;
    }

    this.client = new Client({
      brokerURL: 'ws://localhost:8083/ws/execution',
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('✅ WebSocket connected');
        if (onConnect) onConnect();
      },
      onStompError: (frame) => {
        console.error('❌ STOMP error:', frame);
      },
      onWebSocketError: (event) => {
        console.error('❌ WebSocket error:', event);
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed');
      }
    });

    this.client.activate();
  }

  subscribeLogs(executionId: string, callback: (message: any) => void) {
    if (!this.client) {
      console.error('WebSocket not connected');
      return;
    }

    console.log('📡 Subscribing to:', `/topic/execution/${executionId}`);

    const subscription = this.client.subscribe(
      `/topic/execution/${executionId}`,  // ✅ FIXED!
      (message: IMessage) => {
        console.log('📨 Received message:', message.body);
        try {
          const log = JSON.parse(message.body);
          console.log('✅ Parsed log:', log);
          callback(log);
        } catch (e) {
          console.error('Failed to parse log message:', e);
        }
      }
    );

    this.subscriptions.set(`logs-${executionId}`, subscription);
  }

  unsubscribeLogs(executionId: string) {
    const key = `logs-${executionId}`;
    const subscription = this.subscriptions.get(key);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(key);
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.subscriptions.clear();
    }
  }
}

export const wsService = new WebSocketService();
