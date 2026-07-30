import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-header" [class.anim-fade-up]="animate">
      <div style="display:flex;align-items:center;gap:12px">
        @if (backLink) {
          <a [routerLink]="backLink" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a>
        }
        <div>
          <h1>{{ title }}</h1>
          @if (subtitle) { <p>{{ subtitle }}</p> }
        </div>
      </div>
      @if (hasActions) {
        <div class="page-header-actions"><ng-content select="[actions]"></ng-content></div>
      }
    </div>
  `
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() backLink = '';
  @Input() animate = true;
  @Input() hasActions = true;
}
