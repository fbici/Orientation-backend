import { Component, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink, Router } from "@angular/router";
import { ApiService } from "../../core/services/api.service";
@Component({
  selector: "app-documents",
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: ` <div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>Documents et imports de guides</h1>
        <p>
          PDF texte, PDF scannés, PNG et JPEG · extraction locale puis revue
          humaine.
        </p>
      </div>
      <label class="btn btn-primary"
        >Importer un document<input
          type="file"
          accept=".pdf,.png,.jpg,.jpeg"
          hidden
          [disabled]="uploading()"
          (change)="upload($event)"
      /></label>
    </div>
    @if (error()) {
      <div class="alert alert-error" role="alert">{{ error() }}</div>
    }
    @if (uploading()) {
      <p role="status">Enregistrement sécurisé du document…</p>
    }
    <div class="card">
      <table class="data-table">
        <thead>
          <tr>
            <th>Document</th>
            <th>État</th>
            <th>Pages</th>
            <th>Importé le</th>
            <th>Revue</th>
          </tr>
        </thead>
        <tbody>
          @if (loading()) {
            <tr>
              <td colspan="5">Chargement…</td>
            </tr>
          } @else {
            @for (d of rows(); track d.id) {
              <tr>
                <td>
                  {{ d.title || d.originalFileName }}<br /><small>{{
                    d.mimeType
                  }}</small>
                </td>
                <td>{{ d.status }}</td>
                <td>{{ d.pageCount ?? "—" }}</td>
                <td>{{ d.uploadedAt | date: "short" }}</td>
                <td>
                  <a
                    class="btn btn-secondary btn-sm"
                    [routerLink]="['/documents', d.id, 'review']"
                    >Ouvrir</a
                  >
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="5">
                  Aucun document enregistré pour votre périmètre.
                </td>
              </tr>
            }
          }
        </tbody>
      </table>
    </div>
    <div class="page-header-actions">
      <button
        class="btn btn-secondary"
        [disabled]="page === 0"
        (click)="page = page - 1; load()"
      >
        Précédent</button
      ><span>Page {{ page + 1 }} · {{ total }} documents</span
      ><button
        class="btn btn-secondary"
        [disabled]="(page + 1) * 20 >= total"
        (click)="page = page + 1; load()"
      >
        Suivant</button
      ><button class="btn btn-ghost" (click)="load()">Actualiser</button>
    </div>
  </div>`,
})
export class DocumentsComponent implements OnInit {
  rows = signal<any[]>([]);
  loading = signal(false);
  uploading = signal(false);
  error = signal("");
  page = 0;
  total = 0;
  constructor(
    private api: ApiService,
    private router: Router,
  ) {}
  ngOnInit() {
    this.load();
  }
  load() {
    this.loading.set(true);
    this.error.set("");
    this.api.getDocuments(this.page).subscribe({
      next: (r) => {
        this.rows.set(r.content);
        this.total = r.totalElements;
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.error.set(e.error?.message || "Documents indisponibles.");
      },
    });
  }
  upload(event: Event) {
    const input = event.target as HTMLInputElement;
    const f = input.files?.[0];
    if (!f) return;
    if (f.size > 10 * 1024 * 1024) {
      this.error.set("Taille maximale : 10 Mo.");
      input.value = "";
      return;
    }
    this.uploading.set(true);
    this.error.set("");
    this.api.uploadDocument(f).subscribe({
      next: (r) => {
        this.uploading.set(false);
        input.value = "";
        this.router.navigate(["/documents", r.id, "review"]);
      },
      error: (e) => {
        this.uploading.set(false);
        input.value = "";
        this.error.set(
          e.error?.message || "Import refusé. Vérifiez le format et la taille.",
        );
      },
    });
  }
}
