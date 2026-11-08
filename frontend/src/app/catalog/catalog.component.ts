import { Component, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { ApiService } from "../core/services/api.service";
import {
  AcademicFieldsComponent,
  ACADEMIC_FIELDS,
} from "./academic-fields.component";
import { ModalComponent } from "../shared/components/modal.component";
import { ConfirmDialogComponent } from "../shared/components/confirm-dialog.component";
@Component({
  selector: "app-academic-catalog",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    AcademicFieldsComponent,
    ModalComponent,
    ConfirmDialogComponent,
  ],
  template: `<div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>{{ title }}</h1>
        <p>
          Catalogue académique · Les nouvelles données restent à revoir avant
          publication.
        </p>
      </div>
      <button class="btn btn-primary" (click)="edit()">Créer</button>
    </div>
    <form class="card card-body" (ngSubmit)="page = 0; load()">
      <div class="g3">
        <input
          class="form-input"
          name="search"
          aria-label="Rechercher"
          placeholder="Rechercher par nom"
          [(ngModel)]="search"
        />
        <select
          class="form-input"
          name="country"
          aria-label="Pays"
          [(ngModel)]="countryId"
        >
          <option value="">Tous les pays</option>
          @for (c of countries; track c.id) {
            <option [value]="c.id">{{ c.name }}</option>
          }
        </select>
        @if (typeOptions.length) {
          <select
            class="form-input"
            name="type"
            aria-label="Type"
            [(ngModel)]="type"
          >
            <option value="">Tous les types</option>
            @for (t of typeOptions; track t) {
              <option [value]="t">{{ t }}</option>
            }
          </select>
        }
        @if (resource === "programs" || resource === "scholarships") {
          <select
            class="form-input"
            name="year"
            aria-label="Année académique"
            [(ngModel)]="yearId"
          >
            <option value="">Toutes les années</option>
            @for (y of years; track y.id) {
              <option [value]="y.id">{{ y.label }}</option>
            }
          </select>
        }
        @if (resource === "programs") {
          <input
            class="form-input"
            name="language"
            aria-label="Langue"
            placeholder="Langue"
            [(ngModel)]="language"
          /><input
            class="form-input"
            name="studyField"
            aria-label="Domaine"
            placeholder="Domaine"
            [(ngModel)]="studyField"
          />
        }
        @if (resource !== "scholarships") {
          <select
            class="form-input"
            name="university"
            aria-label="Université"
            [(ngModel)]="universityId"
          >
            <option value="">Toutes les universités</option>
            @for (u of universities; track u.id) {
              <option [value]="u.id">{{ u.name }}</option>
            }
          </select>
        }
        @if (resource === "scholarships") {
          <select
            class="form-input"
            name="status"
            aria-label="Statut"
            [(ngModel)]="status"
          >
            <option value="">Tous les statuts</option>
            @for (t of ["ACTIVE", "CLOSED", "EXPIRED", "SUSPENDED"]; track t) {
              <option [value]="t">{{ t }}</option>
            }
          </select>
        }
        <button class="btn btn-secondary">Rechercher</button>
      </div>
    </form>
    @if (error()) {
      <div role="alert" class="alert alert-error">{{ error() }}</div>
    }
    @if (loading()) {
      <p role="status">Chargement du catalogue…</p>
    } @else {
      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Rattachement</th>
              <th>Validation</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            @for (r of rows(); track r.id) {
              <tr>
                <td>
                  {{ r.name }}<br /><small
                    >{{ r.type }} {{ r.language }} {{ r.academicYear }}</small
                  >
                </td>
                <td>
                  {{ r.universityName || r.countryName || r.campusName || "—" }}
                </td>
                <td>
                  <span class="badge badge-gray">{{
                    r.verificationStatus
                  }}</span>
                  @if (resource === "scholarships") {
                    <p>
                      Candidatures :
                      {{
                        r.openForApplications === null
                          ? "validité inconnue"
                          : r.openForApplications
                            ? "ouvertes"
                            : "fermées"
                      }}
                    </p>
                  }
                </td>
                <td>
                  <button class="btn btn-secondary btn-sm" (click)="edit(r)">
                    Modifier
                  </button>
                  <button class="btn btn-ghost btn-sm" (click)="source(r)">
                    Sources
                  </button>
                  <button class="btn btn-ghost btn-sm" (click)="toDisable = r">
                    Désactiver
                  </button>
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="4">Aucun résultat pour ces filtres.</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
    <div class="page-header-actions">
      <button
        class="btn btn-secondary"
        [disabled]="page === 0 || loading()"
        (click)="page = page - 1; load()"
      >
        Précédent</button
      ><span>Page {{ page + 1 }} · {{ total }} éléments</span
      ><button
        class="btn btn-secondary"
        [disabled]="(page + 1) * 20 >= total || loading()"
        (click)="page = page + 1; load()"
      >
        Suivant
      </button>
    </div>
    <p>
      <a routerLink="/documents"
        >Valider et publier depuis un document source</a
      >
    </p>
    <app-modal
      [open]="formOpen"
      [title]="editId ? 'Modifier ' + title : 'Créer ' + title"
      size="800px"
      [confirmLoading]="saving()"
      (close)="formOpen = false"
      (confirm)="save()"
    >
      <p>
        Les champs marqués * sont obligatoires. Une modification repasse en
        revue documentaire.
      </p>
      <app-academic-fields [kind]="kind" [values]="form"></app-academic-fields>
      @if (formError()) {
        <p role="alert">{{ formError() }}</p>
      }
    </app-modal>
    <app-modal
      [open]="sourceOpen"
      title="Sources publiées"
      [showFooter]="false"
      (close)="sourceOpen = false"
    >
      @for (s of sources; track s.factId) {
        <blockquote>
          {{ s.passage }}
          <p>
            Page {{ s.pageNumber }} · source v{{ s.sourceVersion }} · guide v{{
              s.guideVersionNumber
            }}
            · {{ s.reviewStatus }}
          </p>
          <a [routerLink]="['/documents', s.documentId, 'review']"
            >Consulter la page source</a
          >
        </blockquote>
      } @empty {
        <p>Aucune citation publiée disponible.</p>
      }
    </app-modal>
    <app-confirm-dialog
      [open]="!!toDisable"
      title="Désactiver"
      [message]="'Désactiver ' + (toDisable?.name || '') + ' ?'"
      [loading]="saving()"
      (close)="toDisable = null"
      (confirm)="disable()"
    ></app-confirm-dialog>
  </div>`,
})
export class CatalogComponent implements OnInit {
  resource = "programs";
  kind = "PROGRAM";
  title = "Formations";
  page = 0;
  total = 0;
  search = "";
  countryId = "";
  countries: any[] = [];
  type = "";
  yearId = "";
  language = "";
  studyField = "";
  universityId = "";
  status = "";
  years: any[] = [];
  universities: any[] = [];
  sources: any[] = [];
  sourceOpen = false;
  get typeOptions() {
    return (
      ACADEMIC_FIELDS[this.kind]?.find((f) => f.key === "type")?.options || []
    );
  }
  rows = signal<any[]>([]);
  loading = signal(false);
  saving = signal(false);
  error = signal("");
  formError = signal("");
  formOpen = false;
  editId?: string;
  form: Record<string, any> = {};
  toDisable: any;
  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
  ) {}
  ngOnInit() {
    this.route.data.subscribe((d) => {
      this.resource = d["resource"];
      this.kind = d["kind"];
      this.title = d["title"];
      this.page = 0;
      this.load();
    });
    this.api
      .reference("academic-years")
      .subscribe({
        next: (r) => (this.years = r),
        error: () => this.error.set("Années indisponibles."),
      });
    this.api
      .reference("universities")
      .subscribe({
        next: (r) => (this.universities = r.content),
        error: () => this.error.set("Universités indisponibles."),
      });
    this.api
      .getCountries()
      .subscribe({
        next: (r) => (this.countries = r.content || r),
        error: () => this.error.set("Impossible de charger les pays."),
      });
  }
  load() {
    this.loading.set(true);
    this.error.set("");
    this.api
      .catalog(this.resource, this.page, {
        search: this.search,
        countryId: this.countryId,
        type: this.type,
        academicYearId: this.yearId,
        language: this.language,
        studyField: this.studyField,
        universityId: this.universityId,
        status: this.status,
      })
      .subscribe({
        next: (r) => {
          this.rows.set(r.content);
          this.total = r.totalElements;
          this.loading.set(false);
        },
        error: (e) => {
          this.loading.set(false);
          this.error.set(
            e.error?.message || "Catalogue indisponible. Réessayez.",
          );
        },
      });
  }
  source(row: any) {
    this.sources = [];
    this.api.academicSources(this.resource, row.id).subscribe({
      next: (r) => {
        this.sources = r;
        this.sourceOpen = true;
      },
      error: (e) =>
        this.error.set(e.error?.message || "Sources indisponibles."),
    });
  }
  edit(row?: any) {
    this.editId = row?.id;
    this.form = row ? { ...row } : {};
    this.formError.set("");
    this.formOpen = true;
  }
  save() {
    if (!this.form["name"]?.trim()) {
      this.formError.set("Renseignez le nom.");
      return;
    }
    this.saving.set(true);
    this.api.saveCatalog(this.resource, this.editId, this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen = false;
        this.load();
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(
          e.error?.message ||
            e.error?.detail ||
            "Vérifiez les champs et les références.",
        );
      },
    });
  }
  disable() {
    this.saving.set(true);
    this.api.disableCatalog(this.resource, this.toDisable.id).subscribe({
      next: () => {
        this.toDisable = null;
        this.saving.set(false);
        this.load();
      },
      error: (e) => {
        this.saving.set(false);
        this.error.set(e.error?.message || "Désactivation refusée.");
      },
    });
  }
}
