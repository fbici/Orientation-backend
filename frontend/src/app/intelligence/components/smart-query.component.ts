import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IntelligenceService, SmartQueryResult } from '../../core/services/intelligence.service';

@Component({
  selector: 'app-smart-query',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px">
          <a routerLink="/intelligence" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a>
          <div><h1>Smart Query</h1><p>Recherche intelligente en langage naturel</p></div>
        </div>
      </div>

      <!-- Query input -->
      <div class="card" style="margin-bottom:22px">
        <div class="card-body">
          <div style="display:flex;gap:12px">
            <div style="flex:1;position:relative">
              <span class="material-symbols-rounded" style="position:absolute;left:14px;top:50%;transform:translateY(-50%);font-size:20px;color:var(--n-400)">psychology</span>
              <input type="text" class="form-input" style="padding:14px 14px 14px 44px;font-size:.9375rem" placeholder="Posez une question en langage naturel..." [(ngModel)]="query" (keydown.enter)="ask()">
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

      @if (loading()) {
        <div class="card"><div class="card-body" style="text-align:center;padding:48px"><div class="spinner-lg"></div><p style="margin-top:12px;font-size:.8125rem;color:var(--n-500)">Analyse en cours...</p></div></div>
      }

      @if (result() && !loading()) {
        <!-- Answer -->
        <div class="card" style="margin-bottom:22px">
          <div class="card-header"><h3>Reponse</h3></div>
          <div class="card-body">
            <p style="font-size:.9375rem;color:var(--n-800);line-height:1.7;white-space:pre-wrap">{{ result()!.answer }}</p>
          </div>
        </div>

        <!-- Keywords -->
        @if (result()!.keywords.length) {
          <div class="card" style="margin-bottom:22px">
            <div class="card-header"><h3>Mots-cles identifies</h3></div>
            <div class="card-body">
              <div style="display:flex;flex-wrap:wrap;gap:6px">
                @for (kw of result()!.keywords; track kw) { <span class="badge badge-primary">{{ kw }}</span> }
              </div>
            </div>
          </div>
        }

        <!-- Knowledge found -->
        @if (result()!.knowledgeNodes.length) {
          <div class="card" style="margin-bottom:22px">
            <div class="card-header"><h3>Connaissances trouvees</h3><span class="badge badge-primary">{{ result()!.knowledgeNodes.length }}</span></div>
            <div class="card-body" style="padding:0">
              <table class="data-table">
                <thead><tr><th>Type</th><th>Nom</th><th>Source</th></tr></thead>
                <tbody>
                  @for (n of result()!.knowledgeNodes; track n.id) {
                    <tr>
                      <td><span class="badge" [class]="nodeTypeClass(n.type)">{{ n.type }}</span></td>
                      <td style="font-weight:500">{{ n.name }}</td>
                      <td style="font-size:.75rem;color:var(--n-500)">{{ n.source || '-' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        }

        <!-- Sources -->
        @if (result()!.sources.length) {
          <div class="card" style="margin-bottom:22px">
            <div class="card-header"><h3>Sources utilisees</h3></div>
            <div class="card-body" style="padding:8px 24px">
              @for (src of result()!.sources; track src) {
                <div style="display:flex;align-items:center;gap:10px;padding:8px 0;border-bottom:1px solid var(--n-100)">
                  <span class="material-symbols-rounded" style="font-size:16px;color:var(--n-400)">description</span>
                  <span style="font-size:.8125rem;color:var(--n-700)">{{ src }}</span>
                </div>
              }
            </div>
          </div>
        }

        <!-- Recommended programs -->
        @if (result()!.recommendedPrograms.length) {
          <div class="card">
            <div class="card-header"><h3>Programmes recommandes</h3></div>
            <div class="card-body" style="padding:8px 24px">
              @for (prog of result()!.recommendedPrograms; track prog) {
                <div style="display:flex;align-items:center;gap:10px;padding:10px 0;border-bottom:1px solid var(--n-100)">
                  <span class="material-symbols-rounded" style="font-size:18px;color:var(--brand)">workspace_premium</span>
                  <span style="font-size:.875rem;font-weight:500;color:var(--n-800)">{{ prog }}</span>
                </div>
              }
            </div>
          </div>
        }
      }

      @if (error() && !loading()) {
        <div class="alert alert-error"><span class="material-symbols-rounded">error</span><span>{{ error() }}</span></div>
      }
    </div>
  `,
  styles: [`
    .spinner-lg{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    .spinner-sm{width:16px;height:16px;border:2px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
    .alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);font-size:.8125rem}
    .alert-error{background:var(--red-50);color:var(--red-600);border:1px solid rgba(239,68,68,.15)}
  `]
})
export class SmartQueryComponent {
  query = '';
  loading = signal(false);
  result = signal<SmartQueryResult | null>(null);
  error = signal('');

  examples = [
    "Je suis un etudiant beninois avec 14 de moyenne en Sciences",
    "Quelles universites acceptent un Bac D ?",
    "Je cherche une bourse d'excellence en France",
    "Programmes d'informatique disponibles au Benin",
    "Je veux etudier l'IA au Canada",
  ];

  constructor(private intelSvc: IntelligenceService) {}

  ask(): void {
    if (!this.query.trim()) return;
    this.loading.set(true);
    this.result.set(null);
    this.error.set('');

    this.intelSvc.smartQuery(this.query).subscribe({
      next: (r) => { this.result.set(r); this.loading.set(false); },
      error: (e) => { this.error.set(e.error?.message || 'Erreur lors de la requete.'); this.loading.set(false); }
    });
  }

  nodeTypeClass(type: string): string {
    if (!type) return 'badge-gray';
    const t = type.toLowerCase();
    if (t.includes('university')) return 'badge-primary';
    if (t.includes('program')) return 'badge-success';
    if (t.includes('scholarship')) return 'badge-warning';
    if (t.includes('subject')) return 'badge-violet';
    return 'badge-gray';
  }
}
