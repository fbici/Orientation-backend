import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { WebSocketService } from './websocket.service';

export interface RealtimeEvent {
  type: string; channel: string; data: any; timestamp: string; userId?: string;
}
@Injectable({ providedIn: 'root' })
export class RealtimeService {
  readonly connectionStatus = this.socket.connectionStatus;
  constructor(private socket: WebSocketService) {}
  connect(url: string): void { this.socket.connect(url); }
  subscribe(channel: string): Observable<RealtimeEvent> { return this.socket.subscribe(channel); }
  send(url: string, message: unknown): void { this.socket.send(url, message); }
  disconnect(url: string): void { this.socket.disconnect(url); }
  disconnectAll(): void { this.socket.disconnect(); }
}
