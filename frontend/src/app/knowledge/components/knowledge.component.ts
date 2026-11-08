import { Component, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { ApiService } from "../../core/services/api.service";
@Component({
  selector: "app-knowledge",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `<div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>Base de connaissances</h1>
        <p>Recherche textuelle dans les données publiées et validées.</p>
      </div>
      <a routerLink="/knowledge/smart-query" class="btn btn-secondary"
        >Recherche assistée</a
      >
    </div>
    <form class="card card-body" (ngSubmit)="page = 0; search()">
      <div class="g3">
        <input
          class="form-input"
          name="query"
          aria-label="Recherche"
          [(ngModel)]="query"
          placeholder="Formation, université, condition…"
        /><select
          class="form-input"
          name="type"
          [(ngModel)]="type"
          aria-label="Type"
        >
          <option value="">Tous les types</option>
          @for (t of types; track t) {
            <option [value]="t">{{ t }}</option>
          }
        </select>
        @for (f of filterFields; track f.key) {
          <select
            class="form-input"
            [name]="f.key"
            [attr.aria-label]="f.label"
            [(ngModel)]="filters[f.key]"
          >
            <option value="">{{ f.label }}</option>
            @for (o of options[f.key] || []; track o.id) {
              <option [value]="o.id">{{ o.name || o.label }}</option>
            }
          </select>
        }
        <button class="btn btn-primary" [disabled]="busy()">Rechercher</button>
      </div>
    </form>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }
    @if (busy()) {
      <p role="status">Recherche en cours…</p>
    }
    @for (r of results(); track r.indexId) {
      <article class="card card-body" style="margin-top:16px">
        <h2>{{ r.name }}</h2>
        <span class="badge badge-gray">{{ r.type }}</span>
        <p>{{ r.content }}</p>
        @for (s of r.metadata?.sources || []; track s.factId) {
          <blockquote>
            <p>{{ s.passage }}</p>
            <footer>
              Page {{ s.pageNumber }} · source v{{ s.sourceVersion }} · guide
              v{{ s.guideVersionNumber }} · {{ s.reviewStatus }}
            </footer>
            <a [routerLink]="['/documents', s.documentId, 'review']"
              >Consulter la source</a
            >
          </blockquote>
        }
      </article>
    }
    @if (searched && !busy() && !results().length) {
      <div class="card card-body">
        Aucun résultat publié pour cette recherche.
      </div>
    }
    @if (searched) {
      <div class="page-header-actions">
        <button
          class="btn btn-secondary"
          [disabled]="page === 0 || busy()"
          (click)="page = page - 1; search()"
        >
          Précédent</button
        ><span
          >Page {{ page + 1 }} · {{ results().length }} résultats affichés</span
        ><button
          class="btn btn-secondary"
          [disabled]="results().length < 20 || busy()"
          (click)="page = page + 1; search()"
        >
          Suivant
        </button>
      </div>
    }
    <section class="card card-body" style="margin-top:24px">
      <h2>Graphe publié</h2>
      <button class="btn btn-secondary" (click)="loadGraph()">
        Charger le graphe
      </button>
      @if (graph()) {
        <p>
          {{ graph().totalNodes }} nœuds ·
          {{ graph().edges?.length || graph().relations?.length || 0 }}
          relations affichées {{ graph().truncated ? "(vue limitée)" : "" }}
        </p>
        <ul>
          @for (n of graph().nodes; track n.id) {
            <li>{{ n.name || n.label }} · {{ n.type }}</li>
          }
        </ul>
      }
    </section>
  </div>`,
})
export class KnowledgeComponent implements OnInit {
  page = 0;
  filters: Record<string, string> = {};
  options: Record<string, any[]> = {};
  filterFields = [
    {
      key: "countryId",
      label: "Tous les pays",
      resource: "locations/countries",
    },
    { key: "programId", label: "Toutes les formations", resource: "programs" },
    {
      key: "universityId",
      label: "Toutes les universités",
      resource: "universities",
    },
    {
      key: "academicYearId",
      label: "Toutes les années",
      resource: "academic-years",
    },
    { key: "sourceId", label: "Toutes les sources", resource: "guides" },
  ];
  query = "";
  type = "";
  types = [
    "UNIVERSITY",
    "CAMPUS",
    "FACULTY",
    "PROGRAM",
    "SCHOLARSHIP",
    "ADMISSION_CRITERION",
    "ACADEMIC_FACT",
  ];
  results = signal<any[]>([]);
  graph = signal<any>(null);
  busy = signal(false);
  error = signal("");
  searched = false;
  constructor(private api: ApiService) {}
  ngOnInit() {
    for (const f of this.filterFields)
      this.api.reference(f.resource).subscribe({
        next: (r) => {
          const items = Array.isArray(r) ? r : r.content;
          this.options[f.key] =
            f.key === "sourceId"
              ? items.flatMap((g: any) =>
                  (g.versions || []).map((v: any) => ({
                    id: v.documentId,
                    name: g.title + " · v" + v.versionNumber,
                  })),
                )
              : items;
        },
        error: () =>
          this.error.set(
            "Certains filtres sont indisponibles. Réessayez le chargement.",
          ),
      });
  }
  search() {
    this.busy.set(true);
    this.error.set("");
    this.searched = true;
    this.api
      .knowledgeSearch(this.query, {
        ...this.filters,
        ...(this.type ? { type: this.type } : {}),
        page: String(this.page),
      })
      .subscribe({
        next: (r) => {
          this.results.set(r);
          this.busy.set(false);
        },
        error: (e) => {
          this.busy.set(false);
          this.error.set(e.error?.message || "Recherche indisponible.");
        },
      });
  }
  loadGraph() {
    this.api.knowledgeGraph().subscribe({
      next: (g) => this.graph.set(g),
      error: () => this.error.set("Graphe indisponible."),
    });
  }
}
