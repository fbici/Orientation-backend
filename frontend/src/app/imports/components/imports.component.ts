import { Component, OnInit, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-imports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Imports</h1><p>Import et validation de données — CSV, Excel</p></div>
        <div class="page-header-actions">
          <button class="btn btn-primary" (click)="fileInput.click()">
            <span class="material-symbols-rounded">upload_file</span>Nouvel import
          </button>
          <input #fileInput type="file" accept=".csv,.xlsx,.xls" style="display:none" (change)="onFileSelected($event)">
        </div>
      </div>

      @if (uploading()) {
        <div class="alert alert-info" style="margin-bottom:20px">
          <div class="spinner-sm"></div>
          <span>Import en cours… Veuillez patienter.</span>
        </div>
      }
      @if (uploadSuccess()) {
        <div class="alert alert-success" style="margin-bottom:20px">
          <span class="material-symbols-rounded">check_circle</span>
          <span>Fichier importé avec succès !</span>
        </div>
      }
      @if (uploadError()) {
        <div class="alert alert-error" style="margin-bottom:20px">
          <span class="material-symbols-rounded">error</span>
          <span>{{ uploadError() }}</span>
        </div>
      }

      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:20px;margin-bottom:22px" class="stagger">
        @for (k of kpis; track k.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div>
            <div class="stat-content"><div class="stat-label">{{ k.label }}</div><div class="stat-value">{{ k.val }}</div></div>
          </div>
        }
      </div>

      <div class="card anim-fade-up">
        <div class="card-header"><h3>Historique des imports</h3>
          <button class="btn btn-secondary btn-sm" (click)="loadImports()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner-lg"></div></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Fichier</th><th>Type</th><th>Lignes</th><th>Validées</th><th>Erreurs</th><th>Statut</th><th>Date</th></tr></thead>
              <tbody>
                @for (i of imports; track i.id || i.fileName) {
                  <tr>
                    <td><div style="display:flex;align-items:center;gap:8px"><span class="material-symbols-rounded" style="font-size:18px;color:var(--n-400)">description</span><span style="font-weight:500">{{ i.fileName || i.name }}</span></div></td>
                    <td><span class="badge badge-gray">{{ i.fileType || i.type || 'CSV' }}</span></td>
                    <td>{{ i.totalRows || i.rows || 0 }}</td>
                    <td style="color:var(--green-600);font-weight:600">{{ i.validRows || i.valid || 0 }}</td>
                    <td [style.color]="(i.errorRows || i.errors || 0) > 0 ? 'var(--red-500)' : 'var(--n-400)'" style="font-weight:600">{{ i.errorRows || i.errors || 0 }}</td>
                    <td><span class="badge" [class]="statusClass(i.status)">{{ i.status || 'En cours' }}</span></td>
                    <td style="font-size:.8125rem;color:var(--n-500)">{{ i.createdAt || i.date | date:'short' }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="7" style="text-align:center;padding:48px;color:var(--n-400)">
                    <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px">upload_file</span>
                    Aucun import. Cliquez sur "Nouvel import" pour commencer.
                  </td></tr>
                }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host{display:block}
    .spinner-lg{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    .spinner-sm{width:16px;height:16px;border:2px solid var(--n-300);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
    .alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);font-size:.8125rem}
    .alert-info{background:var(--sky-50);color:var(--sky-600);border:1px solid rgba(14,165,233,.15)}
    .alert-success{background:var(--green-50);color:var(--green-700);border:1px solid rgba(22,163,74,.15)}
    .alert-error{background:var(--red-50);color:var(--red-600);border:1px solid rgba(239,68,68,.15)}
    .alert .material-symbols-rounded{font-size:18px}
  `]
})
export class ImportsComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  imports: any[] = [];
  loading = signal(false);
  uploading = signal(false);
  uploadSuccess = signal(false);
  uploadError = signal('');
  kpis = [
    { icon: 'upload_file', label: 'Total imports', val: '0', g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'check_circle', label: 'Réussis', val: '0', g: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'error', label: 'Échoués', val: '0', g: 'linear-gradient(135deg,#ef4444,#dc2626)' },
    { icon: 'pending', label: 'En cours', val: '0', g: 'linear-gradient(135deg,#f97316,#ea580c)' },
  ];

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadImports();
  }

  loadImports(): void {
    this.loading.set(true);
    this.api.getImports().subscribe({
      next: (res) => {
        const items = res?.content || res || [];
        this.imports = Array.isArray(items) ? items : [];
        this.updateKpis();
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    this.uploading.set(true);
    this.uploadSuccess.set(false);
    this.uploadError.set('');

    this.api.uploadImport(file).subscribe({
      next: () => {
        this.uploading.set(false);
        this.uploadSuccess.set(true);
        this.loadImports();
        // Reset file input
        if (this.fileInput) this.fileInput.nativeElement.value = '';
        setTimeout(() => this.uploadSuccess.set(false), 4000);
      },
      error: (e) => {
        this.uploading.set(false);
        this.uploadError.set(e.error?.message || 'Erreur lors de l\'import du fichier.');
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      }
    });
  }

  private updateKpis(): void {
    const total = this.imports.length;
    const success = this.imports.filter(i => this.isCompleted(i.status)).length;
    const failed = this.imports.filter(i => this.isFailed(i.status)).length;
    const pending = total - success - failed;
    this.kpis[0].val = String(total);
    this.kpis[1].val = String(success);
    this.kpis[2].val = String(failed);
    this.kpis[3].val = String(pending);
  }

  statusClass(status: string): string {
    if (!status) return 'badge-info';
    const s = status.toLowerCase();
    if (s.includes('complet') || s.includes('termin') || s.includes('success')) return 'badge-success';
    if (s.includes('erreur') || s.includes('fail') || s.includes('error')) return 'badge-danger';
    if (s.includes('cours') || s.includes('pending') || s.includes('processing')) return 'badge-info';
    return 'badge-gray';
  }

  private isCompleted(status: string): boolean {
    if (!status) return false;
    const s = status.toLowerCase();
    return s.includes('complet') || s.includes('termin') || s.includes('success');
  }

  private isFailed(status: string): boolean {
    if (!status) return false;
    return status.toLowerCase().includes('erreur') || status.toLowerCase().includes('fail');
  }
}
