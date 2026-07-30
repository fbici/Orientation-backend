import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Analytique</h1><p>Statistiques détaillées et tendances de la plateforme</p></div>
        <div class="page-header-actions">
          <button class="btn btn-secondary"><span class="material-symbols-rounded">calendar_month</span>Période</button>
          <button class="btn btn-secondary"><span class="material-symbols-rounded">download</span>Exporter</button>
        </div>
      </div>
      <div class="g4 stagger" style="margin-bottom:22px">
        @for (m of metrics; track m.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="m.grad"><span class="material-symbols-rounded filled">{{ m.icon }}</span></div>
            <div class="stat-content">
              <div class="stat-label">{{ m.label }}</div>
              <div class="stat-value">{{ m.value }}</div>
              <div class="stat-change" [class.up]="m.trend>0" [class.down]="m.trend<0">
                <span class="material-symbols-rounded">{{ m.trend>0 ? 'trending_up' : 'trending_down' }}</span>
                {{ m.trend>0?'+':'' }}{{ m.trend }}%
              </div>
            </div>
          </div>
        }
      </div>
      <div class="g2" style="margin-bottom:22px">
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Taux de conversion par filière</h3></div>
          <div class="card-body">
            @for (c of conversion; track c.name) {
              <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px">
                <span style="min-width:130px;font-size:.8125rem;font-weight:500;color:var(--n-700)">{{ c.name }}</span>
                <div class="progress" style="flex:1;height:8px"><div class="progress-bar" [class]="c.cls" [style.width.%]="c.val"></div></div>
                <span style="font-size:.8125rem;font-weight:700;color:var(--n-900);min-width:40px;text-align:right">{{ c.val }}%</span>
              </div>
            }
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Répartition géographique</h3></div>
          <div class="card-body" style="padding:8px 24px">
            @for (g of geo; track g.name) {
              <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--n-100)">
                <div style="display:flex;align-items:center;gap:10px">
                  <span class="material-symbols-rounded" style="font-size:18px;color:var(--n-400)">location_on</span>
                  <span style="font-size:.8125rem;color:var(--n-700)">{{ g.name }}</span>
                </div>
                <div style="display:flex;align-items:center;gap:12px">
                  <span style="font-size:.8125rem;font-weight:700;color:var(--n-800)">{{ g.count | number }}</span>
                  <span class="badge badge-primary">{{ g.pct }}%</span>
                </div>
              </div>
            }
          </div>
        </div>
      </div>
      <div class="card anim-fade-up">
        <div class="card-header"><h3>Top universités par score moyen</h3></div>
        <div class="card-body" style="padding:8px 24px">
          @for (u of topUni; track u.name) {
            <div style="display:flex;align-items:center;gap:12px;padding:10px 0;border-bottom:1px solid var(--n-100)">
              <span style="font-weight:800;color:var(--n-400);font-size:.75rem;min-width:20px">#{{ u.r }}</span>
              <div style="flex:1"><div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">{{ u.name }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ u.prog }} programmes</div></div>
              <span style="font-weight:700;font-size:.8125rem;color:var(--brand)">{{ u.score }}%</span>
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class AnalyticsComponent {
  metrics = [
    { icon: 'group', label: 'Candidats actifs', value: '2 847', trend: 12, grad: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'recommend', label: 'Recommandations', value: '18 432', trend: 8, grad: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'school', label: 'Programmes', value: '486', trend: 3, grad: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
    { icon: 'verified', label: 'Satisfaction', value: '94%', trend: 2, grad: 'linear-gradient(135deg,#14b8a6,#0d9488)' },
  ];
  conversion = [
    { name: 'Génie Informatique', val: 92, cls: 'blue' },
    { name: 'Médecine', val: 87, cls: 'green' },
    { name: 'Droit', val: 78, cls: 'violet' },
    { name: 'Génie Civil', val: 74, cls: 'amber' },
    { name: 'Pharmacie', val: 71, cls: 'teal' },
    { name: 'Économie', val: 65, cls: 'red' },
  ];
  geo = [
    { name: 'Cotonou', count: 1240, pct: 43 },
    { name: 'Abomey-Calavi', count: 856, pct: 30 },
    { name: 'Porto-Novo', count: 412, pct: 14 },
    { name: 'Parakou', count: 213, pct: 7 },
    { name: 'Autres', count: 126, pct: 6 },
  ];
  topUni = [
    { r: 1, name: "Université d'Abomey-Calavi", prog: 124, score: 88 },
    { r: 2, name: 'Université de Parakou', prog: 67, score: 82 },
    { r: 3, name: 'UATM Lokossa', prog: 45, score: 79 },
    { r: 4, name: 'UUP Natitingou', prog: 38, score: 76 },
    { r: 5, name: 'EPAC Porto-Novo', prog: 28, score: 72 },
  ];
}
