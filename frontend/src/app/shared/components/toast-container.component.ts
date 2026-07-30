import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (t of toastSvc.toasts(); track t.id) {
        <div class="toast" [class]="'toast-' + t.type">
          <span class="material-symbols-rounded toast-icon">{{ icon(t.type) }}</span>
          <span class="toast-msg">{{ t.message }}</span>
          <button class="toast-close" (click)="toastSvc.remove(t.id)">
            <span class="material-symbols-rounded" style="font-size:16px">close</span>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container{position:fixed;top:80px;right:24px;z-index:200;display:flex;flex-direction:column;gap:8px;max-width:400px}
    .toast{display:flex;align-items:center;gap:10px;padding:12px 16px;border-radius:var(--radius-md);background:#fff;border:1px solid var(--n-200);box-shadow:var(--shadow-lg);animation:slideIn .3s var(--ease-out);font-size:.8125rem;color:var(--n-800)}
    @keyframes slideIn{from{opacity:0;transform:translateX(20px)}to{opacity:1;transform:translateX(0)}}
    .toast-icon{font-size:18px;flex-shrink:0}
    .toast-success .toast-icon{color:var(--green-600)}.toast-error .toast-icon{color:var(--red-500)}.toast-warning .toast-icon{color:var(--amber-500)}.toast-info .toast-icon{color:var(--sky-500)}
    .toast-msg{flex:1;line-height:1.4}
    .toast-close{background:none;border:none;cursor:pointer;color:var(--n-400);padding:2px;display:flex}.toast-close:hover{color:var(--n-600)}
  `]
})
export class ToastContainerComponent {
  constructor(public toastSvc: ToastService) {}
  icon(type: string): string {
    return { success: 'check_circle', error: 'error', warning: 'warning', info: 'info' }[type] || 'info';
  }
}
