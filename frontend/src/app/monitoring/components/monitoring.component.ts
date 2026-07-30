import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-monitoring',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Monitoring</h1><p>Surveillance en temps réel de l'infrastructure</p></div>
        <div class="page-header-actions">
          <span class="badge badge-success" style="display:flex;align-items:center;gap:6px;padding:5px 14px">
            <span class="dot green" style="animation:pulse 2s infinite"></span> Tous les services opérationnels
          </span>
        </div>
      </div>
      <div class="g4 stagger" style="margin-bottom:22px">
        @for (s of services; track s.name) {
          <div class="card anim-fade-up">
            <div class="card-body" style="display:flex;align-items:center;gap:14px;padding:18px 20px">
              <div style="width:40px;height:40px;border-radius:var(--radius-sm);display:flex;align-items:center;justify-content:center" [style.background]="s.bg">
                <span class="material-symbols-rounded" [style.color]="s.color" style="font-size:20px">{{ s.icon }}</span>
              </div>
              <div style="flex:1"><div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">{{ s.name }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ s.detail }}</div></div>
              <span class="badge" [class]="s.ok ? 'badge-success' : 'badge-danger'">{{ s.ok ? 'OK' : 'Erreur' }}</span>
            </div>
          </div>
        }
      </div>
      <div class="g2">
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Métriques serveur</h3></div>
          <div class="card-body">
            @for (m of serverMetrics; track m.label) {
              <div style="margin-bottom:18px">
                <div style="display:flex;justify-content:space-between;margin-bottom:7px"><span style="font-size:.8125rem;color:var(--n-600)">{{ m.label }}</span><span style="font-size:.8125rem;font-weight:700;color:var(--n-800)">{{ m.val }}</span></div>
                <div class="progress" style="height:7px"><div class="progress-bar" [class]="m.cls" [style.width.%]="m.pct"></div></div>
              </div>
            }
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Journaux récents</h3></div>
          <div class="card-body" style="padding:0">
            <table class="data-table">
              <thead><tr><th>Niveau</th><th>Message</th><th>Heure</th></tr></thead>
              <tbody>
                @for (l of logs; track l.t) {
                  <tr>
                    <td><span class="badge" [class]="l.cls">{{ l.level }}</span></td>
                    <td style="font-size:.8125rem">{{ l.msg }}</td>
                    <td style="font-size:.75rem;color:var(--n-500);white-space:nowrap">{{ l.t }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class MonitoringComponent {
  services = [
    { name: 'API Backend', icon: 'api', detail: 'Spring Boot 3.4 · Port 8080', ok: true, bg: '#eff6ff', color: '#3b82f6' },
    { name: 'PostgreSQL', icon: 'storage', detail: 'v16 · Port 5432', ok: true, bg: '#f0fdf4', color: '#16a34a' },
    { name: 'Redis Cache', icon: 'bolt', detail: 'v7 · Port 6379', ok: true, bg: '#fff7ed', color: '#ea580c' },
    { name: 'WebSocket', icon: 'sync', detail: 'Temps réel actif', ok: true, bg: '#f5f3ff', color: '#7c3aed' },
  ];
  serverMetrics = [
    { label: 'Utilisation CPU', val: '34%', pct: 34, cls: 'blue' },
    { label: 'Mémoire utilisée', val: '4.2 / 8 Go', pct: 52, cls: 'violet' },
    { label: 'Espace disque', val: '45 / 100 Go', pct: 45, cls: 'teal' },
    { label: 'Connexions DB', val: '12 / 100', pct: 12, cls: 'green' },
    { label: 'Requêtes / sec', val: '128', pct: 25, cls: 'amber' },
  ];
  logs = [
    { level: 'INFO', cls: 'badge-info', msg: 'Import batch terminé — 124 universités importées', t: '14:32:05' },
    { level: 'INFO', cls: 'badge-info', msg: 'Recommandation générée pour candidat #2847', t: '14:30:12' },
    { level: 'WARN', cls: 'badge-warning', msg: 'Temps de réponse élevé sur /recommendations/generate', t: '14:28:44' },
    { level: 'INFO', cls: 'badge-info', msg: 'Refresh token rotation — admin@orientation.com', t: '14:25:01' },
    { level: 'ERROR', cls: 'badge-danger', msg: 'Échec parsing ligne 45 — CSV malformé', t: '14:20:33' },
    { level: 'INFO', cls: 'badge-info', msg: 'Health check — tous les services OK', t: '14:15:00' },
  ];
}
