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
        @if (r.status === "READY_REVIEW") {
          <div role="status" class="alert alert-info">
            <strong>Extraction terminée — validation nécessaire</strong>
            <p>Les informations extraites ne sont pas encore publiées dans le
              catalogue de l’application. Vérifiez les passages sources et les
              rattachements, validez les informations, puis utilisez la section
              « Publication des informations validées » ci-dessous.</p>
          </div>
        }
        @if (r.status === "VALIDATED") {
          <div role="status" class="alert alert-info">
            <strong>Revue terminée — publication nécessaire</strong>
            <p>La validation seule ne publie pas les informations. Sélectionnez
              les éléments à publier et leur année académique dans la section
              de publication ci-dessous.</p>
          </div>
        }
        @if (r.status === "PARTIALLY_PUBLISHED") {
          <div role="status" class="alert alert-info">
            <strong>Publication partielle</strong>
            <p>Seules les informations marquées « publié » ont rejoint le
              catalogue. Les autres restent à vérifier ou à publier.</p>
          </div>
        }
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
        <section id="review-facts">
          <div class="card card-body">
            <h2>Informations extraites</h2>
            <p>
              Une validation confirme le sens du passage et les rattachements
              choisis. Le score OCR indique une qualité technique de lecture.
            </p>
            <div role="group" aria-label="Informations à vérifier" style="max-height:320px;overflow-y:auto">
            @for (f of r.facts; track f.id) {
              <button
                class="btn btn-ghost"
                style="display:block;width:100%;text-align:left"
                [attr.aria-pressed]="selected?.id === f.id"
                [disabled]="busy()"
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
          </div>
          @if (selected) {
            <div id="fact-editor" tabindex="-1" class="card card-body" style="margin-top:16px;scroll-margin-top:90px">
              @if (error()) { <div role="alert" class="alert alert-error">{{ error() }}</div> }
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
      <section class="card card-body" style="margin-top:24px" id="publication">
        <p role="status">{{ publishableFacts().length }} information(s) validée(s) à publier · {{ chosen.size }} sélectionnée(s)</p>
        @if (publicationIssue()) {
          <div class="alert alert-info" id="publication-help" role="status">{{ publicationIssue() }}</div>
        }
        @if (publishableFacts().length === 0) {
          <button class="btn btn-secondary" [disabled]="busy()" (click)="reviewNext()">Vérifier une information extraite</button>
        } @else {
          <div class="page-header-actions">
            <button class="btn btn-secondary" [disabled]="busy()" (click)="chooseAll()">Sélectionner les informations validées</button>
            <button class="btn btn-ghost" [disabled]="busy()" (click)="chosen.clear()">Tout désélectionner</button>
          </div>
        }
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
          [disabled]="busy() || !!publicationIssue()"
          aria-describedby="publication-help"
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
  private editorScroll?: ReturnType<typeof setTimeout>;
  ngOnDestroy() {
    clearTimeout(this.editorScroll);
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
        const eligible = new Set(this.publishableFacts().map((f: any) => f.id));
        this.chosen = new Set([...this.chosen].filter(id => eligible.has(id)));
        this.preview();
      },
      error: (e) => this.fail(e),
    });
  }
  process() {
    if (this.busy()) return;
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
    if (this.busy()) return;
    this.selected = f;
    this.values = structuredClone(f.values || {});
    clearTimeout(this.editorScroll);
    this.editorScroll = setTimeout(() => {
      const editor = document.getElementById("fact-editor");
      editor?.scrollIntoView({ behavior: "smooth", block: "start" });
      editor?.focus({ preventScroll: true });
    });
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
    if (this.busy() || !this.selected || this.selected.publishedAt) return;
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
          if (f.status === "VALIDATED" && !f.publishedAt) this.chosen.add(f.id);
          else this.chosen.delete(f.id);
          this.message.set("Décision enregistrée : " + f.status);
          this.load();
        },
        error: (e) => this.fail(e),
      });
  }
  add() {
    if (this.busy() || !this.newPassage.trim()) return;
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
  publishableFacts(): any[] {
    return (this.review()?.facts || []).filter((f: any) => f.status === "VALIDATED" && !f.publishedAt);
  }
  chooseAll() {
    this.chosen = new Set(this.publishableFacts().slice(0, 500).map((f: any) => f.id));
  }
  reviewNext() {
    const fact = (this.review()?.facts || []).find((f: any) => !f.publishedAt && f.status !== "REJECTED" && f.status !== "VALIDATED");
    if (fact) this.select(fact);

  }
  publicationIssue(): string {
    if (!this.publication.countryId || !this.publication.academicYearId || !this.publication.guideTitle?.trim() || !this.publication.effectiveDate)
      return "Renseignez le pays, l’année, le titre et la date de validité.";
    if (!this.publishableFacts().length)
      return "Aucune information validée à publier. Vérifiez une information extraite et cliquez sur « Valider l’information ». Remplir les informations du guide ne valide pas ses extractions.";
    if (!this.chosen.size) return "Cochez au moins une information validée à publier.";
    const eligible = new Set(this.publishableFacts().map((f: any) => f.id));
    if ([...this.chosen].some(id => !eligible.has(id))) return "La sélection contient une information qui n’est plus publiable. Rechargez la revue.";
    if (this.chosen.size > 500) return "Sélectionnez au maximum 500 informations par publication.";
    if (this.publication.expiryDate && this.publication.expiryDate < this.publication.effectiveDate)
      return "La fin de validité doit être postérieure ou égale au début.";
    return "";
  }
  publish() {
    if (this.busy()) return;
    const issue = this.publicationIssue();
    if (issue) { this.error.set(issue); return; }
    this.message.set("");
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
