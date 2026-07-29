import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [DatePipe],
  template: `
    <div class="notifications">
      <h1>Notifications</h1>

      <div class="stats-bar">
        <span>Non lues: <strong>{{ unreadCount() }}</strong></span>
        <button (click)="markAllRead()">Tout marquer comme lu</button>
      </div>

      <div class="notifications-list">
        @if (notifications()?.content?.length) {
          @for (notif of notifications().content; track notif.id) {
            <div class="notification-item" [class.unread]="!notif.read">
              <div class="notif-type">{{ notif.type }}</div>
              <div class="notif-content">
                <strong>{{ notif.title }}</strong>
                <p>{{ notif.message }}</p>
              </div>
              <div class="notif-time">{{ notif.createdAt | date:'short' }}</div>
            </div>
          }
        } @else {
          <p>Aucune notification.</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .notifications { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .stats-bar { background: white; padding: 15px 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
    .stats-bar button { padding: 8px 16px; background: #1a1a2e; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .notification-item { background: white; padding: 15px 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 10px; display: flex; gap: 15px; align-items: center; }
    .notification-item.unread { border-left: 4px solid #1a1a2e; }
    .notif-type { background: #e8f4fd; padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; min-width: 80px; text-align: center; }
    .notif-content { flex: 1; }
    .notif-content p { margin: 5px 0 0; color: #666; font-size: 0.9rem; }
    .notif-time { color: #999; font-size: 0.85rem; }
  `]
})
export class NotificationsComponent implements OnInit {
  notifications = signal<any>(null);
  unreadCount = signal(0);

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  private loadNotifications(): void {
    this.api.getNotifications('current-user').subscribe({
      next: (data) => this.notifications.set(data)
    });
    this.api.getUnreadCount('current-user').subscribe({
      next: (data) => this.unreadCount.set(data.count)
    });
  }

  markAllRead(): void {
    // Would call API to mark all as read
    this.unreadCount.set(0);
  }
}
