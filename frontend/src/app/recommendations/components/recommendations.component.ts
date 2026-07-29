import { Component, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  template: `
    <div class="recommendations">
      <h1>Recommandations</h1>

      <div class="generator-card">
        <h3>Générer des Recommandations</h3>
        <form (ngSubmit)="generate()">
          <div class="form-row">
            <div class="form-group">
              <label>Type de Bac</label>
              <select [(ngModel)]="bacType" name="bacType">
                <option value="SCIENTIFIQUE">Scientifique</option>
                <option value="LITTERAIRE">Littéraire</option>
                <option value="MATHEMATIQUES">Mathématiques</option>
              </select>
            </div>
            <div class="form-group">
              <label>Moyenne</label>
              <input type="number" [(ngModel)]="bacAverage" name="bacAverage" step="0.1" min="0" max="20">
            </div>
          </div>
          <button type="submit" [disabled]="loading()">
            {{ loading() ? 'Génération en cours...' : 'Générer' }}
          </button>
        </form>
      </div>

      @if (recommendations()) {
        <div class="results-card">
          <h3>Résultats ({{ recommendations()?.totalPrograms }} programmes)</h3>
          <div class="results-table">
            <div class="table-header">
              <span>Rang</span>
              <span>Programme</span>
              <span>Université</span>
              <span>Score</span>
              <span>Probabilité</span>
              <span>Difficulté</span>
            </div>
            @for (rec of recommendations()?.recommendations || []; track rec.rank) {
              <div class="table-row">
                <span class="rank">{{ rec.rank }}</span>
                <span>{{ rec.programName }}</span>
                <span>{{ rec.universityName }}</span>
                <span class="score">{{ rec.score | number:'1.0-1' }}</span>
                <span>{{ rec.admissionProbability }}%</span>
                <span [class]="'difficulty-' + rec.difficultyLevel?.toLowerCase()">{{ rec.difficultyLevel }}</span>
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .recommendations { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .generator-card, .results-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .form-group { margin-bottom: 15px; }
    .form-group label { display: block; margin-bottom: 5px; color: #333; }
    .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
    button { padding: 12px 24px; background: #1a1a2e; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
    button:disabled { background: #ccc; }
    .results-table { margin-top: 20px; }
    .table-header, .table-row { display: grid; grid-template-columns: 60px 1fr 1fr 80px 100px 100px; padding: 12px; gap: 10px; }
    .table-header { background: #1a1a2e; color: white; border-radius: 4px; font-weight: bold; }
    .table-row { border-bottom: 1px solid #f0f0f0; }
    .table-row:hover { background: #f8f9fa; }
    .rank { font-weight: bold; color: #1a1a2e; }
    .score { font-weight: bold; color: #27ae60; }
    .difficulty-facile { color: #27ae60; }
    .difficulty-moyen { color: #f39c12; }
    .difficulty-difficile { color: #e67e22; }
    .difficulty-très\ difficile { color: #e74c3c; }
  `]
})
export class RecommendationsComponent {
  bacType = 'SCIENTIFIQUE';
  bacAverage = 15;
  loading = signal(false);
  recommendations = signal<any>(null);

  constructor(private api: ApiService) {}

  generate(): void {
    this.loading.set(true);
    this.api.generateRecommendations({
      bacType: this.bacType,
      bacAverage: this.bacAverage,
      subjectGrades: {}
    }).subscribe({
      next: (data) => {
        this.recommendations.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
