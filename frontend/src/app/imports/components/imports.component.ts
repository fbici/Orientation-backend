import { Component, OnInit, signal } from '@angular/core';
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
          <button class="btn btn-secondary"><span class="material-symbols-rounded">history</span>Historique</button>
          <button class="btn btn-primary" (click)="triggerUpload()"><span class="material-symbols-rounded">upload_file</span>Nouvel import</button>
          <input type="file" #fileInput accept=".csv,.xlsx,.xls" style="display:none" (change)="onFileSelected($event)">
        </div>
      </div>

      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:20px;margin-bottom:22px" class="stagger">
        @for (k of kpis; track k.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div>
            <div class="stat-content"><div class="stat-label">{{ k.label }}</div><div class="stat-value">{{ k.val }}</div></div>
          </div>
        }
      </div>

      <div class="card anim-fade-up">
        <div class="card-header"><h3>Historique des imports</h3></div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner-lg"></div></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Fichier</th><th>Type</th><th>Lignes</th><th>Validées</th><th>Erreurs</th><th>Statut</th><th>Date</th></tr></thead>
              <tbody>
                @for (i of imports; track i.id) {
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
                  <tr><td colspan="7" style="text-align:center;padding:48px;color:var(--n-400)">Aucun import. Cliquez sur "Nouvel import" pour commencer.</td></tr>
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
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class ImportsComponent implements OnInit {
  imports: any[] = [];
  loading = signal(false);
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

  triggerUpload(): void {
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    input?.click();
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.loading.set(true);
    this.api.uploadImport(file).subscribe({
      next: () => { this.loadImports(); },
      error: () => { this.loading.set(false); }
    });
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
