import { Injectable, OnDestroy, effect } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { signal } from '@angular/core';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

export interface WebSocketMessage { type: string; data: unknown; timestamp: string; }

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private client?: Client;
  private token: string | null = null;
  private channels = new Map<string, Subject<any>>();
  private connected = signal(false);
  readonly connectionStatus = this.connected.asReadonly();

  constructor(private auth: AuthService) {
    effect(() => {
      const user = this.auth.user();
      const token = user ? this.auth.getToken() : null;
      if (token === this.token) return;
      this.disconnect();
      this.token = token;
      if (token) this.connect();
    });
  }

  connect(_url?: string): void {
    if (this.client || !this.auth.getToken()) return;
    const endpoint = environment.wsUrl.replace(/^ws:/, 'http:').replace(/^wss:/, 'https:');
    const client = new Client({
      webSocketFactory: () => new SockJS(endpoint),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      // Credentials belong in the STOMP CONNECT frame, never in the URL.
      beforeConnect: async () => {
        const token = this.auth.getToken();
        if (!token) { await client.deactivate(); return; }
        client.connectHeaders = { Authorization: 'Bearer ' + token };
      },
      onConnect: () => {
        this.connected.set(true);
        for (const channel of ['notifications', 'pipeline']) {
          client.subscribe('/user/queue/' + channel, message => this.receive(channel, message.body));
        }
        if (this.auth.hasRole('SUPER_ADMIN') && this.auth.hasPermission('TENANT_ACCESS_ALL')
            && this.auth.hasPermission('STATISTICS_READ')) {
          client.subscribe('/topic/dashboard', message => this.receive('dashboard', message.body));
        }
      },
      onWebSocketClose: () => this.connected.set(false),
      onStompError: () => { this.connected.set(false); void client.deactivate(); }
    });
    this.client = client;
    client.activate();
  }

  private receive(channel: string, body: string): void {
    try { this.subject(channel).next(JSON.parse(body)); } catch { /* Invalid frames are discarded. */ }
  }
  private subject(channel: string): Subject<any> {
    if (!this.channels.has(channel)) this.channels.set(channel, new Subject<any>());
    return this.channels.get(channel)!;
  }
  subscribe(channel: string): Observable<any> { return this.subject(channel).asObservable(); }
  send(_url: string, _message: unknown): void {
    throw new Error('Client publications are not supported by this server.');
  }
  disconnect(_url?: string): void {
    const client = this.client;
    this.client = undefined;
    this.token = null;
    this.connected.set(false);
    if (client) void client.deactivate();
  }
  ngOnDestroy(): void {
    this.disconnect();
    this.channels.forEach(subject => subject.complete());
  }
}
