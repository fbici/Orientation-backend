import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Recommandations</h1><p>Moteur d'orientation universitaire intelligent</p></div>
        <div class="page-header-actions">
          <button class="btn btn-secondary"><span class="material-symbols-rounded">experiment</span>Simuler</button>
          <button class="btn btn-primary"><span class="material-symbols-rounded">auto_awesome</span>Générer</button>
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
      <div class="card anim-fade-up" style="margin-bottom:22px">
        <div class="card-header"><h3>Nouvelle recommandation</h3></div>
        <div class="card-body">
          <div class="g3">
            <div class="form-group"><label class="form-label">Type de bac</label><select class="form-input"><option>Sciences Expérimentales</option><option>Mathématiques</option><option>Technique</option><option>Littéraire</option></select></div>
            <div class="form-group"><label class="form-label">Moyenne générale (/20)</label><input type="number" class="form-input" placeholder="ex: 14.5" min="0" max="20" step="0.5"></div>
            <div class="form-group"><label class="form-label">Pays préféré</label><select class="form-input"><option>Tous les pays</option><option>Bénin</option><option>Sénégal</option><option>France</option><option>Canada</option></select></div>
          </div>
          <div style="display:flex;gap:10px;margin-top:4px">
            <button class="btn btn-primary"><span class="material-symbols-rounded">auto_awesome</span>Lancer l'analyse</button>
            <button class="btn btn-secondary"><span class="material-symbols-rounded">science</span>Simuler un scénario</button>
          </div>
        </div>
      </div>
      <div class="card anim-fade-up">
        <div class="card-header">
          <h3>Dernières recommandations</h3>
          <div style="display:flex;gap:8px"><input type="text" class="form-input" style="width:200px" placeholder="Filtrer…"><button class="btn btn-secondary btn-sm"><span class="material-symbols-rounded" style="font-size:16px">filter_list</span>Filtres</button></div>
        </div>
        <div class="card-body" style="padding:0">
          <table class="data-table">
            <thead><tr><th>Candidat</th><th>Programme</th><th>Université</th><th>Score</th><th>Éligibilité</th><th>Date</th><th></th></tr></thead>
            <tbody>
              @for (r of recs; track r.id) {
                <tr>
                  <td><div style="display:flex;align-items:center;gap:10px"><div class="avatar" [style.background]="r.ac">{{ r.ini }}</div><span style="font-weight:600">{{ r.name }}</span></div></td>
                  <td style="font-weight:500">{{ r.prog }}</td>
                  <td>{{ r.uni }}</td>
                  <td><div style="display:flex;align-items:center;gap:8px"><div class="progress" style="width:60px"><div class="progress-bar" [class]="r.sc" [style.width.%]="r.score"></div></div><span style="font-weight:700;font-size:.8125rem">{{ r.score }}%</span></div></td>
                  <td><span class="badge" [class]="r.eCls">{{ r.elig }}</span></td>
                  <td style="font-size:.8125rem;color:var(--n-500)">{{ r.date }}</td>
                  <td><div style="display:flex;gap:2px"><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">visibility</span></button><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">info</span></button></div></td>
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
export class RecommendationsComponent {
  kpis = [
    { icon: 'recommend', label: 'Total reco.', val: '18 432', g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'check_circle', label: 'Taux admission', val: '78%', g: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'school', label: 'Programmes', val: '486', g: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
    { icon: 'science', label: 'Simulations', val: '1 293', g: 'linear-gradient(135deg,#f97316,#ea580c)' },
  ];
  recs = [
    { id: 1, name: 'Jean Dupont', ini: 'JD', ac: '#3b82f6', prog: 'Génie Informatique', uni: 'UAC', score: 92, sc: 'green', elig: 'Éligible', eCls: 'badge-success', date: '29/07/2026' },
    { id: 2, name: 'Marie Koudjo', ini: 'MK', ac: '#8b5cf6', prog: 'Médecine Générale', uni: 'UAC', score: 85, sc: 'blue', elig: 'Éligible', eCls: 'badge-success', date: '29/07/2026' },
    { id: 3, name: 'Paul Agossa', ini: 'PA', ac: '#f97316', prog: 'Droit Privé', uni: 'UNB', score: 72, sc: 'blue', elig: 'Conditionnel', eCls: 'badge-warning', date: '28/07/2026' },
    { id: 4, name: 'Fatima Bello', ini: 'FB', ac: '#14b8a6', prog: 'Pharmacie', uni: 'UAC', score: 68, sc: 'amber', elig: 'Conditionnel', eCls: 'badge-warning', date: '28/07/2026' },
    { id: 5, name: 'Ibrahim Touré', ini: 'IT', ac: '#ef4444', prog: 'Génie Civil', uni: 'UAT', score: 45, sc: 'red', elig: 'Non éligible', eCls: 'badge-danger', date: '27/07/2026' },
  ];
}
