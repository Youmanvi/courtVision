import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export interface ScoreData {
  id?: number;
  league_id: number;
  league_name?: string;
  user_id: number;
  username: string;
  total_score: number;
  average_score: number;
  players_evaluated: number;
  calculated_at: string;
  created_at?: string;
}

export interface LeaderboardMessage {
  league_id: number;
  league_name: string;
  scores: ScoreData[];
  timestamp: string;
  message_type: 'INITIAL_LOAD' | 'SCORES_UPDATED';
}

export class LeaderboardService {
  private stompClient: Client | null = null;
  private subscriptions: Map<number, string> = new Map();
  private messageCallbacks: Map<number, (message: LeaderboardMessage) => void> = new Map();

  /**
   * Connect to WebSocket server
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws/scores'),
        debug: (msg: string) => console.log('[STOMP]', msg),
        onConnect: () => {
          console.log('WebSocket connected');
          resolve();
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame);
          reject(new Error('WebSocket connection failed'));
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.activate();
    });
  }

  /**
   * Subscribe to leaderboard updates for a league
   */
  subscribeToLeaderboard(
    leagueId: number,
    onMessage: (message: LeaderboardMessage) => void
  ): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('WebSocket not connected');
      return;
    }

    // Store callback
    this.messageCallbacks.set(leagueId, onMessage);

    // Subscribe to leaderboard topic
    const subscription = this.stompClient.subscribe(
      `/topic/leaderboard/${leagueId}`,
      (message) => {
        try {
          const leaderboardMessage = JSON.parse(message.body) as LeaderboardMessage;
          onMessage(leaderboardMessage);
        } catch (error) {
          console.error('Failed to parse leaderboard message:', error);
        }
      }
    );

    // Store subscription ID for later unsubscribe
    this.subscriptions.set(leagueId, subscription.id);

    // Send subscription request
    this.stompClient.publish({
      destination: `/app/leaderboard/subscribe/${leagueId}`,
      body: JSON.stringify({ leagueId }),
    });
  }

  /**
   * Unsubscribe from leaderboard updates
   */
  unsubscribeFromLeaderboard(leagueId: number): void {
    const subscriptionId = this.subscriptions.get(leagueId);
    if (subscriptionId && this.stompClient) {
      this.stompClient.unsubscribe(subscriptionId);
      this.subscriptions.delete(leagueId);
      this.messageCallbacks.delete(leagueId);
    }
  }

  /**
   * Disconnect WebSocket
   */
  disconnect(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.deactivate();
    }
    this.subscriptions.clear();
    this.messageCallbacks.clear();
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.stompClient?.connected || false;
  }
}

export default new LeaderboardService();
