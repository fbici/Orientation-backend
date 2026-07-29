import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { RealtimeService, RealtimeEvent } from '../../core/services/realtime.service';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-monitoring',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="monitoring">
      <div class="monitoring-header">
        <h1>Monitoring Système</h1>
        <div class="header-actions">
          <span class="realtime-indicator" [class.active]="realtimeService.connectionStatus()">
            ● Temps Réel
          </span>
          <button (click)="refresh()">↻ Actualiser</button>
        </div>
      </div>

      @if (data()) {
        <!-- System Metrics -->
        <div class="metrics-grid">
          <div class="metric-card">
            <h3>💻 Système</h3>
            <div class="gauge-container">
              <div class="gauge" [style.--value]="data()?.systemMetrics?.cpuUsage || 0">
                <span class="gauge-value">{{ data()?.systemMetrics?.cpuUsage | number:'1.0-0' }}%</span>
              </div>
              <span class="gauge-label">CPU</span>
            </div>
            <div class="gauge-container">
              <div class="gauge" [style.--value]="data()?.systemMetrics?.memoryUsage || 0">
                <span class="gauge-value">{{ data()?.systemMetrics?.memoryUsage | number:'1.0-0' }}%</span>
              </div>
              <span class="gauge-label">RAM</span>
            </div>
          </div>

          <div class="metric-card">
            <h3>📊 JVM</h3>
            <div class="jvm-stats">
              <div class="stat-row">
                <span>Heap Used</span>
                <span>{{ formatBytes(data()?.systemMetrics?.heapUsed) }}</span>
              </div>
              <div class="stat-row">
                <span>Heap Max</span>
                <span>{{ formatBytes(data()?.systemMetrics?.heapMax) }}</span>
              </div>
              <div class="stat-row">
                <span>Non-Heap</span>
                <span>{{ formatBytes(data()?.systemMetrics?.nonHeapUsed) }}</span>
              </div>
              <div class="stat-row">
                <span>Uptime</span>
                <span>{{ formatDuration(data()?.systemMetrics?.uptimeMs) }}</span>
              </div>
            </div>
          </div>

          <div class="metric-card">
            <h3>⚡ Application</h3>
            <div class="jvm-stats">
              <div class="stat-row">
                <span>API Calls</span>
                <span>{{ data()?.applicationMetrics?.totalApiCalls }}</span>
              </div>
              <div class="stat-row">
                <span>Response Time</span>
                <span>{{ data()?.applicationMetrics?.averageResponseTime | number:'1.0-0' }}ms</span>
              </div>
              <div class="stat-row">
                <span>Errors</span>
                <span class="error-count">{{ data()?.applicationMetrics?.errorCount }}</span>
              </div>
              <div class="stat-row">
                <span>Sessions</span>
                <span>{{ data()?.applicationMetrics?.activeSessions }}</span>
              </div>
            </div>
          </div>

          <div class="metric-card">
            <h3>🔗 Health Checks</h3>
            <div class="health-checks">
              @for (check of data()?.healthChecks || []; track check.name) {
                <div class="health-check-item">
                  <span class="health-dot" [class.up]="check.status === 'UP'" [class.down]="check.status !== 'UP'"></span>
                  <span class="health-name">{{ check.name }}</span>
                  <span class="health-status" [class.up]="check.status === 'UP'">{{ check.status }}</span>
                  <span class="health-time">{{ check.responseTimeMs }}ms</span>
                </div>
              }
            </div>
          </div>
        </div>

        <!-- Threads -->
        <div class="threads-card">
          <h3>🧵 Threads</h3>
          <div class="threads-bar">
            <div class="thread-info">
              <span>Actifs: {{ data()?.systemMetrics?.activeThreads }}</span>
              <span>Daemon: {{ data()?.systemMetrics?.daemonThreads }}</span>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .monitoring { max-width: 1600px; }
    .monitoring-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
    .monitoring-header h1 { color: #1a1a2e; margin: 0; }
    .header-actions { display: flex; align-items: center; gap: 15px; }
    .realtime-indicator { color: #27ae60; font-size: 0.9rem; }
    .realtime-indicator.active { animation: pulse 2s infinite; }
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
    button { background: #1a1a2e; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 20px; }
    .metric-card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
    .metric-card h3 { margin: 0 0 20px; color: #1a1a2e; font-size: 1.1rem; }
    .gauge-container { display: flex; flex-direction: column; align-items: center; margin: 15px 0; }
    .gauge { width: 120px; height: 120px; border-radius: 50%; background: conic-gradient(#1a1a2e calc(var(--value) * 1%), #e9ecef 0); display: flex; align-items: center; justify-content: center; position: relative; }
    .gauge::before { content: ''; width: 90px; height: 90px; background: white; border-radius: 50%; position: absolute; }
    .gauge-value { position: relative; z-index: 1; font-weight: bold; font-size: 1.2rem; }
    .gauge-label { margin-top: 8px; color: #666; font-size: 0.9rem; }
    .jvm-stats { display: flex; flex-direction: column; gap: 12px; }
    .stat-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
    .stat-row:last-child { border-bottom: none; }
    .error-count { color: #e74c3c; font-weight: bold; }
    .health-checks { display: flex; flex-direction: column; gap: 10px; }
    .health-check-item { display: flex; align-items: center; gap: 10px; padding: 8px; background: #f8f9fa; border-radius: 6px; }
    .health-dot { width: 8px; height: 8px; border-radius: 50%; }
    .health-dot.up { background: #27ae60; }
    .health-dot.down { background: #e74c3c; }
    .health-name { flex: 1; }
    .health-status { font-weight: bold; color: #27ae60; }
    .health-time { color: #999; font-size: 0.85rem; }
    .threads-card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
    .threads-card h3 { margin: 0 0 15px; color: #1a1a2e; }
    .threads-bar { display: flex; justify-content: space-around; padding: 15px; background: #f8f9fa; border-radius: 8px; }
    .thread-info { display: flex; gap: 30px; }
  `]
})
export class MonitoringComponent implements OnInit, OnDestroy {

  data = signal<any>(null);
  private subscriptions: Subscription[] = [];

  constructor(private api: ApiService, public realtimeService: RealtimeService) {}

  ngOnInit(): void {
    this.loadMonitoring();
    // Auto-refresh every 30 seconds
    this.subscriptions.push(
      interval(30000).subscribe(() => this.loadMonitoring())
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private loadMonitoring(): void {
    this.api.getMonitoring().subscribe({
      next: (data) => this.data.set(data)
    });
  }

  refresh(): void {
    this.loadMonitoring();
  }

  formatBytes(bytes: number): string {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  formatDuration(ms: number): string {
    if (!ms) return '0s';
    const hours = Math.floor(ms / 3600000);
    const minutes = Math.floor((ms % 3600000) / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${hours}h ${minutes}m ${seconds}s`;
  }
}
