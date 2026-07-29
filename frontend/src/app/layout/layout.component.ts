import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/services/auth.service';
import { RealtimeService } from '../core/services/realtime.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule],
  template: `
    <div class="layout" [class.sidebar-collapsed]="sidebarCollapsed()">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="logo" [class.collapsed]="sidebarCollapsed()">
            <span class="logo-icon">🎯</span>
            @if (!sidebarCollapsed()) {
              <span class="logo-text">Orientation</span>
            }
          </div>
          <button class="toggle-btn" (click)="toggleSidebar()">
            {{ sidebarCollapsed() ? '▶' : '◀' }}
          </button>
        </div>

        <nav class="sidebar-nav">
          <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">📊</span>
            @if (!sidebarCollapsed()) { <span>Dashboard</span> }
          </a>
          <a routerLink="/analytics" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">📈</span>
            @if (!sidebarCollapsed()) { <span>Analytics</span> }
          </a>
          <a routerLink="/monitoring" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">🖥️</span>
            @if (!sidebarCollapsed()) { <span>Monitoring</span> }
          </a>

          <div class="nav-divider"></div>
          @if (!sidebarCollapsed()) {
            <div class="nav-section">MÉTIER</div>
          }

          <a routerLink="/recommendations" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">🎯</span>
            @if (!sidebarCollapsed()) { <span>Recommandations</span> }
          </a>
          <a routerLink="/documents" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">📄</span>
            @if (!sidebarCollapsed()) { <span>Documents</span> }
          </a>
          <a routerLink="/imports" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">📥</span>
            @if (!sidebarCollapsed()) { <span>Imports</span> }
          </a>
          <a routerLink="/rules" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">⚡</span>
            @if (!sidebarCollapsed()) { <span>Règles</span> }
          </a>

          <div class="nav-divider"></div>
          @if (!sidebarCollapsed()) {
            <div class="nav-section">SYSTEME</div>
          }

          <a routerLink="/notifications" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">🔔</span>
            @if (!sidebarCollapsed()) { <span>Notifications</span> }
          </a>
          @if (auth.hasRole('ADMIN')) {
            <a routerLink="/administration" routerLinkActive="active" class="nav-item">
              <span class="nav-icon">👥</span>
              @if (!sidebarCollapsed()) { <span>Administration</span> }
            </a>
            <a routerLink="/reports" routerLinkActive="active" class="nav-item">
              <span class="nav-icon">📊</span>
              @if (!sidebarCollapsed()) { <span>Rapports</span> }
            </a>
            <a routerLink="/settings" routerLinkActive="active" class="nav-item">
              <span class="nav-icon">⚙️</span>
              @if (!sidebarCollapsed()) { <span>Paramètres</span> }
            </a>
          }
        </nav>

        <div class="sidebar-footer">
          <a routerLink="/profile" routerLinkActive="active" class="nav-item">
            <span class="nav-icon">👤</span>
            @if (!sidebarCollapsed()) {
              <span>{{ auth.user()?.firstName }}</span>
            }
          </a>
        </div>
      </aside>

      <!-- Main Content -->
      <div class="main-wrapper">
        <header class="topbar">
          <div class="topbar-left">
            <h2>Back Office Orientation</h2>
          </div>
          <div class="topbar-right">
            <div class="search-box">
              <input type="text" placeholder="Rechercher..." [(ngModel)]="searchQuery">
              <span class="search-icon">🔍</span>
            </div>
            <div class="notification-bell" (click)="toggleNotifications()">
              🔔
              @if (unreadCount() > 0) {
                <span class="notification-badge">{{ unreadCount() }}</span>
              }
            </div>
            <div class="user-menu">
              <span class="user-name">{{ auth.user()?.firstName }} {{ auth.user()?.lastName }}</span>
              <span class="user-role">{{ auth.user()?.roles?.[0] }}</span>
            </div>
            <button class="logout-btn" (click)="auth.logout()">Déconnexion</button>
          </div>
        </header>

        <main class="content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .layout { display: flex; height: 100vh; overflow: hidden; }
    .sidebar { width: 260px; background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%); color: white; display: flex; flex-direction: column; transition: width 0.3s ease; z-index: 10; }
    .sidebar-collapsed .sidebar { width: 70px; }
    .sidebar-header { padding: 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
    .logo { display: flex; align-items: center; gap: 10px; }
    .logo-icon { font-size: 1.5rem; }
    .logo-text { font-size: 1.2rem; font-weight: bold; white-space: nowrap; }
    .toggle-btn { background: none; border: none; color: white; cursor: pointer; font-size: 1rem; padding: 5px; }
    .sidebar-nav { flex: 1; padding: 10px 0; overflow-y: auto; }
    .nav-item { display: flex; align-items: center; gap: 12px; padding: 12px 20px; color: rgba(255,255,255,0.7); text-decoration: none; transition: all 0.2s; border-radius: 0 20px 20px 0; margin-right: 10px; }
    .nav-item:hover { background: rgba(255,255,255,0.1); color: white; }
    .nav-item.active { background: rgba(255,255,255,0.15); color: white; font-weight: 500; }
    .nav-icon { font-size: 1.2rem; min-width: 24px; text-align: center; }
    .nav-divider { height: 1px; background: rgba(255,255,255,0.1); margin: 10px 20px; }
    .nav-section { padding: 5px 20px; font-size: 0.75rem; color: rgba(255,255,255,0.4); text-transform: uppercase; letter-spacing: 1px; }
    .sidebar-footer { padding: 10px 0; border-top: 1px solid rgba(255,255,255,0.1); }
    .main-wrapper { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
    .topbar { background: white; padding: 0 30px; display: flex; justify-content: space-between; align-items: center; height: 64px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); z-index: 5; }
    .topbar-left h2 { font-size: 1.2rem; color: #1a1a2e; margin: 0; }
    .topbar-right { display: flex; align-items: center; gap: 20px; }
    .search-box { position: relative; }
    .search-box input { padding: 8px 12px 8px 35px; border: 1px solid #e0e0e0; border-radius: 8px; width: 250px; font-size: 0.9rem; }
    .search-box input:focus { outline: none; border-color: #1a1a2e; }
    .search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); }
    .notification-bell { position: relative; cursor: pointer; font-size: 1.3rem; }
    .notification-badge { position: absolute; top: -5px; right: -5px; background: #e74c3c; color: white; font-size: 0.7rem; padding: 2px 6px; border-radius: 10px; }
    .user-menu { display: flex; flex-direction: column; align-items: flex-end; }
    .user-name { font-weight: 500; font-size: 0.9rem; }
    .user-role { font-size: 0.75rem; color: #666; }
    .logout-btn { background: #e74c3c; color: white; border: none; padding: 6px 14px; border-radius: 6px; cursor: pointer; font-size: 0.85rem; }
    .content { flex: 1; padding: 30px; overflow-y: auto; background: #f5f7fa; }
  `]
})
export class LayoutComponent {
  sidebarCollapsed = signal(false);
  searchQuery = '';
  unreadCount = signal(3);

  constructor(public auth: AuthService, private realtimeService: RealtimeService) {}

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  toggleNotifications(): void {
    // Toggle notification panel
  }
}
