import { Subscription } from "rxjs";
import { Component, OnInit, OnDestroy, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { ApiService } from "../../core/services/api.service";
import { AcademicFieldsComponent } from "../../catalog/academic-fields.component";
@Component({
  selector: "app-document-review",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AcademicFieldsComponent],
  template: `<div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>{{ review()?.title || "Revue documentaire" }}</h1>
        <p>
          Vérifiez chaque information dans sa page source avant de la valider.
        </p>
      </div>
      <a routerLink="/documents" class="btn btn-secondary">Documents</a>
    </div>
    @if (error()) {
      <div role="alert" class="alert alert-error">{{ error() }}</div>
    }
    @if (message()) {
      <p role="status">{{ message() }}</p>
    }
    @if (busy()) {
      <p role="status">Opération en cours…</p>
    }
    @if (review(); as r) {
      <div class="card card-body">
        <p>
          État : <strong>{{ r.status }}</strong> · Source version
          {{ r.sourceVersion }} · {{ r.pageCount || 0 }} page(s)
        </p>
        <small>Empreinte : {{ r.checksum }}</small>
        @if (r.processingError) {
          <p role="alert">{{ r.processingError }}</p>
        }
        @if (r.status === "UPLOADED" || r.status === "FAILED") {
          <button
            class="btn btn-primary"
            [disabled]="busy()"
            (click)="process()"
          >
            Extraire le document
          </button>
        }
      </div>
      <div class="g2" style="align-items:start;margin-top:20px">
        <section class="card card-body">
          <h2>Page source</h2>
          <select
            class="form-input"
            aria-label="Page source"
            [(ngModel)]="page"
            (ngModelChange)="preview()"
          >
            @for (p of r.pages; track p.pageNumber) {
              <option [ngValue]="p.pageNumber">Page {{ p.pageNumber }}</option>
            }
          </select>
          @if (previewUrl) {
            <img
              [src]="previewUrl"
              alt="Aperçu de la page source"
              style="width:100%;margin-top:12px"
            />
          }
          <details>
            <summary>Texte extrait de la page {{ page }}</summary>
            <pre style="white-space:pre-wrap">{{ pageText() }}</pre>
          </details>
          @if (r.pages?.length) {
            <hr />
            <h3>Ajouter une information omise</h3>
            <label class="form-label"
              >Passage exact de la page sélectionnée</label
            ><textarea class="form-input" [(ngModel)]="newPassage"></textarea>
            <label class="form-label">Type</label
            ><select class="form-input" [(ngModel)]="newKind">
              @for (k of kinds; track k) {
                <option [value]="k">{{ k }}</option>
              }
            </select>
            <button
              class="btn btn-secondary"
              [disabled]="busy() || !newPassage.trim()"
              (click)="add()"
            >
              Ajouter à la revue
            </button>
          }
        </section>
        <section>
          <div class="card card-body">
            <h2>Informations extraites</h2>
            <p>
              Une validation confirme le sens du passage et les rattachements
              choisis. Le score OCR indique une qualité technique de lecture.
            </p>
            @for (f of r.facts; track f.id) {
              <button
                class="btn btn-ghost"
                style="display:block;width:100%;text-align:left"
                (click)="select(f)"
              >
                {{ f.values.name || f.kind }} · p. {{ f.pageNumber }} ·
                {{ f.status }} {{ f.publishedAt ? "· publié" : "" }}
              </button>
            } @empty {
              <p>
                Aucune information détectée. Vous pouvez en ajouter à partir
                d'un passage exact.
              </p>
            }
          </div>
          @if (selected) {
            <div class="card card-body" style="margin-top:16px">
              <h3>Vérification · {{ selected.kind }}</h3>
              <blockquote style="white-space:pre-wrap">
                {{ selected.passage }}
              </blockquote>
              <p>
                Page {{ selected.pageNumber }} · source version
                {{ selected.sourceVersion }} · {{ selected.status }}
              </p>
              @if (selected.publishedAt) {
                <p>
                  Information publiée et immuable. Pour corriger la donnée
                  officielle, importez une nouvelle version du guide.
                </p>
              } @else {
                <label class="form-label">Nature de l'information</label
                ><select class="form-input" [(ngModel)]="editKind">
                  @for (k of kinds; track k) {
                    <option [value]="k">{{ k }}</option>
                  }
                </select>
                <app-academic-fields
                  [kind]="editKind"
                  [values]="values"
                  [drafts]="r.facts"
                  [allowExistingTarget]="true"
                ></app-academic-fields>
                <label class="form-label">Note de revue</label
                ><textarea class="form-input" [(ngModel)]="note"></textarea>
                <div class="page-header-actions">
                  <button
                    class="btn btn-secondary"
                    [disabled]="busy()"
                    (click)="decide('TO_REVIEW')"
                  >
                    Enregistrer le brouillon</button
                  ><button
                    class="btn btn-ghost"
                    [disabled]="busy()"
                    (click)="decide('REJECTED')"
                  >
                    Rejeter</button
                  ><button
                    class="btn btn-primary"
                    [disabled]="busy()"
                    (click)="decide('VALIDATED')"
                  >
                    Valider l'information
                  </button>
                </div>
              }
              <details>
                <summary>Valeurs initialement extraites</summary>
                <pre style="white-space:pre-wrap">{{
                  selected.originalValues | json
                }}</pre>
              </details>
            </div>
          }
        </section>
      </div>
      <section class="card card-body" style="margin-top:24px">
        <h2>Publication des informations validées</h2>
        <p>
          Seuls les éléments cochés, validés et leurs références publiées seront
          enregistrés dans le catalogue officiel de ce tenant.
        </p>
        <div class="g2">
          <div>
            <label class="form-label">Pays *</label
            ><select class="form-input" [(ngModel)]="publication.countryId">
              <option value="">Choisir</option>
              @for (c of countries; track c.id) {
                <option [value]="c.id">{{ c.name }}</option>
              }
            </select>
          </div>
          <div>
            <label class="form-label">Année académique *</label
            ><select
              class="form-input"
              [(ngModel)]="publication.academicYearId"
            >
              <option value="">Choisir</option>
              @for (y of years; track y.id) {
                <option [value]="y.id">{{ y.label }}</option>
              }
            </select>
          </div>
          <div>
            <label class="form-label">Titre du guide *</label
            ><input class="form-input" [(ngModel)]="publication.guideTitle" />
          </div>
          <div>
            <label class="form-label">Organisme éditeur</label
            ><input class="form-input" [(ngModel)]="publication.publisher" />
          </div>
          <div>
            <label class="form-label">Début de validité *</label
            ><input
              type="date"
              class="form-input"
              [(ngModel)]="publication.effectiveDate"
            />
          </div>
          <div>
            <label class="form-label">Fin de validité</label
            ><input
              type="date"
              class="form-input"
              [(ngModel)]="publication.expiryDate"
            />
          </div>
        </div>
        @for (f of r.facts; track f.id) {
          @if (f.status === "VALIDATED" && !f.publishedAt) {
            <label style="display:block"
              ><input
                type="checkbox"
                [checked]="chosen.has(f.id)"
                (change)="toggle(f.id, $any($event.target).checked)"
              />
              {{ f.values.name || f.kind }} · p. {{ f.pageNumber }}</label
            >
          }
        }
        <button
          class="btn btn-primary"
          style="margin-top:16px"
          [disabled]="busy() || chosen.size === 0"
          (click)="publish()"
        >
          Publier {{ chosen.size }} information(s)
        </button>
      </section>
    }
  </div>`,
})
export class DocumentReviewComponent implements OnInit, OnDestroy {
  id = "";
  review = signal<any>(null);
  busy = signal(false);
  error = signal("");
  message = signal("");
  selected: any;
  values: Record<string, any> = {};
  editKind = "";
  note = "";
  page = 1;
  previewUrl = "";
  newPassage = "";
  newKind = "ACADEMIC_ATTRIBUTE";
  countries: any[] = [];
  years: any[] = [];
  chosen = new Set<string>();
  publication = {
    countryId: "",
    academicYearId: "",
    guideTitle: "",
    effectiveDate: "",
    expiryDate: "",
    publisher: "",
  };
  kinds = [
    "UNIVERSITY",
    "CAMPUS",
    "FACULTY",
    "PROGRAM",
    "SCHOLARSHIP",
    "ADMISSION_CRITERION",
    "SCHOLARSHIP_CRITERION",
    "COUNTRY",
    "ACADEMIC_YEAR",
    "BAC_REQUIREMENT",
    "DIPLOMA_REQUIREMENT",
    "SUBJECT_REQUIREMENT",
    "DOCUMENT_REQUIREMENT",
    "FEE",
    "DATE",
    "ACADEMIC_ATTRIBUTE",
  ];
  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
  ) {}
  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;
    this.load();
    this.api.getCountries().subscribe({
      next: (r) => (this.countries = r.content || r),
      error: () => this.error.set("Pays indisponibles."),
    });
    this.api.reference("academic-years").subscribe({
      next: (r) => (this.years = r),
      error: () => this.error.set("Années académiques indisponibles."),
    });
  }
  private previewRequest?: Subscription;
  ngOnDestroy() {
    this.previewRequest?.unsubscribe();
    if (this.previewUrl) URL.revokeObjectURL(this.previewUrl);
  }
  fail(e: any) {
    this.busy.set(false);
    this.error.set(
      e.error?.message ||
        e.error?.detail ||
        "Opération refusée. Vérifiez les références et vos permissions.",
    );
  }
  load() {
    this.api.reviewDocument(this.id).subscribe({
      next: (r) => {
        this.review.set(r);
        this.publication.guideTitle ||= r.title;
        this.chosen = new Set(
          r.facts
            .filter((f: any) => f.status === "VALIDATED" && !f.publishedAt)
            .map((f: any) => f.id),
        );
        this.preview();
      },
      error: (e) => this.fail(e),
    });
  }
  process() {
    this.busy.set(true);
    this.error.set("");
    this.api.processDocument(this.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.message.set(
          "Extraction terminée. Les informations attendent votre revue.",
        );
        this.load();
      },
      error: (e) => {
        this.fail(e);
        this.load();
      },
    });
  }
  select(f: any) {
    this.selected = f;
    this.values = structuredClone(f.values);
    this.editKind = f.kind;
    this.note = f.note || "";
    this.page = f.pageNumber;
    this.preview();
    this.error.set("");
  }
  preview() {
    this.previewRequest?.unsubscribe();
    if (!this.review()?.pages?.length) return;
    this.previewRequest = this.api.documentPage(this.id, this.page).subscribe({
      next: (b) => {
        if (this.previewUrl) URL.revokeObjectURL(this.previewUrl);
        this.previewUrl = URL.createObjectURL(b);
      },
      error: () =>
        this.error.set(
          "Aperçu indisponible. Le texte source reste consultable.",
        ),
    });
  }
  pageText() {
    return (
      this.review()?.pages?.find((p: any) => p.pageNumber === this.page)
        ?.text || ""
    );
  }
  decide(status: string) {
    this.busy.set(true);
    this.error.set("");
    this.api
      .reviewFact(this.id, this.selected.id, {
        version: this.selected.version,
        status,
        values: this.values,
        kind: this.editKind,
        note: this.note,
      })
      .subscribe({
        next: (f) => {
          this.busy.set(false);
          this.selected = f;
          this.message.set("Décision enregistrée : " + f.status);
          this.load();
        },
        error: (e) => this.fail(e),
      });
  }
  add() {
    this.busy.set(true);
    this.error.set("");
    this.api
      .addSourceFact(this.id, {
        kind: this.newKind,
        pageNumber: this.page,
        passage: this.newPassage,
        values: { description: this.newPassage },
      })
      .subscribe({
        next: (f) => {
          this.busy.set(false);
          this.newPassage = "";
          this.select(f);
          this.load();
        },
        error: (e) => this.fail(e),
      });
  }
  toggle(id: string, yes: boolean) {
    yes ? this.chosen.add(id) : this.chosen.delete(id);
  }
  publish() {
    if (
      !this.publication.countryId ||
      !this.publication.academicYearId ||
      !this.publication.guideTitle?.trim() ||
      !this.publication.effectiveDate
    ) {
      this.error.set(
        "Renseignez le pays, l’année, le titre et la date de validité.",
      );
      return;
    }
    this.busy.set(true);
    this.error.set("");
    const data = {
      ...this.publication,
      expiryDate: this.publication.expiryDate || null,
      factIds: [...this.chosen],
    };
    this.api.publishDocument(this.id, data).subscribe({
      next: (r) => {
        this.busy.set(false);
        this.message.set(
          r.publishedCount +
            " information(s) publiée(s), version " +
            r.guideVersionNumber +
            ".",
        );
        this.load();
      },
      error: (e) => this.fail(e),
    });
  }
}
