import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

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
          <button class="btn btn-primary"><span class="material-symbols-rounded">upload_file</span>Nouvel import</button>
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
        <div class="card-header"><h3>Historique des imports</h3>
          <button class="btn btn-secondary btn-sm"><span class="material-symbols-rounded" style="font-size:16px">filter_list</span>Filtrer</button>
        </div>
        <div class="card-body" style="padding:0">
          <table class="data-table">
            <thead><tr><th>Fichier</th><th>Type</th><th>Lignes</th><th>Validées</th><th>Erreurs</th><th>Statut</th><th>Date</th><th></th></tr></thead>
            <tbody>
              @for (i of imports; track i.file) {
                <tr>
                  <td><div style="display:flex;align-items:center;gap:8px"><span class="material-symbols-rounded" style="font-size:18px;color:var(--n-400)">description</span><span style="font-weight:500">{{ i.file }}</span></div></td>
                  <td><span class="badge badge-gray">{{ i.type }}</span></td>
                  <td>{{ i.rows }}</td>
                  <td style="color:var(--green-600);font-weight:600">{{ i.valid }}</td>
                  <td [style.color]="i.err>0 ? 'var(--red-500)' : 'var(--n-400)'" style="font-weight:600">{{ i.err }}</td>
                  <td><span class="badge" [class]="i.sCls">{{ i.status }}</span></td>
                  <td style="font-size:.8125rem;color:var(--n-500)">{{ i.date }}</td>
                  <td><div style="display:flex;gap:2px"><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">visibility</span></button><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">restart_alt</span></button></div></td>
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
export class ImportsComponent {
  kpis = [
    { icon: 'upload_file', label: 'Total imports', val: '47', g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'check_circle', label: 'Réussis', val: '42', g: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'error', label: 'Échoués', val: '3', g: 'linear-gradient(135deg,#ef4444,#dc2626)' },
    { icon: 'pending', label: 'En cours', val: '2', g: 'linear-gradient(135deg,#f97316,#ea580c)' },
  ];
  imports = [
    { file: 'universities_benin.csv', type: 'CSV', rows: 124, valid: 124, err: 0, status: 'Terminé', sCls: 'badge-success', date: '29/07/2026' },
    { file: 'programmes_faculte_sciences.xlsx', type: 'Excel', rows: 86, valid: 86, err: 0, status: 'Terminé', sCls: 'badge-success', date: '28/07/2026' },
    { file: 'bulletins_2025.csv', type: 'CSV', rows: 512, valid: 509, err: 3, status: 'Avec erreurs', sCls: 'badge-warning', date: '27/07/2026' },
    { file: 'bourses_excellence.csv', type: 'CSV', rows: 45, valid: 45, err: 0, status: 'Terminé', sCls: 'badge-success', date: '26/07/2026' },
    { file: 'candidats_nouveaux.csv', type: 'CSV', rows: 0, valid: 0, err: 0, status: 'En cours', sCls: 'badge-info', date: '29/07/2026' },
  ];
}
