import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, tap, finalize, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Notification {
  id: string;
  type: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  data?: any;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly API = `${environment.apiUrl}/backoffice/notifications`;

  notifications = signal<Notification[]>([]);
  unreadCount = signal(0);
  loading = signal(false);
  error = signal('');
  busy = signal(false);
  page=signal(0);total=signal(0);
  private generation=0;

  constructor(private http: HttpClient) {}

  load(page=0): void {
    const generation=++this.generation;this.page.set(page);
    this.loading.set(true);this.error.set('');this.notifications.set([]);
    forkJoin({page:this.http.get<any>(this.API,{params:{page:String(page),size:'20'}}),
      unread:this.http.get<{count:number}>(this.API+'/unread-count')})
      .pipe(finalize(()=>{if(generation===this.generation)this.loading.set(false);}))
      .subscribe({next:result=>{
        if(generation!==this.generation)return;
        this.notifications.set(result.page.content || []);
        this.total.set(result.page.totalElements ?? this.notifications().length);
        this.unreadCount.set(result.unread.count);
      },error:()=>{if(generation===this.generation){this.unreadCount.set(0);this.error.set('Notifications indisponibles ou acces refuse. Reessayez.');}}});
  }

  markAsRead(id: string): Observable<any> {
    this.busy.set(true);this.error.set('');
    return this.http.post(`${this.API}/${id}/read`, {}).pipe(
      finalize(()=>this.busy.set(false)),
      catchError(e=>{this.error.set('Action sur les notifications refusee ou indisponible.');return throwError(()=>e);}),
      tap(() => {
        const unread=this.notifications().some(n=>n.id===id && !n.read);
        this.notifications.update(list => list.map(n => n.id === id ? { ...n, read: true } : n));
        if(unread)this.unreadCount.update(count=>Math.max(0,count-1));
      })
    );
  }

  markAllAsRead(): Observable<any> {
    this.busy.set(true);this.error.set('');
    return this.http.post(`${this.API}/read-all`, {}).pipe(
      finalize(()=>this.busy.set(false)),
      catchError(e=>{this.error.set('Action sur les notifications refusee ou indisponible.');return throwError(()=>e);}),
      tap(() => {
        this.notifications.update(list => list.map(n => ({ ...n, read: true })));
        this.unreadCount.set(0);
      })
    );
  }
}
