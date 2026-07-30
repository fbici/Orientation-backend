import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from './modal.component';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  template: `
    <app-modal
      [open]="open"
      [title]="title"
      size="400px"
      [confirmText]="confirmText"
      [confirmClass]="confirmClass"
      [confirmLoading]="loading"
      (close)="close.emit()"
      (confirm)="confirm.emit()">
      <div style="display:flex;align-items:flex-start;gap:14px">
        <span class="material-symbols-rounded" [style.color]="iconColor" style="font-size:24px;flex-shrink:0;margin-top:2px">{{ icon }}</span>
        <div>
          <p style="font-size:.875rem;color:var(--n-700);line-height:1.5">{{ message }}</p>
        </div>
      </div>
    </app-modal>
  `
})
export class ConfirmDialogComponent {
  @Input() open = false;
  @Input() title = 'Confirmation';
  @Input() message = 'Êtes-vous sûr ?';
  @Input() confirmText = 'Confirmer';
  @Input() confirmClass = 'btn-danger';
  @Input() icon = 'warning';
  @Input() iconColor = '#f59e0b';
  @Input() loading = false;
  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
}
