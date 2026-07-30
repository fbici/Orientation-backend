import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Notifications</h1><p>Centre de notifications et alertes système</p></div>
        <div class="page-header-actions">
          @if (notifSvc.unreadCount() > 0) {
            <button class="btn btn-secondary" (click)="markAllRead()"><span class="material-symbols-rounded">done_all</span>Tout marquer comme lu</button>
          }
        </div>
      </div>

      <div class="card anim-fade-up">
        <div class="card-header">
          <div style="display:flex;gap:4px">
            <button class="btn btn-sm" [class.btn-primary]="tab==='all'" [class.btn-ghost]="tab!=='all'" (click)="tab='all'">Toutes</button>
            <button class="btn btn-sm" [class.btn-primary]="tab==='unread'" [class.btn-ghost]="tab!=='unread'" (click)="tab='unread'">
              Non lues @if (notifSvc.unreadCount() > 0) { <span class="badge badge-primary" style="margin-left:4px">{{ notifSvc.unreadCount() }}</span> }
            </button>
          </div>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner-lg"></div></div>
          } @else {
            @for (n of displayed(); track n.id) {
              <div [style.background]="n.read ? 'transparent' : 'var(--brand-50)'"
                   style="display:flex;align-items:flex-start;gap:14px;padding:16px 24px;border-bottom:1px solid var(--n-100);cursor:pointer;transition:background var(--dur-fast)"
                   (click)="read(n.id)">
                <div style="width:8px;display:flex;align-items:flex-start;padding-top:6px">
                  @if (!n.read) { <div style="width:8px;height:8px;border-radius:50%;background:var(--brand)"></div> }
                </div>
                <div style="flex:1;min-width:0">
                  <div style="font-size:.8125rem;color:var(--n-800);line-height:1.5">{{ n.title || n.message }}</div>
                  @if (n.title && n.message && n.title !== n.message) {
                    <div style="font-size:.75rem;color:var(--n-500);margin-top:2px">{{ n.message }}</div>
                  }
                  <div style="font-size:.6875rem;color:var(--n-400);margin-top:4px">{{ n.createdAt | date:'short' }}</div>
                </div>
              </div>
            } @empty {
              <div style="padding:48px;text-align:center;color:var(--n-400)">
                <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px">notifications_off</span>
                Aucune notification
              </div>
            }
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host{display:block}
    .spinner-lg{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class NotificationsComponent implements OnInit {
  tab = 'all';
  loading = signal(false);

  constructor(public notifSvc: NotificationService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.notifSvc.load();
    // Simulate loading end
    setTimeout(() => this.loading.set(false), 500);
  }

  displayed(): any[] {
    const all = this.notifSvc.notifications();
    if (this.tab === 'unread') return all.filter(n => !n.read);
    return all;
  }

  read(id: string): void {
    this.notifSvc.markAsRead(id).subscribe();
  }

  markAllRead(): void {
    this.notifSvc.markAllAsRead().subscribe();
  }
}
