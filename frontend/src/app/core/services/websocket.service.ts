import { Injectable, OnDestroy, signal } from '@angular/core';
import { Subject, Observable, timer } from 'rxjs';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { retry } from 'rxjs/operators';

export interface WebSocketMessage {
  type: string;
  data: any;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  private connections = new Map<string, WebSocketSubject<any>>();
  private subjects = new Map<string, Subject<any>>();
  private connected = signal<boolean>(false);

  connectionStatus = this.connected.asReadonly();

  connect(url: string): WebSocketSubject<any> {
    if (this.connections.has(url)) {
      return this.connections.get(url)!;
    }

    const ws = webSocket({
      url: url,
      openObserver: {
        next: () => {
          console.log(`WebSocket connected: ${url}`);
          this.connected.set(true);
        }
      },
      closeObserver: {
        next: () => {
          console.log(`WebSocket disconnected: ${url}`);
          this.connected.set(false);
          this.reconnect(url);
        }
      }
    });

    this.connections.set(url, ws);
    return ws;
  }

  subscribe(channel: string): Observable<any> {
    if (!this.subjects.has(channel)) {
      this.subjects.set(channel, new Subject<any>());
    }
    return this.subjects.get(channel)!.asObservable();
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

  private reconnect(url: string): void {
    timer(5000).subscribe(() => {
      console.log(`Attempting to reconnect: ${url}`);
      this.connect(url);
    });
  }

  ngOnDestroy(): void {
    this.connections.forEach(ws => ws.complete());
    this.connections.clear();
  }
}
