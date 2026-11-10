import {PaginationComponent} from '../../shared/components/pagination.component';
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-monitoring',
  standalone: true,
  imports: [PaginationComponent,CommonModule],
  template: `
    <div class="anim-fade-up">@if(error()){<div class="api-error" role="alert">{{error()}}</div>}<p class="api-notice">Seules les mesures fournies par le serveur sont affichees. Les metriques non instrumentees ne sont pas estimees.</p>
      <div class="page-header">
        <div><h1>Monitoring</h1><p>Surveillance en temps réel de l'infrastructure</p></div>
        <div class="page-header-actions">
          <span class="badge badge-success" style="display:flex;align-items:center;gap:6px;padding:5px 14px"><span class="dot green" style="animation:pulse 2s infinite"></span> Système opérationnel</span>
          <button class="btn btn-secondary" (click)="load()" aria-label="Actualiser"><span class="material-symbols-rounded">refresh</span></button>
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
          <div class="card-header"><h3>Journaux récents</h3><button class="btn btn-secondary btn-sm" (click)="load()" aria-label="Actualiser"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button></div>
          <div class="card-body" style="padding:0;max-height:400px;overflow-y:auto">
            @for (l of logs(); track l.timestamp) {
              <div style="display:flex;gap:12px;padding:10px 20px;border-bottom:1px solid var(--n-100)">
                <span class="badge" [class]="logClass(l.level)" style="flex-shrink:0">{{ l.level }}</span>
                <div style="flex:1;min-width:0"><div style="font-size:.8125rem;color:var(--n-700)">{{ l.message }}</div></div>
                <span style="font-size:.6875rem;color:var(--n-400);white-space:nowrap;flex-shrink:0">{{ l.timestamp | date:'short' }}</span>
              </div>
            } @empty {
              <div style="padding:32px;text-align:center;color:var(--n-400);font-size:.8125rem">Les journaux applicatifs ne sont pas exposes par cette API. Consultez le journal d audit ci-dessous.</div>
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
    <app-pagination [page]="page" [total]="total()" [loading]="activityLoading()" (pageChange)="page=$event;load()" /></div>
  `,
  styles: [`.spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}@keyframes spin{to{transform:rotate(360deg)}}`]
})
export class MonitoringComponent implements OnInit {
  error = signal('');
  services = signal<any[]>([]);
  metrics = signal<any[]>([]);
  logs = signal<any[]>([]);
  activityLogs = signal<any[]>([]);
  activityLoading = signal(false);
  page=0;total=signal(0);

  constructor(private api: ApiService) {}
  ngOnInit(): void { this.load(); }

  load(): void { this.error.set('');
    this.api.getMonitoring().subscribe({
      next: (data) => {
        this.services.set((data?.healthChecks || []).map((h:any)=>({name:h.name,detail:h.message,ok:h.status==='UP',icon:'monitor_heart',bg:'var(--n-50)',color:'var(--brand)'})));
        const h=data?.systemMetrics || {};
        this.metrics.set([
          {label:'CPU du processus',val:h.cpuUsage==null?'Non mesure':`${h.cpuUsage.toFixed(1)}%`,pct:h.cpuUsage??0,cls:'blue'},
          {label:'Memoire JVM',val:h.memoryUsage==null?'Non mesure':`${h.memoryUsage.toFixed(1)}%`,pct:h.memoryUsage??0,cls:'violet'},
          {label:'Threads actifs',val:h.activeThreads??'Non mesure',pct:0,cls:'green'}
        ]);
        this.logs.set(data?.logs || []);
      },
      error: () => this.error.set('Mesures systeme indisponibles ou acces refuse.')
    });

    this.activityLoading.set(true);
    this.api.getActivityLogs(this.page,20).subscribe({
      next: (r) => { this.activityLogs.set((r.events || []).map((e:any)=>({...e,createdAt:e.timestamp,detail:e.details,userName:e.userId})));this.total.set(r.totalEvents ?? 0); this.activityLoading.set(false); },
      error: () => { this.activityLoading.set(false); this.error.set('Journal des activites indisponible.'); }
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
