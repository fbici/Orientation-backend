import { Component } from '@angular/core';

@Component({
  selector: 'app-reports',
  standalone: true,
  template: `
    <div class="reports">
      <h1>Rapports</h1>
      <div class="content-card">
        <h3>Génération de Rapports</h3>
        <p>Module de rapports en cours de développement.</p>
        <div class="features">
          <div class="feature">📊 Export PDF</div>
          <div class="feature">📈 Export Excel</div>
          <div class="feature">📋 Export CSV</div>
          <div class="feature">📄 Export JSON</div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .reports { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .content-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    .features { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-top: 20px; }
    .feature { padding: 10px; background: #f8f9fa; border-radius: 4px; text-align: center; }
  `]
})
export class ReportsComponent {}
