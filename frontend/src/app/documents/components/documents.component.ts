import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Documents</h1><p>Intelligence documentaire — OCR, classification, extraction</p></div>
        <div class="page-header-actions">
          <button class="btn btn-secondary"><span class="material-symbols-rounded">document_scanner</span>Scanner</button>
          <button class="btn btn-primary"><span class="material-symbols-rounded">upload_file</span>Uploader</button>
        </div>
      </div>
      <div class="g4 stagger" style="margin-bottom:22px">
        @for (k of kpis; track k.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div>
            <div class="stat-content"><div class="stat-label">{{ k.label }}</div><div class="stat-value">{{ k.val }}</div></div>
          </div>
        }
      </div>
      <div class="card anim-fade-up">
        <div class="card-header"><h3>Documents récents</h3>
          <div style="display:flex;gap:8px"><input type="text" class="form-input" style="width:200px" placeholder="Rechercher…"><button class="btn btn-secondary btn-sm"><span class="material-symbols-rounded" style="font-size:16px">filter_list</span></button></div>
        </div>
        <div class="card-body" style="padding:0">
          <table class="data-table">
            <thead><tr><th>Document</th><th>Type</th><th>Statut</th><th>Classification</th><th>Date</th><th></th></tr></thead>
            <tbody>
              @for (d of docs; track d.name) {
                <tr>
                  <td><div style="display:flex;align-items:center;gap:10px"><span class="material-symbols-rounded" [style.color]="d.ic" style="font-size:20px">{{ d.icon }}</span><span style="font-weight:500">{{ d.name }}</span></div></td>
                  <td><span class="badge badge-gray">{{ d.type }}</span></td>
                  <td><span class="badge" [class]="d.sCls">{{ d.status }}</span></td>
                  <td style="font-size:.8125rem">{{ d.classif }}</td>
                  <td style="font-size:.8125rem;color:var(--n-500)">{{ d.date }}</td>
                  <td><div style="display:flex;gap:2px"><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">visibility</span></button><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">download</span></button></div></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class DocumentsComponent {
  kpis = [
    { icon: 'description', label: 'Total', val: '563', g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'check_circle', label: 'Traités', val: '541', g: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'pending', label: 'En attente', val: '18', g: 'linear-gradient(135deg,#f97316,#ea580c)' },
    { icon: 'error', label: 'Erreurs', val: '4', g: 'linear-gradient(135deg,#ef4444,#dc2626)' },
  ];
  docs = [
    { name: 'Guide_Orientation_2025.pdf', icon: 'picture_as_pdf', ic: '#ef4444', type: 'PDF', status: 'Traité', sCls: 'badge-success', classif: 'Guide officiel', date: '29/07/2026' },
    { name: 'Bulletin_Jean_Dupont.pdf', icon: 'picture_as_pdf', ic: '#ef4444', type: 'PDF', status: 'Traité', sCls: 'badge-success', classif: 'Bulletin scolaire', date: '28/07/2026' },
    { name: 'Programmes_Faculté_Sciences.xlsx', icon: 'table_chart', ic: '#16a34a', type: 'Excel', status: 'Traité', sCls: 'badge-success', classif: 'Liste de programmes', date: '27/07/2026' },
    { name: 'Critères_Admission_2026.pdf', icon: 'picture_as_pdf', ic: '#ef4444', type: 'PDF', status: 'En cours', sCls: 'badge-warning', classif: 'En cours de traitement', date: '26/07/2026' },
    { name: 'Scan_Bulletin_Marie.jpg', icon: 'image', ic: '#3b82f6', type: 'Image', status: 'Erreur', sCls: 'badge-danger', classif: 'Échec OCR', date: '25/07/2026' },
  ];
}
