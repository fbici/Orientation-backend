import { Component, OnInit, signal } from '@angular/core';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-analytics',
  standalone: true,
  template: `
    <div class="analytics">
      <h1>Analytics</h1>

      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-value">{{ kpis()?.acceptanceRate || 0 }}%</div>
          <div class="kpi-label">Taux d'Admission</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-value">{{ kpis()?.recommendationSuccessRate || 0 }}%</div>
          <div class="kpi-label">Taux de Succès</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-value">{{ kpis()?.averageScore || 0 }}</div>
          <div class="kpi-label">Score Moyen</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-value">{{ kpis()?.ocrAccuracy || 0 }}%</div>
          <div class="kpi-label">Précision OCR</div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="chart-card">
          <h3>Top Universités</h3>
          @if (topUniversities()) {
            <div class="top-list">
              @for (uni of topUniversities(); track uni.name) {
                <div class="top-item">
                  <span class="top-name">{{ uni.name }}</span>
                  <span class="top-score">{{ uni.score }}%</span>
                </div>
              }
            </div>
          }
        </div>

        <div class="chart-card">
          <h3>Top Programmes</h3>
          @if (topPrograms()) {
            <div class="top-list">
              @for (prog of topPrograms(); track prog.name) {
                <div class="top-item">
                  <span class="top-name">{{ prog.name }}</span>
                  <span class="top-score">{{ prog.count }}</span>
                </div>
              }
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .analytics { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
    .kpi-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); text-align: center; }
    .kpi-value { font-size: 2.5rem; font-weight: bold; color: #1a1a2e; }
    .kpi-label { color: #666; margin-top: 5px; }
    .charts-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 20px; }
    .chart-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    .chart-card h3 { margin-top: 0; color: #1a1a2e; }
    .top-list { display: flex; flex-direction: column; gap: 10px; }
    .top-item { display: flex; justify-content: space-between; padding: 10px; background: #f8f9fa; border-radius: 4px; }
    .top-score { font-weight: bold; color: #1a1a2e; }
  `]
})
export class AnalyticsComponent implements OnInit {
  kpis = signal<any>(null);
  topUniversities = signal<any[]>([]);
  topPrograms = signal<any[]>([]);

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getAnalytics().subscribe({
      next: (data) => {
        this.kpis.set(data.kpis);
        this.topUniversities.set(data.topUniversities?.items || []);
        this.topPrograms.set(data.topPrograms?.items || []);
      }
    });
  }
}
