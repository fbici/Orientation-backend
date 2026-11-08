import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { AuthService } from './auth.service';
import { WebSocketService } from './websocket.service';

describe('Authenticated STOMP transport', () => {
  let service: WebSocketService;
  let auth: any;
  beforeEach(() => {
    spyOn(Client.prototype, 'activate');
    spyOn(Client.prototype, 'deactivate').and.returnValue(Promise.resolve());
    auth = {
      user: signal<any>({ id: 'a', roles: ['ADMIN'] }),
      getToken: jasmine.createSpy().and.returnValue('access-token'),
      hasRole: jasmine.createSpy().and.returnValue(false),
      hasPermission: jasmine.createSpy().and.returnValue(false)
    };
    TestBed.configureTestingModule({ providers: [
      WebSocketService, { provide: AuthService, useValue: auth }
    ] });
    service = TestBed.inject(WebSocketService);
    TestBed.flushEffects();
  });
  afterEach(() => service.ngOnDestroy());
  function client(): Client { return (service as any).client; }

  it('puts credentials in CONNECT and routes only personal frames for a tenant admin', async () => {
    const c = client();
    await c.beforeConnect(c);
    expect(c.connectHeaders['Authorization']).toBe('Bearer access-token');
    const handlers = new Map<string, (frame: any) => void>();
    spyOn(c, 'subscribe').and.callFake((destination: string, callback: any) => {
      handlers.set(destination, callback);
      return { id: destination, unsubscribe: () => {} };
    });
    c.onConnect({} as any);
    expect([...handlers.keys()]).toEqual(['/user/queue/notifications', '/user/queue/pipeline']);
    let received: unknown;
    service.subscribe('notifications').subscribe(value => received = value);
    handlers.get('/user/queue/notifications')!({ body: '{"type":"ALERT"}' });
    expect(received).toEqual({ type: 'ALERT' });
    expect(service.connectionStatus()).toBeTrue();
  });

  it('subscribes to the global invalidation topic only with explicit global permissions', () => {
    auth.hasRole.and.returnValue(true); auth.hasPermission.and.returnValue(true);
    const c = client();
    const subscribe = spyOn(c, 'subscribe').and.returnValue({ id: 'x', unsubscribe: () => {} });
    c.onConnect({} as any);
    expect(subscribe.calls.allArgs().map(args => args[0])).toContain('/topic/dashboard');
  });

  it('stops reconnection on logout and replaces the connection after refresh', () => {
    const first = client();
    auth.getToken.and.returnValue('renewed-token');
    auth.user.set({ id: 'a', roles: ['ADMIN'] });
    TestBed.flushEffects();
    expect(first.deactivate).toHaveBeenCalled();
    expect(client()).not.toBe(first);
    auth.getToken.and.returnValue(null); auth.user.set(null);
    TestBed.flushEffects();
    expect((service as any).client).toBeUndefined();
    expect(service.connectionStatus()).toBeFalse();
  });
});
