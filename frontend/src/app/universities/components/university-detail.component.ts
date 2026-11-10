import {PaginationComponent} from '../../shared/components/pagination.component';
import { Component, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { ApiService } from "../../core/services/api.service";
@Component({
  selector: "app-university-detail",
  standalone: true,
  imports: [PaginationComponent,CommonModule, RouterLink],
  template: `<div class="anim-fade-up">
    <a routerLink="/universities" class="btn btn-secondary">Universités</a>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }
    @if (loading()) {
      <p role="status">Chargement…</p>
    }
    @if (uni(); as u) {
      <div class="page-header">
        <div>
          <h1>{{ u.name }}</h1>
          <p>
            {{ u.countryName }} · {{ u.cityName }} · {{ u.verificationStatus }}
          </p>
        </div>
      </div>
      <section class="card card-body">
        <h2>Informations</h2>
        <p>{{ u.description || "Description non renseignée." }}</p>
        <dl>
          <dt>Type</dt>
          <dd>{{ u.type || "Non renseigné" }}</dd>
          <dt>Statut</dt>
          <dd>{{ u.status }}</dd>
          <dt>Adresse</dt>
          <dd>{{ u.address || "Non renseignée" }}</dd>
          <dt>Courriel</dt>
          <dd>{{ u.email || "Non renseigné" }}</dd>
          <dt>Téléphone</dt>
          <dd>{{ u.phone || "Non renseigné" }}</dd>
          <dt>Site web</dt>
          <dd>{{ u.website || "Non renseigné" }}</dd>
        </dl>
      </section>
      <section class="card card-body" style="margin-top:20px">
        <h2>{{ total }} campus enregistrés</h2>
        @for (c of campuses(); track c.id) {
          <p>
            <strong>{{ c.name }}</strong> · {{ c.cityName }} ·
            {{ c.verificationStatus }}
          </p>
        } @empty {
          <p>Aucun campus enregistré.</p>
        }
        <app-pagination [page]="page" [total]="total" [loading]="loading()" (pageChange)="page=$event;loadCampuses()" />
      </section>
      <section class="card card-body" style="margin-top:20px">
        <h2>Sources publiées</h2>
        @for (s of sources(); track s.factId) {
          <blockquote>
            {{ s.passage }}
            <p>
              Page {{ s.pageNumber }} · guide v{{ s.guideVersionNumber }} ·
              {{ s.reviewStatus }}
            </p>
            <a [routerLink]="['/documents', s.documentId, 'review']"
              >Consulter</a
            >
          </blockquote>
        } @empty {
          <p>Aucune citation publiée pour cette fiche.</p>
        }
      </section>
    }
  </div>`,
})
export class UniversityDetailComponent implements OnInit {
  uni = signal<any>(null);
  campuses = signal<any[]>([]);
  sources = signal<any[]>([]);
  loading = signal(true);
  error = signal("");
  id = "";
  page = 0;
  total = 0;
  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
  ) {}
  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id") || "";
    this.api.getUniversity(this.id).subscribe({
      next: (u) => {
        this.uni.set(u);
        this.loading.set(false);
        this.loadCampuses();
        this.api
          .academicSources("universities", this.id)
          .subscribe({
            next: (s) => this.sources.set(s),
            error: () => this.error.set("Sources indisponibles."),
          });
      },
      error: (e) => {
        this.loading.set(false);
        this.error.set(e.error?.message || "Université indisponible.");
      },
    });
  }
  loadCampuses() {
    this.api
      .catalog("campuses", this.page, { universityId: this.id })
      .subscribe({
        next: (r) => {
          this.campuses.set(r.content);
          this.total = r.totalElements;
        },
        error: () => this.error.set("Campus indisponibles."),
      });
  }
}
