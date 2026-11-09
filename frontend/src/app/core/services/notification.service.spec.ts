import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting,HttpTestingController} from '@angular/common/http/testing';
import {NotificationService} from './notification.service';
describe('Notification load and failure states',()=>{
 let service:NotificationService,http:HttpTestingController;
 beforeEach(()=>{TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});service=TestBed.inject(NotificationService);http=TestBed.inject(HttpTestingController);});
 afterEach(()=>http.verify());
 it('keeps loading until the HTTP page arrives',()=>{service.load();expect(service.loading()).toBeTrue();http.expectOne(r=>r.url.endsWith('/unread-count')).flush({count:1});http.expectOne(r=>r.url.endsWith('/backoffice/notifications')).flush({content:[{id:'n',read:false}]});expect(service.loading()).toBeFalse();expect(service.unreadCount()).toBe(1);});
 it('does not disguise a failed load as successful emptiness',()=>{service.load();http.expectOne(r=>r.url.endsWith('/unread-count')).flush({count:0});http.expectOne(r=>r.url.endsWith('/backoffice/notifications')).flush({}, {status:503,statusText:'Unavailable'});expect(service.error()).toBeTruthy();expect(service.loading()).toBeFalse();});
 it('uses server pagination and a global unread count',()=>{service.load(2);http.expectOne(r=>r.url.endsWith('/unread-count')).flush({count:41});const r=http.expectOne(r=>r.url.endsWith('/backoffice/notifications'));expect(r.request.params.get('page')).toBe('2');r.flush({content:[],totalElements:60});expect(service.unreadCount()).toBe(41);expect(service.total()).toBe(60);});
 it('keeps a rejected notification unread and exposes the error',()=>{service.notifications.set([{id:'n',read:false} as any]);service.markAsRead('n').subscribe({error:()=>{}});http.expectOne(r=>r.url.endsWith('/n/read')).flush({}, {status:403,statusText:'Forbidden'});expect(service.notifications()[0].read).toBeFalse();expect(service.error()).toBeTruthy();expect(service.busy()).toBeFalse();});
});
