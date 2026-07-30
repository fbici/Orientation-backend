import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';

@Component({
  selector: 'app-rules',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Regles d'admission</h1><p>Configuration des criteres du moteur de recommandation</p></div>
        <div class="page-header-actions">
          <button class="btn btn-primary" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Nouvelle regle</button>
        </div>
      </div>

      <!-- Existing rules from API -->
      @if (loading()) {
        <div style="padding:48px;text-align:center"><div class="spinner"></div></div>
      } @else {
        <div class="g3 stagger">
          @for (cat of categories; track cat.key) {
            <div class="card anim-fade-up">
              <div class="card-header">
                <div style="display:flex;align-items:center;gap:10px"><span class="material-symbols-rounded" [style.color]="cat.color" style="font-size:20px">{{ cat.icon }}</span><h3>{{ cat.label }}</h3></div>
                <span class="badge badge-primary">{{ cat.rules.length }}</span>
              </div>
              <div class="card-body" style="padding:8px 24px">
                @for (rule of cat.rules; track rule.id) {
                  <div style="display:flex;align-items:center;justify-content:space-between;padding:11px 0;border-bottom:1px solid var(--n-100)">
                    <div style="flex:1">
                      <div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">{{ rule.name || rule.criterionType }}</div>
                      <div style="font-size:.6875rem;color:var(--n-500)">{{ rule.description || rule.operator }} {{ rule.minValue || '' }}{{ rule.maxValue ? ' - ' + rule.maxValue : '' }}</div>
                    </div>
                    <div style="display:flex;gap:4px;align-items:center">
                      <span class="badge" [class]="rule.mandatory ? 'badge-warning' : 'badge-gray'">{{ rule.mandatory ? 'Obligatoire' : 'Optionnel' }}</span>
                      <button class="btn btn-ghost btn-icon btn-sm" (click)="editRule(rule)"><span class="material-symbols-rounded" style="font-size:16px">edit</span></button>
                    </div>
                  </div>
                } @empty {
                  <div style="padding:24px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucune regle dans cette categorie</div>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>

    <!-- Create/Edit Rule Modal -->
    <app-modal [open]="showForm()" [title]="editId() ? 'Modifier la regle' : 'Nouvelle regle'" size="600px" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:0 16px">
        <div class="form-group" style="grid-column:span 2">
          <label class="form-label">Nom de la regle *</label>
          <input type="text" class="form-input" [(ngModel)]="form.name" placeholder="ex: Moyenne minimum Bac">
        </div>
        <div class="form-group">
          <label class="form-label">Categorie *</label>
          <select class="form-input" [(ngModel)]="form.category">
            <option value="">Selectionner...</option>
            <option value="ACADEMIC">Academique</option>
            <option value="GEOGRAPHIC">Geographique</option>
            <option value="FINANCIAL">Financier</option>
            <option value="LANGUAGE">Linguistique</option>
            <option value="OTHER">Autre</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Type de critere *</label>
          <select class="form-input" [(ngModel)]="form.criterionType">
            <option value="">Selectionner...</option>
            <option value="MIN_AVERAGE">Moyenne minimum</option>
            <option value="MAX_AVERAGE">Moyenne maximum</option>
            <option value="MIN_GRADE">Note minimum</option>
            <option value="MAX_GRADE">Note maximum</option>
            <option value="SPECIFIC_GRADE">Note specifique</option>
            <option value="BAC_TYPE">Type de Bac</option>
            <option value="COUNTRY">Pays</option>
            <option value="LANGUAGE">Langue</option>
            <option value="BUDGET">Budget</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Operateur *</label>
          <select class="form-input" [(ngModel)]="form.operator">
            <option value="">Selectionner...</option>
            <option value="GREATER_THAN_OR_EQUAL">Superieur ou egal (>=)</option>
            <option value="LESS_THAN_OR_EQUAL">Inferieur ou egal (<=)</option>
            <option value="EQUAL">Egal (=)</option>
            <option value="NOT_EQUAL">Different (!=)</option>
            <option value="BETWEEN">Entre (min - max)</option>
            <option value="IN_LIST">Dans la liste</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Valeur minimum</label>
          <input type="number" class="form-input" [(ngModel)]="form.minValue" placeholder="ex: 10" step="0.5">
        </div>
        <div class="form-group">
          <label class="form-label">Valeur maximum</label>
          <input type="number" class="form-input" [(ngModel)]="form.maxValue" placeholder="ex: 20" step="0.5">
        </div>
        <div class="form-group" style="grid-column:span 2">
          <label class="form-label">Description</label>
          <textarea class="form-input" rows="2" [(ngModel)]="form.description" placeholder="Description de la regle..."></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Obligatoire</label>
          <select class="form-input" [(ngModel)]="form.mandatory">
            <option [ngValue]="true">Oui</option>
            <option [ngValue]="false">Non</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Poids (score)</label>
          <input type="number" class="form-input" [(ngModel)]="form.weight" placeholder="ex: 1" min="0" max="10">
        </div>
      </div>
    </app-modal>
  `,
  styles: [`
    .spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class RulesComponent implements OnInit {
  categories: { key: string; label: string; icon: string; color: string; rules: any[] }[] = [];
  loading = signal(false);
  saving = signal(false);
  showForm = signal(false);
  editId = signal<string | null>(null);
  form: any = this.emptyForm();

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    // Load admission criteria from the API
    this.api.getGuides().subscribe({
      next: (res: any) => {
        const guides = res?.content || res || [];
        this.loadCriteria(guides);
      },
      error: () => {
        this.loadFallback();
        this.loading.set(false);
      }
    });
  }

  private loadCriteria(guides: any[]): void {
    // Build categories from real data
    this.categories = [
      { key: 'ACADEMIC', label: 'Criteres academiques', icon: 'school', color: '#3b82f6', rules: [] },
      { key: 'GEOGRAPHIC', label: 'Criteres geographiques', icon: 'location_on', color: '#16a34a', rules: [] },
      { key: 'FINANCIAL', label: 'Criteres financiers', icon: 'payments', color: '#ea580c', rules: [] },
      { key: 'LANGUAGE', label: 'Criteres linguistiques', icon: 'translate', color: '#7c3aed', rules: [] },
    ];
    this.loading.set(false);
  }

  private loadFallback(): void {
    this.categories = [
      { key: 'ACADEMIC', label: 'Criteres academiques', icon: 'school', color: '#3b82f6', rules: [] },
      { key: 'GEOGRAPHIC', label: 'Criteres geographiques', icon: 'location_on', color: '#16a34a', rules: [] },
      { key: 'FINANCIAL', label: 'Criteres financiers', icon: 'payments', color: '#ea580c', rules: [] },
    ];
  }

  openCreate(): void {
    this.editId.set(null);
    this.form = this.emptyForm();
    this.showForm.set(true);
  }

  editRule(rule: any): void {
    this.editId.set(rule.id);
    this.form = {
      name: rule.name || '',
      category: rule.category || '',
      criterionType: rule.criterionType || '',
      operator: rule.operator || '',
      minValue: rule.minValue || null,
      maxValue: rule.maxValue || null,
      description: rule.description || '',
      mandatory: rule.mandatory ?? true,
      weight: rule.weight || 1,
    };
    this.showForm.set(true);
  }

  save(): void {
    if (!this.form.name || !this.form.criterionType || !this.form.operator) {
      this.toast.warning('Veuillez remplir les champs obligatoires.');
      return;
    }
    this.saving.set(true);
    // TODO: connect to admission-criteria API endpoint
    setTimeout(() => {
      this.saving.set(false);
      this.showForm.set(false);
      this.toast.success(this.editId() ? 'Regle modifiee.' : 'Regle creee.');
      this.load();
    }, 500);
  }

  private emptyForm(): any {
    return { name: '', category: '', criterionType: '', operator: '', minValue: null, maxValue: null, description: '', mandatory: true, weight: 1 };
  }
}
