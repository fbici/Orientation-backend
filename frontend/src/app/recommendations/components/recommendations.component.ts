import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Recommandations</h1><p>Moteur d'orientation universitaire intelligent</p></div>
        <div class="page-header-actions">
          <button class="btn btn-secondary" (click)="showSimulator = !showSimulator"><span class="material-symbols-rounded">experiment</span>Simuler</button>
        </div>
      </div>

      <!-- Generate form -->
      <div class="card anim-fade-up" style="margin-bottom:22px">
        <div class="card-header"><h3>Nouvelle recommandation</h3></div>
        <div class="card-body">
          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:0 20px">
            <div class="form-group"><label class="form-label">Type de bac</label>
              <select class="form-input" [(ngModel)]="form.bacType"><option value="">Sélectionner…</option><option>Sciences Expérimentales</option><option>Mathématiques</option><option>Technique</option><option>Littéraire</option></select>
            </div>
            <div class="form-group"><label class="form-label">Moyenne générale (/20)</label><input type="number" class="form-input" [(ngModel)]="form.bacAverage" placeholder="ex: 14.5" min="0" max="20" step="0.5"></div>
            <div class="form-group"><label class="form-label">Pays préféré</label>
              <select class="form-input" [(ngModel)]="form.country"><option value="">Tous les pays</option>@for(c of countries;track c.id){<option [value]="c.id">{{c.name}}</option>}</select>
            </div>
          </div>
          <div style="display:flex;gap:10px;margin-top:4px">
            <button class="btn btn-primary" (click)="generate()" [disabled]="generating()">
              @if(generating()){<span class="spinner" style="width:14px;height:14px;border-width:2px"></span>}@else{<span class="material-symbols-rounded">auto_awesome</span>}
              Lancer l'analyse
            </button>
          </div>
        </div>
      </div>

      <!-- Results -->
      <div class="card anim-fade-up">
        <div class="card-header">
          <h3>Résultats</h3>
          <input type="text" class="form-input" style="width:200px" placeholder="Filtrer…" [(ngModel)]="filter">
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center;color:var(--n-400)"><div class="spinner-lg"></div><p style="margin-top:12px;font-size:.8125rem">Chargement…</p></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Candidat</th><th>Programme</th><th>Université</th><th>Score</th><th>Éligibilité</th><th>Date</th><th></th></tr></thead>
              <tbody>
                @for (r of filteredRecs(); track r.id) {
                  <tr>
                    <td><div style="display:flex;align-items:center;gap:10px"><div class="avatar" [style.background]="r.avatarColor">{{ r.initials }}</div><span style="font-weight:600">{{ r.candidateName }}</span></div></td>
                    <td style="font-weight:500">{{ r.programName }}</td>
                    <td>{{ r.universityName }}</td>
                    <td><div style="display:flex;align-items:center;gap:8px"><div class="progress" style="width:60px"><div class="progress-bar" [class]="scoreColor(r.score)" [style.width.%]="r.score"></div></div><span style="font-weight:700;font-size:.8125rem">{{ r.score }}%</span></div></td>
                    <td><span class="badge" [class]="eligClass(r.status)">{{ r.status }}</span></td>
                    <td style="font-size:.8125rem;color:var(--n-500)">{{ r.createdAt | date:'short' }}</td>
                    <td><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">visibility</span></button></td>
                  </tr>
                } @empty {
                  <tr><td colspan="7" style="text-align:center;padding:48px;color:var(--n-400)">Aucune recommandation. Lancez une analyse ci-dessus.</td></tr>
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
    .spinner{width:18px;height:18px;border:2.5px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}
    .spinner-lg{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class RecommendationsComponent implements OnInit {
  form = { bacType: '', bacAverage: null as number | null, country: '' };
  filter = '';
  generating = signal(false);
  loading = signal(false);
  showSimulator = false;
  recommendations: any[] = [];
  countries: any[] = [];

  private avatarColors = ['#3b82f6','#8b5cf6','#f97316','#14b8a6','#ef4444','#22c55e','#ec4899'];

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadRecs();
    this.api.getCountries().subscribe({
      next: (res) => { this.countries = res?.content || res || []; },
      error: () => {}
    });
  }

  loadRecs(): void {
    this.loading.set(true);
    this.api.getRecommendations().subscribe({
      next: (res) => {
        const items = res?.content || res?.recommendations || res || [];
        this.recommendations = Array.isArray(items) ? items : [];
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
  }

  generate(): void {
    if (!this.form.bacType || !this.form.bacAverage) return;
    this.generating.set(true);
    this.api.generateRecommendations({
      bacType: this.form.bacType,
      bacAverage: this.form.bacAverage,
      preferredCountries: this.form.country ? [this.form.country] : undefined
    }).subscribe({
      next: (res) => {
        this.generating.set(false);
        this.loadRecs();
      },
      error: () => { this.generating.set(false); }
    });
  }

  filteredRecs(): any[] {
    if (!this.filter) return this.recommendations;
    const q = this.filter.toLowerCase();
    return this.recommendations.filter(r =>
      (r.candidateName || '').toLowerCase().includes(q) ||
      (r.programName || '').toLowerCase().includes(q) ||
      (r.universityName || '').toLowerCase().includes(q)
    );
  }

  scoreColor(score: number): string {
    if (score >= 80) return 'green';
    if (score >= 60) return 'blue';
    if (score >= 40) return 'amber';
    return 'red';
  }

  eligClass(status: string): string {
    if (!status) return 'badge-gray';
    const s = status.toLowerCase();
    if (s.includes('eligible') && !s.includes('non') && !s.includes('condition')) return 'badge-success';
    if (s.includes('condition')) return 'badge-warning';
    if (s.includes('non') || s.includes('reject')) return 'badge-danger';
    return 'badge-gray';
  }

  getInitials(name: string): string {
    if (!name) return '??';
    return name.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase();
  }
}
