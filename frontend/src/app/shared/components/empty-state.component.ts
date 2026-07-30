import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="text-align:center;padding:48px 24px;color:var(--n-400)">
      <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:16px;color:var(--n-300)">{{ icon }}</span>
      <h3 style="font-size:.9375rem;font-weight:600;color:var(--n-600);margin-bottom:6px">{{ title }}</h3>
      <p style="font-size:.8125rem;color:var(--n-400);margin-bottom:16px">{{ message }}</p>
      <ng-content></ng-content>
    </div>
  `
})
export class EmptyStateComponent {
  @Input() icon = 'inbox';
  @Input() title = 'Aucune donnée';
  @Input() message = 'Les données apparaîtront ici une fois disponibles.';
}
