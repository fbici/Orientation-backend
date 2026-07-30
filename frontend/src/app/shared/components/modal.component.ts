import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (open) {
      <div class="modal-overlay" (click)="close.emit()">
        <div class="modal-card" [style.max-width]="size" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>{{ title }}</h3>
            <button class="btn btn-ghost btn-icon btn-sm" (click)="close.emit()">
              <span class="material-symbols-rounded">close</span>
            </button>
          </div>
          <div class="modal-body">
            <ng-content></ng-content>
          </div>
          @if (showFooter) {
            <div class="modal-footer">
              <button class="btn btn-secondary" (click)="close.emit()">Annuler</button>
              <button class="btn" [class]="confirmClass" (click)="confirm.emit()" [disabled]="confirmDisabled">
                @if (confirmLoading) { <span class="spinner-sm"></span> }
                {{ confirmText }}
              </button>
            </div>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .modal-overlay{position:fixed;inset:0;background:rgba(0,0,0,.5);backdrop-filter:blur(4px);display:flex;align-items:center;justify-content:center;z-index:100;padding:24px;animation:fadeIn .2s ease}
    .modal-card{background:#fff;border-radius:var(--radius-lg);width:100%;max-height:90vh;display:flex;flex-direction:column;box-shadow:var(--shadow-xl);animation:slideUp .25s var(--ease-out)}
    @keyframes fadeIn{from{opacity:0}to{opacity:1}}
    @keyframes slideUp{from{opacity:0;transform:translateY(16px)}to{opacity:1;transform:translateY(0)}}
    .modal-header{display:flex;align-items:center;justify-content:space-between;padding:20px 24px;border-bottom:1px solid var(--n-100)}
    .modal-header h3{font-size:1rem;font-weight:700;color:var(--n-900)}
    .modal-body{padding:24px;overflow-y:auto;flex:1}
    .modal-footer{display:flex;justify-content:flex-end;gap:10px;padding:16px 24px;border-top:1px solid var(--n-100)}
    .spinner-sm{width:14px;height:14px;border:2px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite;display:inline-block;margin-right:6px}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class ModalComponent {
  @Input() open = false;
  @Input() title = '';
  @Input() size = '520px';
  @Input() showFooter = true;
  @Input() confirmText = 'Confirmer';
  @Input() confirmClass = 'btn-primary';
  @Input() confirmDisabled = false;
  @Input() confirmLoading = false;
  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
}
