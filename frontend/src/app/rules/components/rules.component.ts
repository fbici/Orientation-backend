import { Component, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { ApiService } from "../../core/services/api.service";
@Component({
  selector: "app-rules",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `<div class="anim-fade-up">
    <div class="page-header">
      <div>
        <h1>Règles d'admission publiées</h1>
        <p>
          Critères validés, rattachés à une formation et à une version de guide.
        </p>
      </div>
      <a class="btn btn-primary" routerLink="/documents"
        >Créer ou corriger depuis une source</a
      >
    </div>
    <label class="form-label">Formation</label
    ><select
      class="form-input"
      [(ngModel)]="programId"
      (ngModelChange)="page = 0; load()"
    >
      <option value="">Toutes les formations</option>
      @for (p of programs; track p.id) {
        <option [value]="p.id">{{ p.name }}</option>
      }
    </select>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }
    @if (loading()) {
      <p role="status">Chargement…</p>
    } @else {
      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>Formation</th>
              <th>Condition</th>
              <th>Caractère</th>
              <th>Source</th>
            </tr>
          </thead>
          <tbody>
            @for (r of rows(); track r.id) {
              <tr>
                <td>{{ r.programName }}</td>
                <td>
                  {{ r.criterionType }} · {{ r.operator }} ·
                  {{ r.minValue ?? r.stringValue }} {{ r.maxValue ?? ""
                  }}<br /><small>{{ r.description }}</small>
                </td>
                <td>
                  {{ r.mandatory ? "Obligatoire" : "Optionnel" }} ·
                  {{ r.logicalOperator }} / {{ r.logicalGroup }}
                </td>
                <td>
                  Guide v{{ r.guideVersionNumber }}<br /><small>{{
                    r.sourceFactId
                  }}</small>
                    @if(r.source){<p><a [routerLink]="['/documents',r.source.documentId,'review']">Page {{r.source.pageNumber}} - consulter la source</a></p><blockquote>{{r.source.passage}}</blockquote>}
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="4">Aucune règle publiée pour ces filtres.</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
    <div class="page-header-actions">
      <button
        class="btn btn-secondary"
        [disabled]="page === 0"
        (click)="page = page - 1; load()"
      >
        Précédent</button
      ><span>{{ total }} règles</span
      ><button
        class="btn btn-secondary"
        [disabled]="(page + 1) * 20 >= total"
        (click)="page = page + 1; load()"
      >
        Suivant
      </button>
    </div>
  </div>`,
})
export class RulesComponent implements OnInit {
  rows = signal<any[]>([]);
  loading = signal(false);
  error = signal("");
  page = 0;
  total = 0;
  programId = "";
  programs: any[] = [];
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.load();
    this.api.reference("programs").subscribe({
      next: (r) => (this.programs = r.content),
      error: () => this.error.set("Formations indisponibles."),
    });
  }
  load() {
    this.loading.set(true);
    this.error.set("");
    this.api.publishedRules(this.page, this.programId).subscribe({
      next: (r) => {
        this.rows.set(r.content);
        this.total = r.totalElements;
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.error.set(e.error?.message || "Règles indisponibles.");
      },
    });
  }
}
