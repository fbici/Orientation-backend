import { Component, OnInit, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';
import { ModalComponent } from '../../shared/components/modal.component';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmDialogComponent, ModalComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Documents</h1><p>Intelligence documentaire — OCR, classification, extraction</p></div>
        <div class="page-header-actions">
          <button class="btn btn-primary" (click)="fileInput.click()"><span class="material-symbols-rounded">upload_file</span>Uploader</button>
          <input #fileInput type="file" accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xlsx" style="display:none" (change)="onUpload($event)">
        </div>
      </div>

      @if (uploading()) {
        <div class="alert alert-info" style="margin-bottom:20px"><div class="spinner-sm"></div><span>Upload et traitement en cours…</span></div>
      }

      <div class="g4 stagger" style="margin-bottom:22px">
        @for (k of kpis; track k.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div>
            <div class="stat-content"><div class="stat-label">{{ k.label }}</div><div class="stat-value">{{ k.val }}</div></div>
          </div>
        }
      </div>

      <div class="card">
        <div class="card-header">
          <span style="font-size:.8125rem;color:var(--n-500)">{{ documents().length }} documents</span>
          <button class="btn btn-secondary btn-sm" (click)="load()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner"></div></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Document</th><th>Type</th><th>Classification</th><th>OCR</th><th>Statut</th><th>Date</th><th style="text-align:right">Actions</th></tr></thead>
              <tbody>
                @for (d of documents(); track d.id) {
                  <tr>
                    <td>
                      <div style="display:flex;align-items:center;gap:10px">
                        <span class="material-symbols-rounded" [style.color]="fileColor(d)" style="font-size:20px">{{ fileIcon(d) }}</span>
                        <div><div style="font-weight:600;font-size:.875rem;color:var(--n-900)">{{ d.fileName || d.name }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ d.fileSize || '—' }}</div></div>
                      </div>
                    </td>
                    <td><span class="badge badge-gray">{{ d.fileType || d.mimeType || '—' }}</span></td>
                    <td style="font-size:.8125rem">{{ d.classification || '—' }}</td>
                    <td><span class="badge" [class]="d.ocrStatus === 'COMPLETED' ? 'badge-success' : d.ocrStatus === 'PROCESSING' ? 'badge-info' : 'badge-gray'">{{ d.ocrStatus || 'En attente' }}</span></td>
                    <td><span class="badge" [class]="d.status === 'PROCESSED' ? 'badge-success' : 'badge-info'">{{ d.status || 'Nouveau' }}</span></td>
                    <td style="font-size:.8125rem;color:var(--n-500)">{{ d.createdAt | date:'short' }}</td>
                    <td style="text-align:right">
                      <div style="display:flex;gap:2px;justify-content:flex-end">
                        <button class="btn btn-ghost btn-icon btn-sm" (click)="viewDetail(d)"><span class="material-symbols-rounded" style="font-size:18px">visibility</span></button>
                        <button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDel(d)"><span class="material-symbols-rounded" style="font-size:18px;color:var(--red-500)">delete</span></button>
                      </div>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="7" style="text-align:center;padding:48px;color:var(--n-400)">
                    <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">description</span>
                    <p style="font-weight:600;color:var(--n-600)">Aucun document</p>
                    <p style="font-size:.8125rem;margin-bottom:16px">Uploadez des documents pour lancer l'OCR et la classification.</p>
                    <button class="btn btn-primary btn-sm" (click)="fileInput.click()"><span class="material-symbols-rounded">upload_file</span>Uploader un document</button>
                  </td></tr>
                }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <app-modal [open]="showDetail()" [title]="detail()?.fileName || 'Document'" size="700px" [showFooter]="false" (close)="showDetail.set(false)">
      @if (detail()) {
        <div class="g2" style="margin-bottom:20px">
          <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Type</span><span style="font-weight:600">{{ detail().fileType || detail().mimeType }}</span></div>
          <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Classification</span><span style="font-weight:600">{{ detail().classification || '—' }}</span></div>
          <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">OCR</span><span class="badge" [class]="detail().ocrStatus === 'COMPLETED' ? 'badge-success' : 'badge-info'">{{ detail().ocrStatus || 'En attente' }}</span></div>
          <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Uploadé le</span><span>{{ detail().createdAt | date:'medium' }}</span></div>
        </div>
        @if (detail().extractedText) {
          <div style="margin-top:16px"><span style="font-size:.75rem;font-weight:600;color:var(--n-600);display:block;margin-bottom:8px">Texte extrait (OCR)</span>
            <div style="background:var(--n-50);padding:16px;border-radius:var(--radius-sm);font-size:.8125rem;color:var(--n-700);line-height:1.6;max-height:300px;overflow-y:auto;white-space:pre-wrap">{{ detail().extractedText }}</div>
          </div>
        }
        @if (detail().entities?.length) {
          <div style="margin-top:16px"><span style="font-size:.75rem;font-weight:600;color:var(--n-600);display:block;margin-bottom:8px">Entités détectées</span>
            <div style="display:flex;flex-wrap:wrap;gap:6px">@for(e of detail().entities;track e){<span class="badge badge-primary">{{e}}</span>}</div>
          </div>
        }
      }
    </app-modal>

    <app-confirm-dialog [open]="showDel()" title="Supprimer le document" [message]="'Supprimer « ' + (delTarget()?.fileName || '') + ' » ?'" icon="delete_forever" iconColor="#ef4444" [loading]="deleting()" (close)="showDel.set(false)" (confirm)="doDel()"></app-confirm-dialog>
  `,
  styles: [`
    .spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    .spinner-sm{width:16px;height:16px;border:2px solid var(--n-300);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
    .alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);font-size:.8125rem}
    .alert-info{background:var(--sky-50);color:var(--sky-600);border:1px solid rgba(14,165,233,.15)}
  `]
})
export class DocumentsComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  documents = signal<any[]>([]);
  loading = signal(false);
  uploading = signal(false);
  deleting = signal(false);
  showDetail = signal(false);
  showDel = signal(false);
  detail = signal<any>(null);
  delTarget = signal<any>(null);
  kpis = [
    { icon: 'description', label: 'Total', val: '0', g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'check_circle', label: 'Traités', val: '0', g: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'pending', label: 'En cours', val: '0', g: 'linear-gradient(135deg,#f97316,#ea580c)' },
    { icon: 'error', label: 'Erreurs', val: '0', g: 'linear-gradient(135deg,#ef4444,#dc2626)' },
  ];

  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.getDocuments().subscribe({
      next: (r) => {
        const items = r?.content || r || [];
        this.documents.set(Array.isArray(items) ? items : []);
        this.kpis[0].val = String(this.documents().length);
        this.kpis[1].val = String(this.documents().filter(d => d.status === 'PROCESSED' || d.ocrStatus === 'COMPLETED').length);
        this.kpis[2].val = String(this.documents().filter(d => d.ocrStatus === 'PROCESSING').length);
        this.kpis[3].val = String(this.documents().filter(d => d.status === 'ERROR').length);
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
  }

  onUpload(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.uploading.set(true);
    this.api.uploadDocument(file).subscribe({
      next: () => { this.uploading.set(false); this.toast.success('Document uploadé. Traitement en cours…'); this.load(); if (this.fileInput) this.fileInput.nativeElement.value = ''; },
      error: (e) => { this.uploading.set(false); this.toast.error(e.error?.message || 'Erreur upload.'); if (this.fileInput) this.fileInput.nativeElement.value = ''; }
    });
  }

  viewDetail(d: any): void {
    this.detail.set(d);
    // Load extractions if available
    if (d.id) {
      this.api.getDocumentExtractions(d.id).subscribe({ next: (r) => { this.detail.set({ ...d, entities: r?.entities || [] }); }, error: () => {} });
    }
    this.showDetail.set(true);
  }

  confirmDel(d: any): void { this.delTarget.set(d); this.showDel.set(true); }
  doDel(): void {
    this.deleting.set(true);
    this.api.deleteDocument(this.delTarget()?.id).subscribe({
      next: () => { this.deleting.set(false); this.showDel.set(false); this.toast.success('Document supprimé.'); this.load(); },
      error: (e) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur.'); }
    });
  }

  fileIcon(d: any): string {
    const t = (d.fileType || d.mimeType || '').toLowerCase();
    if (t.includes('pdf')) return 'picture_as_pdf';
    if (t.includes('image') || t.includes('jpg') || t.includes('png')) return 'image';
    if (t.includes('excel') || t.includes('xlsx') || t.includes('csv')) return 'table_chart';
    return 'description';
  }

  fileColor(d: any): string {
    const t = (d.fileType || d.mimeType || '').toLowerCase();
    if (t.includes('pdf')) return '#ef4444';
    if (t.includes('image')) return '#3b82f6';
    if (t.includes('excel') || t.includes('csv')) return '#16a34a';
    return '#6b7280';
  }
}
