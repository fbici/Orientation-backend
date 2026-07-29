import { Injectable, OnDestroy, signal } from '@angular/core';
import { Subject, Observable, timer, Subscription } from 'rxjs';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';

export interface RealtimeEvent {
  type: string;
  channel: string;
  data: any;
  timestamp: string;
  userId?: string;
}

@Injectable({ providedIn: 'root' })
export class RealtimeService implements OnDestroy {

  private connections = new Map<string, WebSocketSubject<any>>();
  private subscriptions = new Map<string, Subject<RealtimeEvent>>();
  private reconnectAttempts = new Map<string, number>();
  private connected = signal<boolean>(false);

  connectionStatus = this.connected.asReadonly();

  connect(url: string): void {
    if (this.connections.has(url)) return;

    const ws = webSocket({
      url,
      openObserver: {
        next: () => {
          console.log(`WebSocket connected: ${url}`);
          this.connected.set(true);
          this.reconnectAttempts.set(url, 0);
        }
      },
      closeObserver: {
        next: () => {
          console.log(`WebSocket disconnected: ${url}`);
          this.connected.set(false);
          this.scheduleReconnect(url);
        }
      }
    });

    this.connections.set(url, ws);
  }

  subscribe(channel: string): Observable<RealtimeEvent> {
    if (!this.subscriptions.has(channel)) {
      this.subscriptions.set(channel, new Subject<RealtimeEvent>());
    }
    return this.subscriptions.get(channel)!.asObservable();
  }

  send(url: string, message: any): void {
    const ws = this.connections.get(url);
    if (ws) {
      ws.next(message);
    }
  }

  disconnect(url: string): void {
    const ws = this.connections.get(url);
    if (ws) {
      ws.complete();
      this.connections.delete(url);
    }
  }

  disconnectAll(): void {
    this.connections.forEach((ws, url) => ws.complete());
    this.connections.clear();
    this.connected.set(false);
  }

  private scheduleReconnect(url: string): void {
    const attempts = this.reconnectAttempts.get(url) || 0;
    const delay = Math.min(1000 * Math.pow(2, attempts), 30000);

    timer(delay).subscribe(() => {
      console.log(`Reconnecting to ${url} (attempt ${attempts + 1})`);
      this.reconnectAttempts.set(url, attempts + 1);
      this.connect(url);
    });
  }

  ngOnDestroy(): void {
    this.disconnectAll();
  }
}
