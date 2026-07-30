import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Rapports</h1><p>Génération et consultation des rapports analytiques</p></div>
        <div class="page-header-actions"><button class="btn btn-primary"><span class="material-symbols-rounded">add</span>Nouveau rapport</button></div>
      </div>
      <div class="g3 stagger">
        @for (r of reports; track r.title) {
          <div class="card anim-fade-up" style="cursor:pointer">
            <div class="card-body">
              <div style="display:flex;align-items:center;gap:12px;margin-bottom:14px">
                <div style="width:44px;height:44px;border-radius:var(--radius-md);display:flex;align-items:center;justify-content:center" [style.background]="r.bg">
                  <span class="material-symbols-rounded" [style.color]="r.ic" style="font-size:22px">{{ r.icon }}</span>
                </div>
                <div><div style="font-weight:600;color:var(--n-900);font-size:.9375rem">{{ r.title }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ r.type }}</div></div>
              </div>
              <p style="font-size:.8125rem;color:var(--n-600);line-height:1.5;margin-bottom:14px">{{ r.desc }}</p>
              <div style="display:flex;align-items:center;justify-content:space-between;padding-top:12px;border-top:1px solid var(--n-100)">
                <span style="font-size:.6875rem;color:var(--n-400)">{{ r.last }}</span>
                <button class="btn btn-secondary btn-sm"><span class="material-symbols-rounded" style="font-size:16px">download</span>Exporter</button>
              </div>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class ReportsComponent {
  reports = [
    { icon: 'assessment', title: "Rapport d'orientation", type: 'PDF / Excel', desc: "Synthèse des recommandations, taux d'admission, top programmes par période.", bg: '#eff6ff', ic: '#3b82f6', last: '28/07/2026' },
    { icon: 'group', title: 'Rapport candidats', type: 'PDF / CSV', desc: 'Liste complète des candidats avec profils, scores et statuts.', bg: '#f0fdf4', ic: '#16a34a', last: '27/07/2026' },
    { icon: 'school', title: 'Rapport universités', type: 'PDF', desc: 'Statistiques par université — programmes, classement, taux de remplissage.', bg: '#f5f3ff', ic: '#7c3aed', last: '25/07/2026' },
    { icon: 'upload_file', title: 'Rapport imports', type: 'PDF / CSV', desc: 'Historique des imports, taux de succès, erreurs fréquentes.', bg: '#fff7ed', ic: '#ea580c', last: '24/07/2026' },
    { icon: 'monitor_heart', title: 'Rapport système', type: 'PDF', desc: 'Performance, uptime, métriques serveur et alertes.', bg: '#f0f9ff', ic: '#0284c7', last: '23/07/2026' },
    { icon: 'payments', title: 'Rapport bourses', type: 'PDF / Excel', desc: 'Bourses attribuées, critères, budgets consommés.', bg: '#fef2f2', ic: '#dc2626', last: '20/07/2026' },
  ];
}
