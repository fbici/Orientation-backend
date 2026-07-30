import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-universities',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ModalComponent, ConfirmDialogComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Universités</h1><p>Gestion des universités et établissements</p></div>
        <div class="page-header-actions">
          <button class="btn btn-primary" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Ajouter</button>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span style="font-size:.8125rem;color:var(--n-500)">{{ total() }} universités</span>
          <div style="display:flex;gap:8px">
            <div style="position:relative">
              <span class="material-symbols-rounded" style="position:absolute;left:10px;top:50%;transform:translateY(-50%);font-size:18px;color:var(--n-400)">search</span>
              <input type="text" class="form-input" style="padding-left:36px;width:220px" placeholder="Rechercher..." [(ngModel)]="search" (ngModelChange)="load()">
            </div>
            <button class="btn btn-secondary btn-sm" (click)="load()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button>
          </div>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner"></div><p style="margin-top:12px;font-size:.8125rem;color:var(--n-500)">Chargement...</p></div>
          } @else {
            <table class="data-table">
              <thead>
                <tr>
                  <th>Nom</th><th>Pays</th><th>Ville</th><th>Statut</th><th>Classement</th><th style="text-align:right">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (u of universities(); track u.id) {
                  <tr>
                    <td>
                      <a [routerLink]="['/universities', u.id]" style="font-weight:600;color:var(--n-900)">{{ u.name }}</a>
                      @if (u.shortName) { <span style="font-size:.75rem;color:var(--n-500);margin-left:6px">{{ u.shortName }}</span> }
                    </td>
                    <td style="font-size:.8125rem">{{ u.country?.name || '-' }}</td>
                    <td style="font-size:.8125rem">{{ u.city?.name || '-' }}</td>
                    <td><span class="badge" [class]="u.status === 'ACTIVE' ? 'badge-success' : 'badge-gray'">{{ u.status || 'ACTIVE' }}</span></td>
                    <td style="font-size:.8125rem;font-weight:600">{{ u.ranking || '-' }}</td>
                    <td style="text-align:right">
                      <div style="display:flex;gap:2px;justify-content:flex-end">
                        <button class="btn btn-ghost btn-icon btn-sm" (click)="openEdit(u)"><span class="material-symbols-rounded" style="font-size:18px">edit</span></button>
                        <button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDelete(u)"><span class="material-symbols-rounded" style="font-size:18px;color:var(--red-500)">delete</span></button>
                      </div>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" style="text-align:center;padding:48px;color:var(--n-400)">
                    <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">school</span>
                    <p style="font-weight:600;color:var(--n-600)">Aucune université</p>
                    <p style="font-size:.8125rem;margin-bottom:16px">Ajoutez votre premiere universite pour commencer.</p>
                    <button class="btn btn-primary btn-sm" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Ajouter une universite</button>
                  </td></tr>
                }
              </tbody>
            </table>
          }
        </div>
        @if (totalPages() > 1) {
          <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-top:1px solid var(--n-100)">
            <span style="font-size:.8125rem;color:var(--n-500)">{{ total() }} resultats</span>
            <div style="display:flex;gap:4px">
              <button class="btn btn-ghost btn-sm btn-icon" [disabled]="page()===0" (click)="page.set(page()-1);load()"><span class="material-symbols-rounded" style="font-size:18px">chevron_left</span></button>
              <span style="font-size:.8125rem;color:var(--n-600);padding:5px 12px">{{ page()+1 }} / {{ totalPages() }}</span>
              <button class="btn btn-ghost btn-sm btn-icon" [disabled]="page()>=totalPages()-1" (click)="page.set(page()+1);load()"><span class="material-symbols-rounded" style="font-size:18px">chevron_right</span></button>
            </div>
          </div>
        }
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <app-modal [open]="showForm()" [title]="formTitle()" size="600px" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:0 16px">
        <div class="form-group" style="grid-column:span 2"><label class="form-label">Nom *</label><input type="text" class="form-input" [(ngModel)]="form.name" placeholder="ex: Universite d'Abomey-Calavi"></div>
        <div class="form-group"><label class="form-label">Sigle</label><input type="text" class="form-input" [(ngModel)]="form.shortName" placeholder="ex: UAC"></div>
        <div class="form-group"><label class="form-label">Pays *</label>
          <select class="form-input" [(ngModel)]="form.countryId"><option value="">Selectionner...</option>@for(c of countries();track c.id){<option [value]="c.id">{{c.name}}</option>}</select>
        </div>
        <div class="form-group"><label class="form-label">Ville *</label>
          <select class="form-input" [(ngModel)]="form.cityId"><option value="">Selectionner...</option>@for(c of cities();track c.id){<option [value]="c.id">{{c.name}}</option>}</select>
        </div>
        <div class="form-group"><label class="form-label">Statut</label>
          <select class="form-input" [(ngModel)]="form.status"><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select>
        </div>
        <div class="form-group" style="grid-column:span 2"><label class="form-label">Adresse</label><input type="text" class="form-input" [(ngModel)]="form.address" placeholder="Adresse complete"></div>
        <div class="form-group"><label class="form-label">Email</label><input type="email" class="form-input" [(ngModel)]="form.email" placeholder="contact@univ.edu"></div>
        <div class="form-group"><label class="form-label">Telephone</label><input type="tel" class="form-input" [(ngModel)]="form.phone" placeholder="+229 XX XX XX XX"></div>
        <div class="form-group"><label class="form-label">Site web</label><input type="url" class="form-input" [(ngModel)]="form.website" placeholder="https://..."></div>
        <div class="form-group"><label class="form-label">Annee de fondation</label><input type="number" class="form-input" [(ngModel)]="form.foundedYear" placeholder="ex: 1970"></div>
        <div class="form-group" style="grid-column:span 2"><label class="form-label">Description</label><textarea class="form-input" rows="3" [(ngModel)]="form.description" placeholder="Description de l'universite..."></textarea></div>
      </div>
    </app-modal>

    <!-- Delete Confirmation -->
    <app-confirm-dialog
      [open]="showDelete()"
      title="Supprimer"
      [message]="deleteMessage()"
      confirmText="Supprimer"
      icon="delete_forever"
      iconColor="#ef4444"
      [loading]="deleting()"
      (close)="showDelete.set(false)"
      (confirm)="doDelete()">
    </app-confirm-dialog>
  `,
  styles: [`
    .spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class UniversitiesComponent implements OnInit {
  universities = signal<any[]>([]);
  countries = signal<any[]>([]);
  cities = signal<any[]>([]);
  loading = signal(false);
  saving = signal(false);
  deleting = signal(false);
  showForm = signal(false);
  showDelete = signal(false);
  editId = signal<string | null>(null);
  deleteTarget = signal<any>(null);
  page = signal(0);
  total = signal(0);
  totalPages = signal(0);
  search = '';

  form: any = this.emptyForm();

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnInit(): void {
    this.load();
    this.api.getCountries().subscribe({ next: (r) => this.countries.set(r?.content || r || []), error: () => {} });
    this.api.getCities().subscribe({ next: (r) => this.cities.set(r?.content || r || []), error: () => {} });
  }

  formTitle(): string {
    return this.editId() ? 'Modifier' : 'Nouvelle universite';
  }

  deleteMessage(): string {
    const name = this.deleteTarget()?.name || '';
    return `Supprimer "${name}" ? Cette action est irreversible.`;
  }

  load(): void {
    this.loading.set(true);
    this.api.getUniversities(this.page(), 20).subscribe({
      next: (r) => {
        this.universities.set(r?.content || []);
        this.total.set(r?.totalElements || 0);
        this.totalPages.set(r?.totalPages || 0);
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); this.toast.error('Erreur lors du chargement.'); }
    });
  }

  openCreate(): void {
    this.editId.set(null);
    this.form = this.emptyForm();
    this.showForm.set(true);
  }

  openEdit(u: any): void {
    this.editId.set(u.id);
    this.form = {
      name: u.name || '', shortName: u.shortName || '', countryId: u.country?.id || '', cityId: u.city?.id || '',
      address: u.address || '', email: u.email || '', phone: u.phone || '', website: u.website || '',
      foundedYear: u.foundedYear || null, status: u.status || 'ACTIVE', description: u.description || ''
    };
    this.showForm.set(true);
  }

  save(): void {
    if (!this.form.name || !this.form.countryId || !this.form.cityId) {
      this.toast.warning('Veuillez remplir les champs obligatoires.');
      return;
    }
    this.saving.set(true);
    const call = this.editId()
      ? this.api.updateUniversity(this.editId()!, this.form)
      : this.api.createUniversity(this.form);
    call.subscribe({
      next: () => { this.saving.set(false); this.showForm.set(false); this.toast.success(this.editId() ? 'Universite modifiee.' : 'Universite creee.'); this.load(); },
      error: (e) => { this.saving.set(false); this.toast.error(e.error?.message || 'Erreur lors de la sauvegarde.'); }
    });
  }

  confirmDelete(u: any): void {
    this.deleteTarget.set(u);
    this.showDelete.set(true);
  }

  doDelete(): void {
    this.deleting.set(true);
    this.api.deleteUniversity(this.deleteTarget()?.id).subscribe({
      next: () => { this.deleting.set(false); this.showDelete.set(false); this.toast.success('Universite supprimee.'); this.load(); },
      error: (e) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur lors de la suppression.'); }
    });
  }

  private emptyForm(): any {
    return { name: '', shortName: '', countryId: '', cityId: '', address: '', email: '', phone: '', website: '', foundedYear: null, status: 'ACTIVE', description: '' };
  }
}
