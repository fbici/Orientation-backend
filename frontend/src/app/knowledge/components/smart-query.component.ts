import { Component, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { ApiService } from "../../core/services/api.service";
@Component({
  selector: "app-smart-query",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `<div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>Recherche assistée</h1>
        <p>
          Réponses fondées sur les passages publiés. La recherche utilise le
          texte ; aucun modèle d'embeddings n'est configuré.
        </p>
      </div>
      <a routerLink="/knowledge" class="btn btn-secondary">Connaissances</a>
    </div>
    <form class="card card-body" (ngSubmit)="search()">
      <label class="form-label">Votre recherche</label
      ><input
        name="query"
        class="form-input"
        [(ngModel)]="query"
        required
      /><button class="btn btn-primary" [disabled]="busy() || !query.trim()">
        Rechercher
      </button>
    </form>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }
    @if (busy()) {
      <p role="status">Recherche en cours…</p>
    }
    @if (result()) {
      <article class="card card-body">
        <p style="white-space:pre-wrap">{{ result().response }}</p>
        @for (r of result().results || []; track r.id) {
          <h3>{{ r.name }}</h3>
          @for (s of r.metadata?.sources || []; track s.factId) {
            <blockquote>
              {{ s.passage }}
              <p>
                Page {{ s.pageNumber }} · guide v{{ s.guideVersionNumber }} ·
                {{ s.reviewStatus }}
              </p>
              <a [routerLink]="['/documents', s.documentId, 'review']"
                >Consulter la source</a
              >
            </blockquote>
          }
        }
      </article>
    }
  </div>`,
})
export class SmartQueryComponent {
  query = "";
  busy = signal(false);
  error = signal("");
  result = signal<any>(null);
  constructor(private api: ApiService) {}
  search() {
    this.busy.set(true);
    this.error.set("");
    this.result.set(null);
    this.api.smartQuery(this.query).subscribe({
      next: (r) => {
        this.result.set(r);
        this.busy.set(false);
      },
      error: (e) => {
        this.busy.set(false);
        this.error.set(e.error?.message || "Recherche indisponible.");
      },
    });
  }
}
