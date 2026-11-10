import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';

interface Diagnostic {
  id: string; runId: string; candidateId: string; programId: string;
  eligibilityStatus: 'ELIGIBLE' | 'NOT_ELIGIBLE' | 'INSUFFICIENT_DATA';
  score: number | null; dataCoverage: number; computedAt: string; stale: boolean; sourceCurrent: boolean;
  explanation: {
    programName?: string; academicYearId?: string; sourceUrl?: string;
    eligibility?: { warnings?: string[]; criteria?: Array<{
      criterionId: string; criterionName: string; expectedValue: string;
      actualValue: string | null; status: string; message: string; sourceFactId: string;
      guideVersionId: string;
    }> };
  };
}
@Component({
  selector: 'app-recommendations', standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header"><div><h1>Recommandations</h1><p>Diagnostics des calculs depuis le profil confirme du candidat</p></div></div>
      <div class="card" style="margin-bottom:22px"><div class="card-body">
        <label class="form-label" for="candidate-id">Identifiant du candidat</label>
        <input id="candidate-id" class="form-input" [(ngModel)]="candidateId" placeholder="UUID du candidat">
        <p>La confirmation du candidat ne constitue pas une certification officielle. Un score de compatibilite ne garantit pas une admission.</p>
        <button class="btn btn-secondary" (click)="load()" [disabled]="loading()">Consulter</button>
        <button class="btn btn-primary" (click)="generate()" [disabled]="loading()">Recalculer depuis le profil enregistre</button>
        @if (error()) { <p role="alert">{{ error() }}</p> }
      </div></div>
      <div class="card">
        <div class="card-header"><span>{{ total() }} resultats</span></div>
        <div class="card-body" style="padding:0">
          <table class="data-table"><thead><tr><th>Programme</th><th>Eligibilite</th><th>Compatibilite</th><th>Couverture des donnees</th><th>Date</th><th>Actions</th></tr></thead><tbody>
          @for (r of recommendations(); track r.id) {
            <tr><td>{{ r.explanation.programName || r.programId }}</td>
              <td><span class="badge" [class]="eligClass(r.eligibilityStatus)">{{ r.eligibilityStatus }}</span>
                @if (r.stale) { <span class="badge badge-warning">Profil modifie</span> }
                @if (!r.sourceCurrent) { <span class="badge badge-warning">Guide inactif ou non determine</span> }
              </td>
              <td>{{ r.score === null ? 'Non calculee' : (r.score | number:'1.0-2') + ' /100' }}</td>
              <td>{{ r.dataCoverage | number:'1.0-2' }}%</td>
              <td>{{ r.computedAt | date:'short' }}</td>
              <td><button class="btn btn-ghost btn-icon" (click)="selected.set(r)" aria-label="Details" title="Details"><span class="material-symbols-rounded" aria-hidden="true">visibility</span></button></td>
            </tr>
          } @empty { <tr><td colspan="6">Aucun resultat pour ce candidat.</td></tr> }
          </tbody></table>
          <button class="btn btn-secondary" (click)="changePage(-1)" [disabled]="page === 0 || loading()">Precedent</button>
          <button class="btn btn-secondary" (click)="changePage(1)" [disabled]="(page + 1) * 20 >= total() || loading()">Suivant</button>
        </div>
      </div>
    </div>
    <app-modal [open]="selected() !== null" title="Diagnostic de recommandation" size="900px" [showFooter]="false" (close)="selected.set(null)">
      @if (selected(); as result) {
        <p>Calcul : {{ result.runId }}</p>
        <p>Annee : {{ result.explanation.academicYearId || 'Inconnue' }}</p>
        @for (warning of result.explanation.eligibility?.warnings || []; track $index) { <p>{{ warning }}</p> }
        <table class="data-table"><thead><tr><th>Critere</th><th>Attendu</th><th>Observe</th><th>Resultat</th><th>Source</th></tr></thead><tbody>
          @for (c of result.explanation.eligibility?.criteria || []; track $index) {
            <tr><td>{{ c.criterionName }}</td><td>{{ c.expectedValue }}</td><td>{{ c.actualValue ?? 'Manquant' }}</td>
              <td>{{ c.status }} : {{ c.message }}</td><td>{{ c.sourceFactId }}<br>{{ c.guideVersionId }}</td></tr>
          }
        </tbody></table>
      }
    </app-modal>
  `
})
export class RecommendationsComponent {
  candidateId = ''; page = 0;
  recommendations = signal<Diagnostic[]>([]); total = signal(0); loading = signal(false);
  error = signal(''); selected = signal<Diagnostic | null>(null);
  constructor(private api: ApiService, private toast: ToastService) {}
  private valid(): boolean {
    if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(this.candidateId)) return true;
    this.error.set('Indiquez un identifiant candidat valide.'); return false;
  }
  load(): void {
    if (!this.valid()) return;
    this.loading.set(true); this.error.set('');
    this.api.getRecommendations(this.page, 20, this.candidateId).subscribe({
      next: r => { this.recommendations.set(r.content); this.total.set(r.totalElements); this.loading.set(false); },
      error: e => { this.recommendations.set([]); this.total.set(0); this.loading.set(false); this.error.set(e.error?.message || 'Consultation refusee ou indisponible.'); }
    });
  }
  generate(): void {
    if (!this.valid()) return;
    this.loading.set(true); this.error.set('');
    this.api.generateRecommendations({candidateId: this.candidateId}).subscribe({
      next: () => { this.loading.set(false); this.page = 0; this.toast.success('Calcul enregistre.'); this.load(); },
      error: e => { this.loading.set(false); this.error.set(e.error?.message || 'Calcul impossible. Verifiez le profil confirme et ses sources.'); }
    });
  }
  changePage(delta: number): void { this.page += delta; this.load(); }
  eligClass(status: string): string {
    return status === 'ELIGIBLE' ? 'badge-success' : status === 'NOT_ELIGIBLE' ? 'badge-danger' : 'badge-warning';
  }
}
