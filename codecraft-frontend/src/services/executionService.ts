import axios from 'axios';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const API_BASE = 'http://localhost:8080/api';

class ExecutionService {
  private stompClient: Client | null = null;

  async execute(projectId: string, language: string) {
    try {
      const token = localStorage.getItem('token');
      
      const response = await axios.post(
        `${API_BASE}/executions`,
        { 
          projectId, 
          language: language.toUpperCase()
        },
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );
      return response.data;
    } catch (error) {
      console.error('Failed to start execution', error);
      throw error;
    }
  }

  connectWebSocket(executionId: string, onMessage: (message: any) => void) {
    const socket = new SockJS('http://localhost:8083/ws/execution');
    
    this.stompClient = new Client({
      webSocketFactory: () => socket as any,
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = () => {
      console.log('Connected to WebSocket');
      
      this.stompClient?.subscribe(`/topic/execution/${executionId}`, (message) => {
        const body = JSON.parse(message.body);
        onMessage(body);
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}

export const executionService = new ExecutionService();
