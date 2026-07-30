import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (loading) {
      <div [style.padding]="padding" style="text-align:center">
        <div class="spinner" [style.width]="size + 'px'" [style.height]="size + 'px'"></div>
        @if (message) { <p style="margin-top:12px;font-size:.8125rem;color:var(--n-500)">{{ message }}</p> }
      </div>
    } @else {
      <ng-content></ng-content>
    }
  `,
  styles: [`
    .spinner{border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class LoadingComponent {
  @Input() loading = false;
  @Input() message = 'Chargement…';
  @Input() size = 32;
  @Input() padding = '48px';
}
