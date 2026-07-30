import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-smart-query',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px">
          <a routerLink="/knowledge" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a>
          <div><h1>Smart Query</h1><p>Testez le moteur de recherche intelligent de la plateforme</p></div>
        </div>
      </div>

      <!-- Query input -->
      <div class="card" style="margin-bottom:22px">
        <div class="card-body">
          <div style="display:flex;gap:12px">
            <div style="flex:1;position:relative">
              <span class="material-symbols-rounded" style="position:absolute;left:14px;top:50%;transform:translateY(-50%);font-size:20px;color:var(--n-400)">psychology</span>
              <input type="text" class="form-input" style="padding:14px 14px 14px 44px;font-size:.9375rem" placeholder="Posez une question en langage naturel…" [(ngModel)]="query" (keydown.enter)="ask()">
            </div>
            <button class="btn btn-primary btn-lg" (click)="ask()" [disabled]="loading() || !query">
              @if (loading()) { <span class="spinner-sm"></span> } @else { <span class="material-symbols-rounded">send</span> }
              Analyser
            </button>
          </div>
          <div style="display:flex;gap:8px;margin-top:12px;flex-wrap:wrap">
            @for (ex of examples; track ex) {
              <button class="btn btn-secondary btn-sm" (click)="query = ex; ask()">{{ ex }}</button>
            }
          </div>
        </div>
      </div>

      <!-- Results -->
      @if (loading()) {
        <div class="card"><div class="card-body" style="text-align:center;padding:48px"><div class="spinner"></div><p style="margin-top:12px;font-size:.8125rem;color:var(--n-500)">Analyse en cours…</p></div></div>
      }

      @if (result() && !loading()) {
        <!-- Answer -->
        <div class="card" style="margin-bottom:22px">
          <div class="card-header"><h3>Réponse</h3></div>
          <div class="card-body">
            <p style="font-size:.9375rem;color:var(--n-800);line-height:1.7;white-space:pre-wrap">{{ result().answer || result().response || 'Aucune réponse.' }}</p>
          </div>
        </div>

        <!-- Knowledge found -->
        @if (result().nodes?.length || result().knowledge?.length) {
          <div class="card" style="margin-bottom:22px">
            <div class="card-header"><h3>Connaissances trouvées</h3><span class="badge badge-primary">{{ (result().nodes || result().knowledge || []).length }}</span></div>
            <div class="card-body" style="padding:0">
              <table class="data-table">
                <thead><tr><th>Type</th><th>Nom</th><th>Pertinence</th></tr></thead>
                <tbody>
                  @for (n of result().nodes || result().knowledge || []; track n.id) {
                    <tr>
                      <td><span class="badge badge-gray">{{ n.type || n.nodeType || '—' }}</span></td>
                      <td style="font-weight:500">{{ n.name || n.label || '—' }}</td>
                      <td><div style="display:flex;align-items:center;gap:8px"><div class="progress" style="width:80px"><div class="progress-bar blue" [style.width.%]="(n.score || n.relevance || 0) * 100"></div></div><span style="font-size:.8125rem;font-weight:600">{{ ((n.score || n.relevance || 0) * 100) | number:'1.0-0' }}%</span></div></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        }

        <!-- Documents used -->
        @if (result().documents?.length) {
          <div class="card" style="margin-bottom:22px">
            <div class="card-header"><h3>Documents utilisés</h3><span class="badge badge-primary">{{ result().documents.length }}</span></div>
            <div class="card-body" style="padding:8px 24px">
              @for (doc of result().documents; track doc.id) {
                <div style="display:flex;align-items:center;gap:12px;padding:10px 0;border-bottom:1px solid var(--n-100)">
                  <span class="material-symbols-rounded" style="font-size:18px;color:var(--n-400)">description</span>
                  <div style="flex:1"><div style="font-size:.8125rem;font-weight:500;color:var(--n-800)">{{ doc.fileName || doc.name }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ doc.classification || '—' }}</div></div>
                </div>
              }
            </div>
          </div>
        }

        <!-- Recommendations -->
        @if (result().recommendations?.length) {
          <div class="card" style="margin-bottom:22px">
            <div class="card-header"><h3>Recommandations générées</h3></div>
            <div class="card-body" style="padding:0">
              <table class="data-table">
                <thead><tr><th>Programme</th><th>Université</th><th>Score</th><th>Justification</th></tr></thead>
                <tbody>
                  @for (r of result().recommendations; track r.programName) {
                    <tr>
                      <td style="font-weight:500">{{ r.programName || r.program }}</td>
                      <td>{{ r.universityName || r.university }}</td>
                      <td><span style="font-weight:700;color:var(--brand)">{{ r.score }}%</span></td>
                      <td style="font-size:.8125rem;color:var(--n-600)">{{ r.justification || r.explanation || '—' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        }
      }

      @if (error() && !loading()) {
        <div class="alert alert-error" style="margin-bottom:22px"><span class="material-symbols-rounded">error</span><span>{{ error() }}</span></div>
      }
    </div>
  `,
  styles: [`
    .spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    .spinner-sm{width:16px;height:16px;border:2px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
    .alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);font-size:.8125rem}
    .alert-error{background:var(--red-50);color:var(--red-600);border:1px solid rgba(239,68,68,.15)}
  `]
})
export class SmartQueryComponent {
  query = '';
  loading = signal(false);
  result = signal<any>(null);
  error = signal('');

  examples = [
    "Je suis un étudiant béninois avec 14 de moyenne en Sciences",
    "Quelles universités acceptent un Bac D ?",
    "Je cherche une bourse d'excellence en France",
    "Programmes d'informatique disponibles au Bénin",
  ];

  constructor(private api: ApiService) {}

  ask(): void {
    if (!this.query.trim()) return;
    this.loading.set(true);
    this.result.set(null);
    this.error.set('');

    this.api.smartQuery(this.query).subscribe({
      next: (r) => { this.result.set(r); this.loading.set(false); },
      error: (e) => { this.error.set(e.error?.message || 'Erreur lors de la requête.'); this.loading.set(false); }
    });
  }
}
