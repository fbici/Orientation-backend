import { Component, signal, computed, HostListener, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/services/auth.service';
import { NotificationService } from '../core/services/notification.service';

interface NavItem {
  icon: string;
  label: string;
  route: string;
  roles?: string[];
  badge?: number;
}

interface NavSection {
  title: string;
  items: NavItem[];
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, FormsModule],
  template: `
    <div class="shell" [class.collapsed]="collapsed()">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-brand">
          <div class="brand-mark"><span class="material-symbols-rounded filled">school</span></div>
          @if (!collapsed()) { <div class="brand-text"><span class="brand-name">Orientation</span><span class="brand-sub">Administration</span></div> }
        </div>
        <nav class="sidebar-nav">
          @for (sec of sections(); track sec.title) {
            <div class="nav-group">
              @if (!collapsed()) { <div class="nav-group-label">{{ sec.title }}</div> }
              @for (item of sec.items; track item.route) {
                @if (!item.roles || auth.hasAnyRole(item.roles)) {
                  <a [routerLink]="item.route" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: item.route === '/dashboard' }" class="nav-link" [attr.data-tip]="collapsed() ? item.label : null">
                    <span class="nav-link-icon material-symbols-rounded">{{ item.icon }}</span>
                    @if (!collapsed()) { <span class="nav-link-text">{{ item.label }}</span> }
                    @if (item.route === '/notifications' && notifSvc.unreadCount() > 0 && !collapsed()) {
                      <span class="nav-badge">{{ notifSvc.unreadCount() }}</span>
                    }
                  </a>
                }
              }
            </div>
          }
        </nav>
        <button class="sidebar-collapse-btn" (click)="collapsed.set(!collapsed())">
          <span class="material-symbols-rounded">{{ collapsed() ? 'chevron_right' : 'chevron_left' }}</span>
        </button>
      </aside>

      <!-- Main -->
      <div class="main">
        <header class="topbar">
          <div class="topbar-left">
            <button class="btn btn-ghost btn-icon mobile-trigger" (click)="collapsed.set(!collapsed())"><span class="material-symbols-rounded">menu</span></button>
            <div class="search"><span class="material-symbols-rounded search-icon">search</span><input class="search-input" type="text" placeholder="Rechercher…" [(ngModel)]="q"></div>
          </div>
          <div class="topbar-right">
            <div class="topbar-pill online"><span class="dot green" style="animation:pulse 2s infinite"></span> Opérationnel</div>

            <!-- Notifications -->
            <button class="btn btn-ghost btn-icon topbar-icon-btn" (click)="toggleNotifPanel()">
              <span class="material-symbols-rounded">notifications</span>
              @if (notifSvc.unreadCount() > 0) { <span class="icon-badge">{{ notifSvc.unreadCount() }}</span> }
            </button>

            @if (auth.hasRole('ADMIN') || auth.hasRole('SUPER_ADMIN')) {
              <a routerLink="/settings" class="btn btn-ghost btn-icon topbar-icon-btn"><span class="material-symbols-rounded">settings</span></a>
            }

            <div class="topbar-sep"></div>

            <!-- User menu -->
            <div class="user-trigger" (click)="menuOpen.set(!menuOpen())">
              <div class="avatar" style="background:var(--brand);font-size:.6875rem">{{ auth.initials() }}</div>
              <div class="user-meta">
                <span class="user-meta-name">{{ auth.fullName() }}</span>
                <span class="user-meta-role">{{ auth.user()?.roles?.[0] }}</span>
              </div>
              <span class="material-symbols-rounded" style="font-size:18px;color:var(--n-400)">expand_more</span>
            </div>

            @if (menuOpen()) {
              <div class="overlay" (click)="menuOpen.set(false)"></div>
              <div class="dropdown-menu" style="min-width:240px">
                <div style="padding:12px 16px;border-bottom:1px solid var(--n-100)">
                  <div style="font-size:.8125rem;font-weight:600;color:var(--n-900)">{{ auth.fullName() }}</div>
                  <div style="font-size:.6875rem;color:var(--n-500)">{{ auth.user()?.email }}</div>
                </div>
                <a routerLink="/profile" class="dropdown-item" (click)="menuOpen.set(false)">
                  <span class="material-symbols-rounded">person</span>Mon profil
                </a>
                <a routerLink="/settings" class="dropdown-item" (click)="menuOpen.set(false)">
                  <span class="material-symbols-rounded">settings</span>Paramètres
                </a>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item danger" (click)="logout()">
                  <span class="material-symbols-rounded">logout</span>Se déconnecter
                </button>
              </div>
            }

            <!-- Notification panel -->
            @if (notifPanelOpen()) {
              <div class="overlay" (click)="notifPanelOpen.set(false)"></div>
              <div class="dropdown-menu notif-panel">
                <div style="padding:14px 16px;border-bottom:1px solid var(--n-100);display:flex;align-items:center;justify-content:space-between">
                  <span style="font-size:.875rem;font-weight:700;color:var(--n-900)">Notifications</span>
                  @if (notifSvc.unreadCount() > 0) {
                    <button class="btn btn-ghost btn-sm" (click)="markAllRead()">Tout marquer lu</button>
                  }
                </div>
                <div style="max-height:360px;overflow-y:auto">
                  @for (n of notifSvc.notifications(); track n.id) {
                    <div class="notif-item" [class.unread]="!n.read" (click)="readNotif(n.id)">
                      <div class="notif-dot-wrap">@if (!n.read) { <div class="notif-dot"></div> }</div>
                      <div style="flex:1;min-width:0">
                        <div style="font-size:.8125rem;color:var(--n-800);line-height:1.45">{{ n.title || n.message }}</div>
                        <div style="font-size:.6875rem;color:var(--n-400);margin-top:3px">{{ n.createdAt | date:'short' }}</div>
                      </div>
                    </div>
                  } @empty {
                    <div style="padding:32px 16px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucune notification</div>
                  }
                </div>
                <div style="padding:10px 16px;border-top:1px solid var(--n-100);text-align:center">
                  <a routerLink="/notifications" class="btn btn-ghost btn-sm" (click)="notifPanelOpen.set(false)" style="width:100%">Voir toutes les notifications</a>
                </div>
              </div>
            }
          </div>
        </header>
        <main class="content"><router-outlet></router-outlet></main>
      </div>
    </div>
  `,
  styles: [`
    .shell{display:flex;height:100vh;overflow:hidden}
    .sidebar{width:var(--sidebar-w);background:var(--n-950);display:flex;flex-direction:column;transition:width var(--dur-slow) var(--ease-out);z-index:20;flex-shrink:0}
    .shell.collapsed .sidebar{width:var(--sidebar-collapsed)}
    .main{flex:1;display:flex;flex-direction:column;overflow:hidden;min-width:0}
    .sidebar-brand{display:flex;align-items:center;gap:12px;padding:20px 20px 24px;border-bottom:1px solid hsla(0,0%,100%,.06)}
    .brand-mark{width:34px;height:34px;background:var(--brand);border-radius:var(--radius-md);display:flex;align-items:center;justify-content:center;flex-shrink:0}
    .brand-mark .material-symbols-rounded{font-size:20px;color:#fff}
    .brand-text{display:flex;flex-direction:column;overflow:hidden}
    .brand-name{font-size:.9375rem;font-weight:800;color:#fff;letter-spacing:-.02em;white-space:nowrap}
    .brand-sub{font-size:.625rem;color:var(--n-500);text-transform:uppercase;letter-spacing:.08em;white-space:nowrap}
    .sidebar-nav{flex:1;overflow-y:auto;overflow-x:hidden;padding:12px 10px}
    .nav-group{margin-bottom:6px}
    .nav-group-label{font-size:.625rem;font-weight:700;color:var(--n-500);text-transform:uppercase;letter-spacing:.1em;padding:10px 12px 4px;white-space:nowrap}
    .nav-link{display:flex;align-items:center;gap:11px;padding:8px 12px;color:var(--n-400);text-decoration:none;border-radius:var(--radius-sm);transition:all var(--dur-fast);margin-bottom:1px;position:relative;white-space:nowrap}
    .nav-link:hover{color:#fff;background:hsla(0,0%,100%,.06)}.nav-link.active{color:#fff;background:var(--brand)}.nav-link.active .nav-link-icon{color:#fff}
    .nav-link-icon{font-size:20px;flex-shrink:0;width:20px;text-align:center}
    .nav-link-text{font-size:.8125rem;font-weight:500;overflow:hidden;text-overflow:ellipsis}
    .nav-badge{margin-left:auto;background:var(--red-500);color:#fff;font-size:.5625rem;font-weight:700;padding:2px 6px;border-radius:var(--radius-full);min-width:18px;text-align:center}
    .sidebar-collapse-btn{margin:12px;border-top:1px solid hsla(0,0%,100%,.06);padding:12px;display:flex;align-items:center;justify-content:center;background:none;border-left:none;border-right:none;border-bottom:none;cursor:pointer;color:var(--n-500);transition:color var(--dur-fast)}.sidebar-collapse-btn:hover{color:#fff}
    .topbar{height:var(--topbar-h);background:#fff;border-bottom:1px solid var(--n-200);display:flex;align-items:center;justify-content:space-between;padding:0 24px;flex-shrink:0;z-index:10}
    .topbar-left{display:flex;align-items:center;gap:16px}.mobile-trigger{display:none}
    .search{position:relative}.search-icon{position:absolute;left:10px;top:50%;transform:translateY(-50%);font-size:18px;color:var(--n-400)}
    .search-input{width:300px;padding:7px 12px 7px 36px;font-size:.8125rem;font-family:inherit;color:var(--n-800);background:var(--n-50);border:1px solid var(--n-200);border-radius:var(--radius-sm);outline:none;transition:all var(--dur-base)}
    .search-input:focus{background:#fff;border-color:var(--brand-light);box-shadow:var(--shadow-ring);width:380px}.search-input::placeholder{color:var(--n-400)}
    .topbar-right{display:flex;align-items:center;gap:6px;position:relative}
    .topbar-pill{display:flex;align-items:center;gap:6px;padding:4px 12px;border-radius:var(--radius-full);font-size:.6875rem;font-weight:600}
    .topbar-pill.online{background:var(--green-50);color:var(--green-700)}
    .topbar-icon-btn{position:relative;color:var(--n-500)}.topbar-icon-btn:hover{color:var(--n-700)}
    .icon-badge{position:absolute;top:2px;right:2px;width:16px;height:16px;background:var(--red-500);color:#fff;font-size:.5625rem;font-weight:700;border-radius:var(--radius-full);display:flex;align-items:center;justify-content:center;border:2px solid #fff}
    .topbar-sep{width:1px;height:24px;background:var(--n-200);margin:0 8px}
    .user-trigger{display:flex;align-items:center;gap:10px;padding:4px 8px;border-radius:var(--radius-sm);cursor:pointer;transition:background var(--dur-fast);margin-left:4px}.user-trigger:hover{background:var(--n-50)}
    .user-meta{display:flex;flex-direction:column}.user-meta-name{font-size:.8125rem;font-weight:600;color:var(--n-800);line-height:1.2}.user-meta-role{font-size:.625rem;color:var(--n-500);text-transform:uppercase;letter-spacing:.04em}
    .content{flex:1;padding:28px;overflow-y:auto;background:var(--n-50)}
    .notif-panel{min-width:360px;max-width:400px}
    .notif-item{display:flex;gap:10px;padding:12px 16px;border-bottom:1px solid var(--n-100);cursor:pointer;transition:background var(--dur-fast)}.notif-item:hover{background:var(--n-50)}.notif-item.unread{background:var(--brand-50)}
    .notif-dot-wrap{width:8px;display:flex;align-items:flex-start;padding-top:5px}.notif-dot{width:8px;height:8px;border-radius:50%;background:var(--brand)}
    @media(max-width:1024px){.search-input{width:200px}.search-input:focus{width:260px}.user-meta{display:none}.notif-panel{min-width:300px}}
    @media(max-width:768px){.sidebar{position:fixed;left:0;top:0;bottom:0;transform:translateX(-100%);z-index:30}.shell.collapsed .sidebar{transform:translateX(0);width:var(--sidebar-w)}.mobile-trigger{display:flex}.content{padding:20px 16px}}
  `]
})
export class LayoutComponent implements OnInit {
  collapsed = signal(false);
  menuOpen = signal(false);
  notifPanelOpen = signal(false);
  q = '';

  sections = signal<NavSection[]>([
    { title: 'Vue d\'ensemble', items: [
      { icon: 'dashboard', label: 'Tableau de bord', route: '/dashboard' },
      { icon: 'analytics', label: 'Analytique', route: '/analytics' },
      { icon: 'monitor_heart', label: 'Monitoring', route: '/monitoring' },
    ]},
    { title: 'Métier', items: [
      { icon: 'school', label: 'Universités', route: '/universities' },
      { icon: 'recommend', label: 'Recommandations', route: '/recommendations' },
      { icon: 'description', label: 'Documents', route: '/documents' },
      { icon: 'upload_file', label: 'Imports de données', route: '/imports' },
      { icon: 'tune', label: 'Règles d\'admission', route: '/rules' },
      { icon: 'hub', label: 'Knowledge Engine', route: '/knowledge', roles: ['SUPER_ADMIN', 'ADMIN'] },
    ]},
    { title: 'Système', items: [
      { icon: 'notifications', label: 'Notifications', route: '/notifications' },
      { icon: 'admin_panel_settings', label: 'Administration', route: '/administration', roles: ['SUPER_ADMIN', 'ADMIN'] },
      { icon: 'summarize', label: 'Rapports', route: '/reports', roles: ['SUPER_ADMIN', 'ADMIN'] },
      { icon: 'settings', label: 'Paramètres', route: '/settings', roles: ['SUPER_ADMIN', 'ADMIN'] },
    ]}
  ]);

  constructor(
    public auth: AuthService,
    public notifSvc: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.notifSvc.load();
  }

  toggleNotifPanel(): void {
    this.notifPanelOpen.update(v => !v);
    if (this.notifPanelOpen()) {
      this.notifSvc.load();
    }
  }

  readNotif(id: string): void {
    this.notifSvc.markAsRead(id).subscribe();
  }

  markAllRead(): void {
    this.notifSvc.markAllAsRead().subscribe();
  }

  logout(): void {
    this.menuOpen.set(false);
    this.auth.logout();
  }

  @HostListener('document:keydown.escape') onEsc() {
    this.menuOpen.set(false);
    this.notifPanelOpen.set(false);
  }
}
