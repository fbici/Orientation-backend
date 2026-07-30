import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="stat-card">
      <div class="stat-icon" [style.background]="gradient"><span class="material-symbols-rounded filled">{{ icon }}</span></div>
      <div class="stat-content">
        <div class="stat-label">{{ label }}</div>
        <div class="stat-value">{{ value }}</div>
        @if (change !== null && change !== undefined) {
          <div class="stat-change" [class.up]="change > 0" [class.down]="change < 0">
            <span class="material-symbols-rounded">{{ change > 0 ? 'trending_up' : 'trending_down' }}</span>
            {{ change > 0 ? '+' : '' }}{{ change }}%
          </div>
        }
      </div>
    </div>
  `
})
export class StatCardComponent {
  @Input() icon = 'analytics';
  @Input() label = '';
  @Input() value: string | number = 0;
  @Input() change: number | null = null;
  @Input() gradient = 'linear-gradient(135deg,#3b82f6,#1d4ed8)';
}
