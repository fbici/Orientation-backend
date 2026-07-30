import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

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
  private readonly API = '/api/v1/notifications';

  notifications = signal<Notification[]>([]);
  unreadCount = signal(0);

  constructor(private http: HttpClient) {}

  load(): void {
    this.http.get<any>(`${this.API}`).subscribe({
      next: (res) => {
        const items = res?.content || res?.notifications || res || [];
        this.notifications.set(Array.isArray(items) ? items : []);
        this.unreadCount.set(this.notifications().filter(n => !n.read).length);
      },
      error: () => {}
    });
  }

  markAsRead(id: string): Observable<any> {
    return this.http.put(`${this.API}/${id}/read`, {}).pipe(
      tap(() => {
        this.notifications.update(list =>
          list.map(n => n.id === id ? { ...n, read: true } : n)
        );
        this.unreadCount.set(this.notifications().filter(n => !n.read).length);
      })
    );
  }

  markAllAsRead(): Observable<any> {
    return this.http.put(`${this.API}/read-all`, {}).pipe(
      tap(() => {
        this.notifications.update(list => list.map(n => ({ ...n, read: true })));
        this.unreadCount.set(0);
      })
    );
  }
}
