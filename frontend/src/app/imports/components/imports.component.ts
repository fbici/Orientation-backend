import {PaginationComponent} from '../../shared/components/pagination.component';
import { Component, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { ApiService } from "../../core/services/api.service";
@Component({
  selector: "app-imports",
  standalone: true,
  imports: [PaginationComponent,CommonModule, FormsModule, RouterLink],
  template: `<div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>Imports de données</h1>
        <p>
          CSV UTF-8 et XLSX · 2 Mo, 1 000 lignes maximum · insertion atomique de
          brouillons.
        </p>
      </div>
      <a routerLink="/documents" class="btn btn-secondary"
        >Importer un guide PDF ou une image</a
      >
    </div>
    <div class="card card-body">
      <p>
        Les colonnes utilisent les champs du catalogue (name, countryId, cityId,
        facultyId…). Une importation ne publie aucune donnée officielle. Les
        règles d'admission passent par la revue du guide.
      </p>
      <label class="form-label">Données à importer</label
      ><select class="form-input" [(ngModel)]="type">
        @for (t of types; track t) {
          <option [value]="t">{{ t }}</option>
        }
      </select>
      <label class="form-label">Fichier</label
      ><input
        type="file"
        accept=".csv,.xlsx"
        [disabled]="busy()"
        (change)="file = $any($event.target).files?.[0] || null"
      />
      <label
        ><input type="checkbox" [(ngModel)]="dryRun" /> Vérifier sans
        enregistrer (toutes les écritures sont annulées)</label
      >
      <p>
        <button
          class="btn btn-primary"
          [disabled]="busy() || !file"
          (click)="upload()"
        >
          {{ dryRun ? "Vérifier" : "Importer les brouillons" }}
        </button>
      </p>
      @if (busy()) {
        <p role="status">Validation et traitement du fichier…</p>
      }
      @if (error()) {
        <p role="alert">{{ error() }}</p>
      }
      @if (result()) {
        <p role="status">
          {{ result().status }} · {{ result().totalRecords }} lignes ·
          {{ result().successRecords }} brouillons enregistrés
        </p>
        @for (w of result().warnings || []; track w) {
          <p>{{ w }}</p>
        }
      }
    </div>
    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>Fichier</th>
            <th>Type</th>
            <th>Lignes</th>
            <th>Enregistrées</th>
            <th>État</th>
          </tr>
        </thead>
        <tbody>
          @for (r of rows(); track r.id) {
            <tr>
              <td>
                {{ r.fileName }}<br /><small>{{
                  r.createdAt | date: "short"
                }}</small>
              </td>
              <td>{{ r.dataType }}</td>
              <td>{{ r.totalRows }}</td>
              <td>{{ r.importedRows }}</td>
              <td>
                {{ r.status }}<br /><small>{{ r.comments }}</small>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="5">Aucun import dans votre périmètre.</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
    <app-pagination [page]="page" [total]="total" [loading]="busy()" (pageChange)="page=$event;load()" />
  </div>`,
})
export class ImportsComponent implements OnInit {
  type = "UNIVERSITIES";
  types = ["UNIVERSITIES", "FACULTIES", "PROGRAMS", "SCHOLARSHIPS"];
  file: File | null = null;
  dryRun = true;
  busy = signal(false);
  error = signal("");
  result = signal<any>(null);
  rows = signal<any[]>([]);
  page = 0;
  total = 0;
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.load();
  }
  load() {
    this.api.getImports(this.page).subscribe({
      next: (r) => {
        this.rows.set(r.content);
        this.total = r.totalElements;
      },
      error: () => this.error.set("Historique d'import indisponible."),
    });
  }
  upload() {
    if (!this.file) return;
    if (this.file.size > 2097152) {
      this.error.set("Taille maximale : 2 Mo.");
      return;
    }
    this.busy.set(true);
    this.error.set("");
    this.result.set(null);
    this.api.importAcademic(this.file, this.type, this.dryRun).subscribe({
      next: (r) => {
        this.busy.set(false);
        this.result.set(r);
        this.load();
      },
      error: (e) => {
        this.busy.set(false);
        this.result.set(e.error);
        this.error.set(
          e.error?.errors?.join(", ") ||
            e.error?.message ||
            "Import refusé. Aucun brouillon partiel enregistré.",
        );
        this.load();
      },
    });
  }
}
