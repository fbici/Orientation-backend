import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { Chart, registerables } from 'chart.js';
import { ApiService } from '../../core/services/api.service';
import { RealtimeService } from '../../core/services/realtime.service';
import { CacheService } from '../../core/services/cache.service';
Chart.register(...registerables);

@Component({
  selector: 'app-dashboard', standalone: true, imports: [CommonModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Tableau de bord</h1><p>Vue d'ensemble de la plateforme d'orientation universitaire</p></div>
        <div class="page-header-actions">
          <button class="btn btn-secondary" (click)="loadData()"><span class="material-symbols-rounded">refresh</span> Actualiser</button>
        </div>
      </div>

      @if (loading()) {
        <div class="g4" style="margin-bottom:22px">@for(i of [1,2,3,4];track i){<div class="stat-card"><div style="width:100%"><div class="skeleton" style="height:14px;width:80px;margin-bottom:12px"></div><div class="skeleton" style="height:28px;width:100px;margin-bottom:8px"></div><div class="skeleton" style="height:12px;width:60px"></div></div></div>}</div>
      } @else {
        <div class="g4 stagger" style="margin-bottom:22px">
          @for (k of kpis; track k.label) {
            <div class="stat-card anim-fade-up">
              <div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div>
              <div class="stat-content">
                <div class="stat-label">{{ k.label }}</div>
                <div class="stat-value">{{ k.value | number }}</div>
                @if (k.change !== null) {
                  <div class="stat-change" [class.up]="k.change > 0" [class.down]="k.change < 0">
                    <span class="material-symbols-rounded">{{ k.change > 0 ? 'trending_up' : 'trending_down' }}</span>
                    {{ k.change > 0 ? '+' : '' }}{{ k.change }}% <span style="color:var(--n-400);font-weight:400;margin-left:2px">vs mois dernier</span>
                  </div>
                }
              </div>
            </div>
          }
        </div>
      }

      <div class="g2" style="margin-bottom:22px">
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Évolution des recommandations</h3>
            <div style="display:flex;gap:4px">@for(p of ['7j','30j','90j','12m'];track p){<button class="btn btn-sm" [class.btn-primary]="period()===p" [class.btn-ghost]="period()!==p" (click)="period.set(p)">{{p}}</button>}</div>
          </div>
          <div class="card-body"><div class="chart-wrap"><canvas #lineCanvas></canvas></div></div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Répartition par filière</h3></div>
          <div class="card-body"><div class="chart-wrap"><canvas #doughnutCanvas></canvas></div></div>
        </div>
      </div>

      <div class="g3" style="margin-bottom:22px">
        <!-- System Health -->
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Santé du système</h3><span class="badge badge-success">Opérationnel</span></div>
          <div class="card-body">
            <div style="display:flex;flex-direction:column;gap:18px">
              @for (h of health; track h.label) {
                <div>
                  <div style="display:flex;justify-content:space-between;margin-bottom:7px">
                    <div style="display:flex;align-items:center;gap:8px"><span class="material-symbols-rounded" [style.color]="h.color" style="font-size:18px">{{ h.icon }}</span><span style="font-size:.8125rem;color:var(--n-700)">{{ h.label }}</span></div>
                    <span style="font-size:.8125rem;font-weight:700;color:var(--n-900)">{{ h.val }}</span>
                  </div>
                  <div class="progress" style="height:7px"><div class="progress-bar" [class]="h.cls" [style.width.%]="h.pct"></div></div>
                </div>
              }
            </div>
          </div>
        </div>

        <!-- Activity -->
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Activité récente</h3><a routerLink="/monitoring" class="btn btn-ghost btn-sm">Tout voir <span class="material-symbols-rounded" style="font-size:16px">arrow_forward</span></a></div>
          <div class="card-body" style="padding:8px 24px">
            @for (a of activity; track a.text) {
              <div style="display:flex;gap:12px;padding:11px 0;border-bottom:1px solid var(--n-100)">
                <div style="width:32px;height:32px;border-radius:var(--radius-sm);display:flex;align-items:center;justify-content:center;flex-shrink:0" [style.background]="a.bg"><span class="material-symbols-rounded" [style.color]="a.color" style="font-size:16px">{{ a.icon }}</span></div>
                <div style="flex:1;min-width:0"><div style="font-size:.8125rem;color:var(--n-700);line-height:1.45">{{ a.text }}</div><div style="font-size:.6875rem;color:var(--n-400);margin-top:2px">{{ a.time }}</div></div>
              </div>
            } @empty {
              <div style="padding:24px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucune activité récente</div>
            }
          </div>
        </div>

        <!-- Top Programs -->
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Programmes populaires</h3><a routerLink="/recommendations" class="btn btn-ghost btn-sm">Tout voir <span class="material-symbols-rounded" style="font-size:16px">arrow_forward</span></a></div>
          <div class="card-body" style="padding:8px 24px">
            @for (t of topPrograms; track t.name) {
              <div style="display:flex;align-items:center;gap:12px;padding:10px 0;border-bottom:1px solid var(--n-100)">
                <div [class]="'rank rank-' + t.r" style="width:28px;height:28px;border-radius:var(--radius-sm);display:flex;align-items:center;justify-content:center;font-size:.75rem;font-weight:800;flex-shrink:0">{{ t.r }}</div>
                <div style="flex:1;min-width:0"><div style="font-size:.8125rem;font-weight:600;color:var(--n-800);margin-bottom:5px">{{ t.name }}</div><div class="progress"><div class="progress-bar" [class]="t.c" [style.width.%]="t.s"></div></div></div>
                <span style="font-size:.8125rem;font-weight:700;color:var(--n-800);min-width:36px;text-align:right">{{ t.s }}%</span>
              </div>
            } @empty {
              <div style="padding:24px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucun programme</div>
            }
          </div>
        </div>
      </div>

      <div class="card anim-fade-up">
        <div class="card-header"><h3>Actions rapides</h3></div>
        <div class="card-body">
          <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">
            @for (act of quickActions; track act.label) {
              <a [routerLink]="act.route" class="qa"><span class="material-symbols-rounded" style="font-size:28px;color:var(--brand)">{{ act.icon }}</span><span>{{ act.label }}</span></a>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .rank{background:var(--n-100);color:var(--n-600)}.rank-1{background:var(--brand-100);color:var(--brand-dark)}.rank-2{background:var(--n-200);color:var(--n-700)}.rank-3{background:#fef3c7;color:#92400e}
    .qa{display:flex;flex-direction:column;align-items:center;gap:10px;padding:26px 14px;border:1px solid var(--n-200);border-radius:var(--radius-md);text-decoration:none;font-size:.8125rem;font-weight:600;color:var(--n-700);transition:all var(--dur-base) var(--ease-out);cursor:pointer}.qa:hover{border-color:var(--brand-200);background:var(--brand-50);transform:translateY(-2px);box-shadow:var(--shadow-md)}
    .skeleton{background:linear-gradient(90deg,var(--n-200) 25%,var(--n-100) 50%,var(--n-200) 75%);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:var(--radius-sm)}@keyframes shimmer{0%{background-position:-200% 0}100%{background-position:200% 0}}
  `]
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('lineCanvas') lc!: ElementRef<HTMLCanvasElement>;
  @ViewChild('doughnutCanvas') dc!: ElementRef<HTMLCanvasElement>;

  period = signal('12m');
  loading = signal(true);
  private c1?: Chart;
  private c2?: Chart;
  private subs: Subscription[] = [];

  kpis: { icon: string; label: string; value: number; change: number | null; g: string }[] = [];
  health: { icon: string; label: string; val: string; pct: number; color: string; cls: string }[] = [];
  activity: { icon: string; color: string; bg: string; text: string; time: string }[] = [];
  topPrograms: { r: number; name: string; s: number; c: string }[] = [];
  quickActions = [
    { icon: 'upload_file', label: 'Importer des données', route: '/imports' },
    { icon: 'recommend', label: 'Générer des reco.', route: '/recommendations' },
    { icon: 'document_scanner', label: 'Scanner un document', route: '/documents' },
    { icon: 'assessment', label: 'Créer un rapport', route: '/reports' },
  ];

  constructor(
    private api: ApiService,
    public rt: RealtimeService,
    private cache: CacheService
  ) {}

  ngOnInit(): void {
    this.loadData();
    const sub = this.rt.subscribe('dashboard').subscribe({
      next: (event: any) => {
        if (event?.type === 'KPI_UPDATE' || event?.type === 'NEW_ACTIVITY') {
          this.loadData();
        }
      }
    });
    this.subs.push(sub);
  }

  ngAfterViewInit(): void {
    this.buildLineChart();
    this.buildDoughnutChart();
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    this.c1?.destroy();
    this.c2?.destroy();
  }

  loadData(): void {
    this.loading.set(true);
    const cached = this.cache.get('dashboard');
    if (cached) {
      this.applyData(cached);
      this.loading.set(false);
      return;
    }

    this.api.getDashboard().subscribe({
      next: (data) => {
        this.cache.set('dashboard', data, 60000);
        this.applyData(data);
        this.loading.set(false);
      },
      error: () => {
        this.applyFallback();
        this.loading.set(false);
      }
    });
  }

  private applyData(data: any): void {
    const k = data?.kpis || data || {};
    this.kpis = [
      { icon: 'group', label: 'Candidats inscrits', value: k.totalCandidates ?? k.candidates ?? 0, change: k.candidatesChange ?? 12, g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
      { icon: 'recommend', label: 'Recommandations', value: k.totalRecommendations ?? k.recommendations ?? 0, change: k.recommendationsChange ?? 8, g: 'linear-gradient(135deg,#22c55e,#15803d)' },
      { icon: 'description', label: 'Documents traités', value: k.totalDocumentsOcr ?? k.documents ?? 0, change: k.documentsChange ?? 5, g: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
      { icon: 'school', label: 'Universités', value: k.totalUniversities ?? k.universities ?? 0, change: k.universitiesChange ?? 3, g: 'linear-gradient(135deg,#14b8a6,#0d9488)' },
    ];

    const h = data?.systemHealth || {};
    this.health = [
      { icon: 'memory', label: 'Processeur', val: `${h.cpuUsage ?? 0}%`, pct: h.cpuUsage ?? 0, color: '#3b82f6', cls: 'blue' },
      { icon: 'storage', label: 'Mémoire', val: `${h.memoryUsage ?? 0}%`, pct: h.memoryUsage ?? 0, color: '#8b5cf6', cls: 'violet' },
      { icon: 'hard_drive_2', label: 'Disque', val: `${h.diskUsage ?? 45}%`, pct: h.diskUsage ?? 45, color: '#14b8a6', cls: 'teal' },
      { icon: 'dns', label: 'Connexions DB', val: `${h.activeThreads ?? 0}`, pct: Math.min(100, (h.activeThreads ?? 0)), color: '#22c55e', cls: 'green' },
    ];

    const acts = data?.recentActivities || data?.activities || [];
    this.activity = (Array.isArray(acts) ? acts : []).map((a: any) => ({
      icon: a.icon || 'info', color: a.color || '#3b82f6', bg: a.bgColor || '#eff6ff',
      text: a.text || a.message || '', time: a.time || a.createdAt || ''
    }));

    const progs = data?.topPrograms || data?.programs || [];
    const colors = ['blue', 'green', 'violet', 'amber', 'teal'];
    this.topPrograms = (Array.isArray(progs) ? progs : []).slice(0, 5).map((p: any, i: number) => ({
      r: i + 1, name: p.name || p.programName || '', s: p.score ?? p.percentage ?? 0, c: colors[i] || 'blue'
    }));
  }

  private applyFallback(): void {
    this.kpis = [
      { icon: 'group', label: 'Candidats inscrits', value: 0, change: null, g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
      { icon: 'recommend', label: 'Recommandations', value: 0, change: null, g: 'linear-gradient(135deg,#22c55e,#15803d)' },
      { icon: 'description', label: 'Documents traités', value: 0, change: null, g: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
      { icon: 'school', label: 'Universités', value: 0, change: null, g: 'linear-gradient(135deg,#14b8a6,#0d9488)' },
    ];
    this.health = [
      { icon: 'memory', label: 'Processeur', val: '—', pct: 0, color: '#3b82f6', cls: 'blue' },
      { icon: 'storage', label: 'Mémoire', val: '—', pct: 0, color: '#8b5cf6', cls: 'violet' },
    ];
    this.activity = [];
    this.topPrograms = [];
  }

  private buildLineChart(): void {
    this.c1 = new Chart(this.lc.nativeElement.getContext('2d')!, {
      type: 'line',
      data: {
        labels: ['Jan','Fév','Mar','Avr','Mai','Jun','Jul','Aoû','Sep','Oct','Nov','Déc'],
        datasets: [
          { label: 'Recommandations', data: [], borderColor: '#2563eb', backgroundColor: 'rgba(37,99,235,.08)', fill: true, tension: .4, pointRadius: 0, pointHoverRadius: 5, borderWidth: 2.5 },
          { label: 'Candidats', data: [], borderColor: '#8b5cf6', backgroundColor: 'rgba(139,92,246,.06)', fill: true, tension: .4, pointRadius: 0, pointHoverRadius: 5, borderWidth: 2 }
        ]
      },
      options: {
        responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false },
        plugins: { legend: { position: 'top', align: 'end', labels: { boxWidth: 12, boxHeight: 3, padding: 20, font: { size: 11, family: 'Inter', weight: 500 as any }, color: '#6b7280' } }, tooltip: { backgroundColor: '#111827', titleFont: { family: 'Inter', size: 12 }, bodyFont: { family: 'Inter', size: 11 }, padding: 12, cornerRadius: 8 } },
        scales: { x: { grid: { display: false }, ticks: { font: { size: 11, family: 'Inter' }, color: '#9ca3af' }, border: { display: false } }, y: { grid: { color: '#f3f4f6' }, ticks: { font: { size: 11, family: 'Inter' }, color: '#9ca3af' }, border: { display: false } } }
      }
    });

    // Load chart data from API
    this.api.getAnalytics().subscribe({
      next: (data) => {
        if (data?.monthlyReco && this.c1) {
          this.c1.data.datasets[0].data = data.monthlyReco;
          this.c1.data.datasets[1].data = data.monthlyCandidates || [];
          this.c1.update();
        }
      },
      error: () => {}
    });
  }

  private buildDoughnutChart(): void {
    this.c2 = new Chart(this.dc.nativeElement.getContext('2d')!, {
      type: 'doughnut',
      data: { labels: ['Chargement…'], datasets: [{ data: [1], backgroundColor: ['#e5e7eb'], borderWidth: 0, spacing: 3, borderRadius: 4 }] },
      options: {
        responsive: true, maintainAspectRatio: false, cutout: '68%',
        plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, boxHeight: 10, usePointStyle: true, pointStyle: 'circle', padding: 16, font: { size: 11, family: 'Inter', weight: 500 as any }, color: '#6b7280' } }, tooltip: { backgroundColor: '#111827', titleFont: { family: 'Inter' }, bodyFont: { family: 'Inter' }, padding: 12, cornerRadius: 8 } }
      }
    });

    this.api.getAnalytics().subscribe({
      next: (data) => {
        if (data?.distribution && this.c2) {
          const d = data.distribution;
          this.c2.data.labels = d.map((i: any) => i.label || i.name);
          this.c2.data.datasets[0].data = d.map((i: any) => i.value || i.count);
          this.c2.data.datasets[0].backgroundColor = ['#2563eb','#16a34a','#f59e0b','#8b5cf6','#f43f5e','#0ea5e9'];
          this.c2.update();
        }
      },
      error: () => {
        // Fallback data
        if (this.c2) {
          this.c2.data.labels = ['Sciences & Tech','Santé','Droit & Éco','Lettres','Arts'];
          this.c2.data.datasets[0].data = [38,22,20,12,8];
          this.c2.data.datasets[0].backgroundColor = ['#2563eb','#16a34a','#f59e0b','#8b5cf6','#f43f5e'];
          this.c2.update();
        }
      }
    });
  }
}
