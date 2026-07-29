import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-imports',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="imports">
      <h1>Imports</h1>

      <div class="upload-card">
        <h3>Importer des Données</h3>
        <div class="form-row">
          <div class="form-group">
            <label>Type de données</label>
            <select [(ngModel)]="dataType" name="dataType">
              <option value="COUNTRIES">Pays</option>
              <option value="UNIVERSITIES">Universités</option>
              <option value="PROGRAMS">Programmes</option>
              <option value="SUBJECTS">Matières</option>
              <option value="SCHOLARSHIPS">Bourses</option>
            </select>
          </div>
          <div class="form-group">
            <label>Fichier</label>
            <input type="file" (change)="onFileSelected($event)" accept=".csv,.xlsx,.json">
          </div>
        </div>
        <button (click)="importFile()" [disabled]="!selectedFile() || importing()">
          {{ importing() ? 'Import en cours...' : 'Importer' }}
        </button>
        @if (importResult()) {
          <div class="result-card">
            <p>{{ importResult()?.message }}</p>
            <p>Total: {{ importResult()?.totalRecords }} | Importés: {{ importResult()?.successRecords }}</p>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .imports { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .upload-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
    .form-group label { display: block; margin-bottom: 5px; }
    .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
    button { padding: 12px 24px; background: #1a1a2e; color: white; border: none; border-radius: 4px; cursor: pointer; }
    button:disabled { background: #ccc; }
    .result-card { margin-top: 15px; padding: 15px; background: #d4edda; border-radius: 4px; }
  `]
})
export class ImportsComponent {
  dataType = 'COUNTRIES';
  selectedFile = signal<File | null>(null);
  importing = signal(false);
  importResult = signal<any>(null);

  constructor(private api: ApiService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile.set(input.files[0]);
    }
  }

  importFile(): void {
    const file = this.selectedFile();
    if (!file) return;

    this.importing.set(true);
    this.api.importFile(file, this.dataType).subscribe({
      next: (data) => {
        this.importResult.set(data);
        this.importing.set(false);
      },
      error: () => this.importing.set(false)
    });
  }
}
