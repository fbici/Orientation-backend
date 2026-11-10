import {PaginationComponent} from '../shared/components/pagination.component';
import { Component, OnInit, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { RouterLink } from "@angular/router";
import { ApiService } from "../core/services/api.service";
@Component({
  selector: "app-guides",
  standalone: true,
  imports: [PaginationComponent,CommonModule, FormsModule, RouterLink],
  template: `<div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>Guides publiés</h1>
        <p>Versions issues de sources revues et validées.</p>
      </div>
      <a class="btn btn-primary" routerLink="/documents">Importer un guide</a>
    </div>
    <form class="card card-body" (ngSubmit)="page = 0; load()">
      <div class="g3">
        <input
          class="form-input"
          name="search"
          aria-label="Rechercher un guide"
          [(ngModel)]="search"
          placeholder="Rechercher un guide"
        /><select
          class="form-input"
          name="country"
          aria-label="Pays"
          [(ngModel)]="countryId"
        >
          <option value="">Tous les pays</option>
          @for (c of countries; track c.id) {
            <option [value]="c.id">{{ c.name }}</option>
          }</select
        ><select
          class="form-input"
          name="year"
          aria-label="Année"
          [(ngModel)]="yearId"
        >
          <option value="">Toutes les années</option>
          @for (y of years; track y.id) {
            <option [value]="y.id">{{ y.label }}</option>
          }</select
        ><button class="btn btn-secondary">Rechercher</button>
      </div>
    </form>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }
    @if (loading()) {
      <p role="status">Chargement…</p>
    }
    @for (g of rows(); track g.id) {
      <article class="card card-body" style="margin-bottom:16px">
        <h2>{{ g.title }}</h2>
        <p>
          {{ g.country?.name }} · {{ g.academicYear?.label }} ·
          {{ g.publisher || "Éditeur non renseigné" }}
        </p>
        @for (v of g.versions; track v.id) {
          <div style="margin-top:12px">
            <strong>Version {{ v.versionNumber }} · {{ v.status }}</strong>
            <p>
              Validité : {{ v.effectiveDate }} —
              {{ v.expiryDate || "fin non précisée" }}
            </p>
            <small>Empreinte source : {{ v.checksum }}</small>
            <p>
              <a [routerLink]="['/documents', v.documentId, 'review']"
                >Consulter la source et sa revue</a
              >
            </p>
          </div>
        }
      </article>
    } @empty {
      @if (!loading()) {
        <div class="card card-body">
          Aucun guide publié dans votre périmètre.
        </div>
      }
    }
    <app-pagination [page]="page" [total]="total" [loading]="loading()" (pageChange)="page=$event;load()" />
  </div>`,
})
export class GuidesComponent implements OnInit {
  rows = signal<any[]>([]);
  loading = signal(false);
  error = signal("");
  page = 0;
  total = 0;
  search = "";
  countryId = "";
  yearId = "";
  countries: any[] = [];
  years: any[] = [];
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.load();
    this.api
      .getCountries()
      .subscribe({
        next: (r) => (this.countries = r),
        error: () => this.error.set("Pays indisponibles."),
      });
    this.api
      .reference("academic-years")
      .subscribe({
        next: (r) => (this.years = r),
        error: () => this.error.set("Années indisponibles."),
      });
  }
  load() {
    this.loading.set(true);
    this.error.set("");
    this.api
      .catalog("guides", this.page, {
        search: this.search,
        countryId: this.countryId,
        academicYearId: this.yearId,
      })
      .subscribe({
        next: (r) => {
          this.rows.set(r.content);
          this.total = r.totalElements;
          this.loading.set(false);
        },
        error: (e) => {
          this.loading.set(false);
          this.error.set(e.error?.message || "Guides indisponibles.");
        },
      });
  }
}
