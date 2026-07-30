import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Recommandations</h1><p>Moteur d'orientation universitaire intelligent</p></div>
      </div>

      <!-- Generate -->
      <div class="card" style="margin-bottom:22px">
        <div class="card-header"><h3>Nouvelle recommandation</h3></div>
        <div class="card-body">
          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:0 16px">
            <div class="form-group"><label class="form-label">Type de bac *</label>
              <select class="form-input" [(ngModel)]="form.bacType"><option value="">Sélectionner…</option><option>Sciences Expérimentales</option><option>Mathématiques</option><option>Technique</option><option>Littéraire</option></select>
            </div>
            <div class="form-group"><label class="form-label">Moyenne (/20) *</label><input type="number" class="form-input" [(ngModel)]="form.bacAverage" placeholder="ex: 14.5" min="0" max="20" step="0.5"></div>
            <div class="form-group"><label class="form-label">Langue</label>
              <select class="form-input" [(ngModel)]="form.language"><option value="">Indifférent</option><option>Français</option><option>Anglais</option><option>Arabe</option></select>
            </div>
          </div>
          <button class="btn btn-primary" (click)="generate()" [disabled]="generating()">
            @if (generating()) { <span class="spinner-sm"></span> } @else { <span class="material-symbols-rounded">auto_awesome</span> }
            Lancer l'analyse
          </button>
        </div>
      </div>

      <!-- Results -->
      <div class="card">
        <div class="card-header">
          <span style="font-size:.8125rem;color:var(--n-500)">{{ total() }} recommandations</span>
          <button class="btn btn-secondary btn-sm" (click)="load()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner"></div></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Candidat</th><th>Programme</th><th>Université</th><th>Score</th><th>Éligibilité</th><th>Date</th><th style="text-align:right">Actions</th></tr></thead>
              <tbody>
                @for (r of recommendations(); track r.id) {
                  <tr>
                    <td><div style="display:flex;align-items:center;gap:10px"><div class="avatar" [style.background]="avatarColor(r)">{{ initials(r) }}</div><span style="font-weight:600">{{ r.candidateName || r.candidate?.firstName + ' ' + r.candidate?.lastName || '—' }}</span></div></td>
                    <td style="font-weight:500">{{ r.programName || r.program?.name || '—' }}</td>
                    <td>{{ r.universityName || r.program?.faculty?.campus?.university?.name || '—' }}</td>
                    <td><div style="display:flex;align-items:center;gap:8px"><div class="progress" style="width:60px"><div class="progress-bar" [class]="scoreColor(r.score)" [style.width.%]="r.score"></div></div><span style="font-weight:700;font-size:.8125rem">{{ r.score }}%</span></div></td>
                    <td><span class="badge" [class]="eligClass(r.status || r.eligibilityStatus)">{{ r.status || r.eligibilityStatus || '—' }}</span></td>
                    <td style="font-size:.8125rem;color:var(--n-500)">{{ r.createdAt | date:'short' }}</td>
                    <td style="text-align:right"><button class="btn btn-ghost btn-icon btn-sm" (click)="viewDetail(r)"><span class="material-symbols-rounded" style="font-size:18px">visibility</span></button></td>
                  </tr>
                } @empty {
                  <tr><td colspan="7" style="text-align:center;padding:48px;color:var(--n-400)">
                    <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">recommend</span>
                    <p style="font-weight:600;color:var(--n-600)">Aucune recommandation</p>
                    <p style="font-size:.8125rem">Lancez une analyse pour générer des recommandations.</p>
                  </td></tr>
                }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <app-modal [open]="showDetail()" [title]="'Détail recommandation'" size="700px" [showFooter]="false" (close)="showDetail.set(false)">
      @if (detailData()) {
        <div class="g2" style="margin-bottom:16px">
          <div><span style="font-size:.75rem;color:var(--n-500)">Score</span><div style="font-size:2rem;font-weight:800;color:var(--brand)">{{ detailData().score }}%</div></div>
          <div><span style="font-size:.75rem;color:var(--n-500)">Éligibilité</span><div><span class="badge" [class]="eligClass(detailData().status)">{{ detailData().status || '—' }}</span></div></div>
        </div>
        @if (detailData().explanation || detailData().justification) {
          <div style="margin-top:16px"><span style="font-size:.75rem;font-weight:600;color:var(--n-600)">Explication</span><p style="font-size:.875rem;color:var(--n-700);line-height:1.6;margin-top:6px">{{ detailData().explanation || detailData().justification }}</p></div>
        }
        @if (detailData().criteria?.length) {
          <div style="margin-top:16px"><span style="font-size:.75rem;font-weight:600;color:var(--n-600)">Critères utilisés</span>
            <div style="margin-top:8px">@for(c of detailData().criteria;track c){<span class="badge badge-gray" style="margin:2px">{{c}}</span>}</div>
          </div>
        }
      }
    </app-modal>
  `,
  styles: [`
    .spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    .spinner-sm{width:16px;height:16px;border:2px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class RecommendationsComponent implements OnInit {
  recommendations = signal<any[]>([]);
  total = signal(0);
  loading = signal(false);
  generating = signal(false);
  showDetailModal = signal(false);
  detailData = signal<any>(null);
  form = { bacType: '', bacAverage: null as number | null, language: '' };
  private colors = ['#3b82f6','#8b5cf6','#f97316','#14b8a6','#ef4444','#22c55e'];

  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.getRecommendations().subscribe({
      next: (r) => {
        const items = r?.content || r || [];
        this.recommendations.set(Array.isArray(items) ? items : []);
        this.total.set(r?.totalElements ?? this.recommendations().length);
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
  }

  generate(): void {
    if (!this.form.bacType || !this.form.bacAverage) { this.toast.warning('Remplissez le type de bac et la moyenne.'); return; }
    this.generating.set(true);
    this.api.generateRecommendations(this.form).subscribe({
      next: () => { this.generating.set(false); this.toast.success('Recommandations générées.'); this.load(); },
      error: (e) => { this.generating.set(false); this.toast.error(e.error?.message || 'Erreur.'); }
    });
  }

  viewDetail(r: any): void {
    this.detailData.set(r);
    if (r.id) {
      this.api.getRecommendationExplanation(r.id).subscribe({
        next: (exp) => { this.detailData.set({ ...r, explanation: exp?.summary || exp?.text || '', criteria: exp?.criteria || [] }); },
        error: () => {}
      });
    }
    this.showDetailModal.set(true);
  }

  showDetail(): boolean { return this.showDetailModal(); }

  initials(r: any): string {
    const name = r.candidateName || `${r.candidate?.firstName || ''} ${r.candidate?.lastName || ''}`;
    return name.split(' ').map((w: string) => w[0]).join('').substring(0, 2).toUpperCase() || '??';
  }

  avatarColor(r: any): string {
    const hash = (r.candidateName || '').split('').reduce((a: number, c: string) => a + c.charCodeAt(0), 0);
    return this.colors[hash % this.colors.length];
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
}
