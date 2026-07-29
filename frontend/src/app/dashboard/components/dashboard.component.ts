import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { RealtimeService, RealtimeEvent } from '../../core/services/realtime.service';
import { ChartService, ChartConfig } from '../../core/services/chart.service';
import { CacheService } from '../../core/services/cache.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  template: `
    <div class="dashboard">
      <div class="dashboard-header">
        <h1>Dashboard Exécutif</h1>
        <div class="header-actions">
          <span class="realtime-indicator" [class.active]="realtimeService.connectionStatus()">
            ● Temps Réel
          </span>
          <button (click)="refresh()" class="refresh-btn">↻ Actualiser</button>
        </div>
      </div>

      <!-- KPI Cards -->
      <div class="kpi-grid">
        @for (kpi of kpiCards(); track kpi.label) {
          <div class="kpi-card" [class]="kpi.color">
            <div class="kpi-icon">{{ kpi.icon }}</div>
            <div class="kpi-content">
              <div class="kpi-value">{{ kpi.value }}</div>
              <div class="kpi-label">{{ kpi.label }}</div>
              <div class="kpi-change" [class.positive]="kpi.change > 0" [class.negative]="kpi.change < 0">
                {{ kpi.change > 0 ? '+' : '' }}{{ kpi.change }}% aujourd'hui
              </div>
            </div>
          </div>
        }
      </div>

      <!-- Charts Row -->
      <div class="charts-row">
        <div class="chart-card wide">
          <h3>Évolution des Recommandations</h3>
          <div class="chart-container">
            <canvas id="recommendationsChart"></canvas>
          </div>
        </div>
        <div class="chart-card">
          <h3>Répartition par Type</h3>
          <div class="chart-container">
            <canvas id="typeChart"></canvas>
          </div>
        </div>
      </div>

      <!-- System Health & Activity -->
      <div class="bottom-row">
        <div class="chart-card">
          <h3>Santé du Système</h3>
          @if (systemHealth()) {
            <div class="health-grid">
              <div class="health-item">
                <span class="health-label">CPU</span>
                <div class="progress-bar">
                  <div class="progress-fill" [style.width.%]="systemHealth()?.cpuUsage"></div>
                </div>
                <span class="health-value">{{ systemHealth()?.cpuUsage | number:'1.0-0' }}%</span>
              </div>
              <div class="health-item">
                <span class="health-label">Mémoire</span>
                <div class="progress-bar">
                  <div class="progress-fill" [style.width.%]="systemHealth()?.memoryUsage"></div>
                </div>
                <span class="health-value">{{ systemHealth()?.memoryUsage | number:'1.0-0' }}%</span>
              </div>
              <div class="health-item">
                <span class="health-label">Threads</span>
                <span class="health-value">{{ systemHealth()?.activeThreads }}</span>
              </div>
              <div class="health-item">
                <span class="health-label">DB</span>
                <span class="health-badge up">UP</span>
              </div>
            </div>
          }
        </div>

        <div class="chart-card">
          <h3>Activité Récente</h3>
          <div class="activity-timeline">
            @for (activity of recentActivities(); track activity.time) {
              <div class="timeline-item">
                <div class="timeline-dot" [class]="activity.type"></div>
                <div class="timeline-content">
                  <div class="timeline-text">{{ activity.text }}</div>
                  <div class="timeline-time">{{ activity.time }}</div>
                </div>
              </div>
            }
          </div>
        </div>

        <div class="chart-card">
          <h3>Top Universités</h3>
          <div class="top-list">
            @for (uni of topUniversities(); track uni.name) {
              <div class="top-item">
                <span class="top-rank">#{{ uni.rank }}</span>
                <span class="top-name">{{ uni.name }}</span>
                <div class="top-bar">
                  <div class="top-bar-fill" [style.width.%]="uni.score"></div>
                </div>
                <span class="top-score">{{ uni.score }}%</span>
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { max-width: 1600px; }
    .dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
    .dashboard-header h1 { color: #1a1a2e; margin: 0; }
    .header-actions { display: flex; align-items: center; gap: 15px; }
    .realtime-indicator { color: #27ae60; font-size: 0.9rem; }
    .realtime-indicator.active { animation: pulse 2s infinite; }
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
    .refresh-btn { background: #1a1a2e; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 20px; margin-bottom: 30px; }
    .kpi-card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); display: flex; gap: 15px; align-items: center; transition: transform 0.2s; }
    .kpi-card:hover { transform: translateY(-2px); }
    .kpi-icon { font-size: 2.5rem; }
    .kpi-value { font-size: 2rem; font-weight: bold; color: #1a1a2e; }
    .kpi-label { color: #666; font-size: 0.9rem; }
    .kpi-change { font-size: 0.8rem; margin-top: 4px; }
    .kpi-change.positive { color: #27ae60; }
    .kpi-change.negative { color: #e74c3c; }
    .charts-row { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 20px; }
    .chart-card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
    .chart-card h3 { margin: 0 0 20px; color: #1a1a2e; font-size: 1.1rem; }
    .chart-container { height: 300px; position: relative; }
    .bottom-row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; }
    .health-grid { display: flex; flex-direction: column; gap: 15px; }
    .health-item { display: flex; align-items: center; gap: 10px; }
    .health-label { min-width: 80px; color: #666; }
    .health-value { font-weight: bold; min-width: 50px; text-align: right; }
    .health-badge { padding: 4px 12px; border-radius: 12px; font-size: 0.8rem; font-weight: bold; }
    .health-badge.up { background: #d4edda; color: #155724; }
    .progress-bar { flex: 1; height: 8px; background: #e9ecef; border-radius: 4px; overflow: hidden; }
    .progress-fill { height: 100%; background: linear-gradient(90deg, #1a1a2e, #16213e); border-radius: 4px; transition: width 0.5s; }
    .activity-timeline { display: flex; flex-direction: column; gap: 12px; }
    .timeline-item { display: flex; gap: 12px; align-items: flex-start; }
    .timeline-dot { width: 10px; height: 10px; border-radius: 50%; margin-top: 4px; flex-shrink: 0; }
    .timeline-dot.import { background: #3498db; }
    .timeline-dot.recommendation { background: #27ae60; }
    .timeline-dot.document { background: #e74c3c; }
    .timeline-dot.user { background: #9b59b6; }
    .timeline-text { font-size: 0.9rem; color: #333; }
    .timeline-time { font-size: 0.8rem; color: #999; }
    .top-list { display: flex; flex-direction: column; gap: 10px; }
    .top-item { display: flex; align-items: center; gap: 10px; }
    .top-rank { font-weight: bold; color: #1a1a2e; min-width: 30px; }
    .top-name { flex: 1; font-size: 0.9rem; }
    .top-bar { width: 100px; height: 6px; background: #e9ecef; border-radius: 3px; overflow: hidden; }
    .top-bar-fill { height: 100%; background: #1a1a2e; border-radius: 3px; }
    .top-score { font-weight: bold; color: #1a1a2e; min-width: 40px; text-align: right; }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {

  kpiCards = signal<any[]>([]);
  systemHealth = signal<any>(null);
  recentActivities = signal<any[]>([]);
  topUniversities = signal<any[]>([]);

  private subscriptions: Subscription[] = [];

  constructor(
    private api: ApiService,
    public realtimeService: RealtimeService,
    private chartService: ChartService,
    private cacheService: CacheService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.setupRealtimeUpdates();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private loadDashboard(): void {
    const cached = this.cacheService.get('dashboard');
    if (cached) {
      this.updateDashboard(cached);
      return;
    }

    this.api.getDashboard().subscribe({
      next: (data) => {
        this.cacheService.set('dashboard', data, 60000);
        this.updateDashboard(data);
      }
    });
  }

  private updateDashboard(data: any): void {
    const kpis = data?.kpis;
    this.kpiCards.set([
      { icon: '👥', label: 'Candidats', value: kpis?.totalCandidates || 0, change: 12, color: 'blue' },
      { icon: '🎯', label: 'Recommandations', value: kpis?.totalRecommendations || 0, change: 8, color: 'green' },
      { icon: '📄', label: 'Documents OCR', value: kpis?.totalDocumentsOcr || 0, change: 5, color: 'purple' },
      { icon: '⚡', label: 'Règles Actives', value: kpis?.activeRules || 0, change: 3, color: 'orange' },
      { icon: '🏠', label: 'Tenants', value: kpis?.totalTenants || 0, change: 0, color: 'teal' },
      { icon: '👤', label: 'Utilisateurs', value: kpis?.activeUsers || 0, change: 15, color: 'red' }
    ]);

    this.systemHealth.set(data?.systemHealth);

    this.recentActivities.set([
      { type: 'import', text: 'Import universities.csv terminé', time: 'Il y a 5 min' },
      { type: 'recommendation', text: '12 nouvelles recommandations générées', time: 'Il y a 12 min' },
      { type: 'document', text: 'Guide Orientation 2025 uploadé', time: 'Il y a 30 min' },
      { type: 'user', text: 'Nouvel utilisateur inscrit', time: 'Il y a 1h' }
    ]);

    this.topUniversities.set([
      { name: 'Université Mohammed V', score: 85, rank: 1 },
      { name: 'Université Hassan II', score: 82, rank: 2 },
      { name: 'Université Cadi Ayyad', score: 78, rank: 3 },
      { name: 'Université Sidi Mohamed', score: 75, rank: 4 }
    ]);
  }

  private setupRealtimeUpdates(): void {
    const sub = this.realtimeService.subscribe('dashboard').subscribe({
      next: (event) => this.handleRealtimeEvent(event)
    });
    this.subscriptions.push(sub);
  }

  private handleRealtimeEvent(event: RealtimeEvent): void {
    switch (event.type) {
      case 'KPI_UPDATE':
        this.loadDashboard();
        break;
      case 'NEW_ACTIVITY':
        this.recentActivities.update(activities => [event.data, ...activities.slice(0, 9)]);
        break;
      case 'HEALTH_UPDATE':
        this.systemHealth.set(event.data);
        break;
    }
  }

  refresh(): void {
    this.cacheService.delete('dashboard');
    this.loadDashboard();
  }
}
