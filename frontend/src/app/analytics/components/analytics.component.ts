import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Analytique</h1><p>Statistiques détaillées et tendances de la plateforme</p></div>
        <div class="page-header-actions">
          <button class="btn btn-secondary" (click)="load()"><span class="material-symbols-rounded">refresh</span></button>
        </div>
      </div>

      <div class="g4 stagger" style="margin-bottom:22px">
        @for (m of metrics(); track m.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="m.grad"><span class="material-symbols-rounded filled">{{ m.icon }}</span></div>
            <div class="stat-content"><div class="stat-label">{{ m.label }}</div><div class="stat-value">{{ m.value }}</div></div>
          </div>
        }
      </div>

      <div class="g2" style="margin-bottom:22px">
        <div class="card">
          <div class="card-header"><h3>Taux de conversion par filière</h3></div>
          <div class="card-body">
            @for (c of conversion(); track c.name) {
              <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px">
                <span style="min-width:130px;font-size:.8125rem;font-weight:500;color:var(--n-700)">{{ c.name }}</span>
                <div class="progress" style="flex:1;height:8px"><div class="progress-bar" [class]="c.cls" [style.width.%]="c.val"></div></div>
                <span style="font-size:.8125rem;font-weight:700;color:var(--n-900);min-width:40px;text-align:right">{{ c.val }}%</span>
              </div>
            } @empty {
              <div style="padding:24px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucune donnée de conversion</div>
            }
          </div>
        </div>

        <div class="card">
          <div class="card-header"><h3>Répartition géographique</h3></div>
          <div class="card-body" style="padding:8px 24px">
            @for (g of geo(); track g.name) {
              <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--n-100)">
                <div style="display:flex;align-items:center;gap:10px"><span class="material-symbols-rounded" style="font-size:18px;color:var(--n-400)">location_on</span><span style="font-size:.8125rem;color:var(--n-700)">{{ g.name }}</span></div>
                <div style="display:flex;align-items:center;gap:12px"><span style="font-size:.8125rem;font-weight:700;color:var(--n-800)">{{ g.count | number }}</span><span class="badge badge-primary">{{ g.pct }}%</span></div>
              </div>
            } @empty {
              <div style="padding:24px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucune donnée géographique</div>
            }
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><h3>Top universités par score moyen</h3></div>
        <div class="card-body" style="padding:8px 24px">
          @for (u of topUni(); track u.name) {
            <div style="display:flex;align-items:center;gap:12px;padding:10px 0;border-bottom:1px solid var(--n-100)">
              <span style="font-weight:800;color:var(--n-400);font-size:.75rem;min-width:20px">#{{ u.r }}</span>
              <div style="flex:1"><div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">{{ u.name }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ u.prog }} programmes</div></div>
              <span style="font-weight:700;font-size:.8125rem;color:var(--brand)">{{ u.score }}%</span>
            </div>
          } @empty {
            <div style="padding:24px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucune donnée</div>
          }
        </div>
      </div>
    </div>
  `
})
export class AnalyticsComponent implements OnInit {
  metrics = signal<any[]>([]);
  conversion = signal<any[]>([]);
  geo = signal<any[]>([]);
  topUni = signal<any[]>([]);

  constructor(private api: ApiService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.getAnalytics().subscribe({
      next: (data) => {
        this.metrics.set([
          { icon: 'group', label: 'Candidats actifs', value: data?.totalCandidates ?? 0, grad: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
          { icon: 'recommend', label: 'Recommandations', value: data?.totalRecommendations ?? 0, grad: 'linear-gradient(135deg,#22c55e,#15803d)' },
          { icon: 'school', label: 'Programmes', value: data?.totalPrograms ?? 0, grad: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
          { icon: 'verified', label: 'Satisfaction', value: `${data?.satisfaction ?? 0}%`, grad: 'linear-gradient(135deg,#14b8a6,#0d9488)' },
        ]);
        this.conversion.set(data?.conversion || []);
        this.geo.set(data?.geographic || []);
        this.topUni.set(data?.topUniversities || []);
      },
      error: () => {}
    });
  }
}
