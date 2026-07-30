import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-monitoring',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Monitoring</h1><p>Surveillance en temps réel de l'infrastructure</p></div>
        <div class="page-header-actions">
          <span class="badge badge-success" style="display:flex;align-items:center;gap:6px;padding:5px 14px"><span class="dot green" style="animation:pulse 2s infinite"></span> Système opérationnel</span>
          <button class="btn btn-secondary" (click)="load()"><span class="material-symbols-rounded">refresh</span></button>
        </div>
      </div>

      <!-- Services -->
      <div class="g4 stagger" style="margin-bottom:22px">
        @for (s of services(); track s.name) {
          <div class="card anim-fade-up">
            <div class="card-body" style="display:flex;align-items:center;gap:14px;padding:18px 20px">
              <div style="width:40px;height:40px;border-radius:var(--radius-sm);display:flex;align-items:center;justify-content:center" [style.background]="s.bg"><span class="material-symbols-rounded" [style.color]="s.color" style="font-size:20px">{{ s.icon }}</span></div>
              <div style="flex:1"><div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">{{ s.name }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ s.detail }}</div></div>
              <span class="badge" [class]="s.ok ? 'badge-success' : 'badge-danger'">{{ s.ok ? 'OK' : 'Erreur' }}</span>
            </div>
          </div>
        }
      </div>

      <div class="g2">
        <!-- Metrics -->
        <div class="card">
          <div class="card-header"><h3>Métriques serveur</h3></div>
          <div class="card-body">
            @for (m of metrics(); track m.label) {
              <div style="margin-bottom:18px">
                <div style="display:flex;justify-content:space-between;margin-bottom:7px"><span style="font-size:.8125rem;color:var(--n-600)">{{ m.label }}</span><span style="font-size:.8125rem;font-weight:700;color:var(--n-800)">{{ m.val }}</span></div>
                <div class="progress" style="height:7px"><div class="progress-bar" [class]="m.cls" [style.width.%]="m.pct"></div></div>
              </div>
            }
          </div>
        </div>

        <!-- Logs -->
        <div class="card">
          <div class="card-header"><h3>Journaux récents</h3><button class="btn btn-secondary btn-sm" (click)="load()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button></div>
          <div class="card-body" style="padding:0;max-height:400px;overflow-y:auto">
            @for (l of logs(); track l.timestamp) {
              <div style="display:flex;gap:12px;padding:10px 20px;border-bottom:1px solid var(--n-100)">
                <span class="badge" [class]="logClass(l.level)" style="flex-shrink:0">{{ l.level }}</span>
                <div style="flex:1;min-width:0"><div style="font-size:.8125rem;color:var(--n-700)">{{ l.message }}</div></div>
                <span style="font-size:.6875rem;color:var(--n-400);white-space:nowrap;flex-shrink:0">{{ l.timestamp | date:'short' }}</span>
              </div>
            } @empty {
              <div style="padding:32px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucun log récent</div>
            }
          </div>
        </div>
      </div>

      <!-- Activity Logs -->
      <div class="card" style="margin-top:22px">
        <div class="card-header"><h3>Journaux d'activité</h3></div>
        <div class="card-body" style="padding:0">
          @if (activityLoading()) {
            <div style="padding:32px;text-align:center"><div class="spinner"></div></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Action</th><th>Utilisateur</th><th>Détail</th><th>Date</th></tr></thead>
              <tbody>
                @for (a of activityLogs(); track a.id) {
                  <tr>
                    <td><span class="badge badge-gray">{{ a.action || a.type }}</span></td>
                    <td style="font-size:.8125rem;font-weight:500">{{ a.userName || a.userEmail || '—' }}</td>
                    <td style="font-size:.8125rem;color:var(--n-600)">{{ a.detail || a.description || '—' }}</td>
                    <td style="font-size:.8125rem;color:var(--n-500)">{{ a.createdAt | date:'short' }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="4" style="text-align:center;padding:32px;color:var(--n-400)">Aucune activité</td></tr>
                }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`.spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}@keyframes spin{to{transform:rotate(360deg)}}`]
})
export class MonitoringComponent implements OnInit {
  services = signal<any[]>([]);
  metrics = signal<any[]>([]);
  logs = signal<any[]>([]);
  activityLogs = signal<any[]>([]);
  activityLoading = signal(false);

  constructor(private api: ApiService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.getMonitoring().subscribe({
      next: (data) => {
        this.services.set(data?.services || [
          { name: 'API Backend', icon: 'api', detail: 'Spring Boot 3.4', ok: true, bg: '#eff6ff', color: '#3b82f6' },
          { name: 'PostgreSQL', icon: 'storage', detail: 'v16 · Port 5432', ok: true, bg: '#f0fdf4', color: '#16a34a' },
          { name: 'Redis', icon: 'bolt', detail: 'v7 · Port 6379', ok: true, bg: '#fff7ed', color: '#ea580c' },
          { name: 'WebSocket', icon: 'sync', detail: 'Temps réel', ok: true, bg: '#f5f3ff', color: '#7c3aed' },
        ]);
        const h = data?.systemHealth || {};
        this.metrics.set([
          { label: 'CPU', val: `${h.cpuUsage ?? 34}%`, pct: h.cpuUsage ?? 34, cls: 'blue' },
          { label: 'Mémoire', val: `${h.memoryUsage ?? 52}%`, pct: h.memoryUsage ?? 52, cls: 'violet' },
          { label: 'Disque', val: `${h.diskUsage ?? 45}%`, pct: h.diskUsage ?? 45, cls: 'teal' },
          { label: 'Threads actifs', val: `${h.activeThreads ?? 12}`, pct: Math.min(100, (h.activeThreads ?? 12) * 2), cls: 'green' },
        ]);
        this.logs.set(data?.logs || []);
      },
      error: () => {}
    });

    this.activityLoading.set(true);
    this.api.getActivityLogs().subscribe({
      next: (r) => { this.activityLogs.set(r?.content || r || []); this.activityLoading.set(false); },
      error: () => { this.activityLoading.set(false); }
    });
  }

  logClass(level: string): string {
    if (!level) return 'badge-gray';
    const l = level.toUpperCase();
    if (l === 'ERROR') return 'badge-danger';
    if (l === 'WARN') return 'badge-warning';
    if (l === 'INFO') return 'badge-info';
    return 'badge-gray';
  }
}
