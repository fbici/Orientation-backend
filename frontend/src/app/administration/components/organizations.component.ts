import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-organizations', standalone: true, imports: [CommonModule, FormsModule, RouterLink, ModalComponent, ConfirmDialogComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Organisations</h1><p>Gestion des organisations partenaires</p></div></div>
        <div class="page-header-actions"><button class="btn btn-primary" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Ajouter</button></div>
      </div>
      <div class="card">
        <div class="card-body" style="padding:0">
          @if (loading()) { <div style="padding:48px;text-align:center"><div class="spinner"></div></div> } @else {
            <table class="data-table">
              <thead><tr><th>Nom</th><th>Pays</th><th>Tenants</th><th>Statut</th><th style="text-align:right">Actions</th></tr></thead>
              <tbody>
                @for (o of orgs(); track o.id) {
                  <tr>
                    <td style="font-weight:600">{{ o.name }}</td>
                    <td>{{ o.country || '—' }}</td>
                    <td>{{ o.tenantCount || 0 }}</td>
                    <td><span class="badge badge-success">{{ o.status || 'Active' }}</span></td>
                    <td style="text-align:right"><div style="display:flex;gap:2px;justify-content:flex-end">
                      <button class="btn btn-ghost btn-icon btn-sm" (click)="openEdit(o)"><span class="material-symbols-rounded" style="font-size:18px">edit</span></button>
                      <button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDel(o)"><span class="material-symbols-rounded" style="font-size:18px;color:var(--red-500)">delete</span></button>
                    </div></td>
                  </tr>
                } @empty { <tr><td colspan="5" style="text-align:center;padding:48px;color:var(--n-400)">Aucune organisation</td></tr> }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>
    <app-modal [open]="showForm()" [title]="editId() ? 'Modifier' : 'Nouvelle organisation'" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div class="form-group"><label class="form-label">Nom *</label><input type="text" class="form-input" [(ngModel)]="form.name"></div>
      <div class="form-group"><label class="form-label">Pays</label><input type="text" class="form-input" [(ngModel)]="form.country"></div>
      <div class="form-group"><label class="form-label">Description</label><textarea class="form-input" rows="3" [(ngModel)]="form.description"></textarea></div>
    </app-modal>
    <app-confirm-dialog [open]="showDel()" title="Supprimer" [message]="'Supprimer « ' + (delTarget()?.name || '') + ' » ?'" [loading]="deleting()" (close)="showDel.set(false)" (confirm)="doDel()"></app-confirm-dialog>
  `,
  styles: [`.spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}@keyframes spin{to{transform:rotate(360deg)}}`]
})
export class OrganizationsComponent implements OnInit {
  orgs = signal<any[]>([]); loading = signal(false); saving = signal(false); deleting = signal(false);
  showForm = signal(false); showDel = signal(false); editId = signal<string | null>(null); delTarget = signal<any>(null);
  form: any = {};
  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.api.getOrganizations().subscribe({ next: (r) => { this.orgs.set(r?.content || r || []); this.loading.set(false); }, error: () => this.loading.set(false) }); }
  openCreate(): void { this.editId.set(null); this.form = { name: '', country: '', description: '' }; this.showForm.set(true); }
  openEdit(o: any): void { this.editId.set(o.id); this.form = { name: o.name, country: o.country, description: o.description }; this.showForm.set(true); }
  save(): void { if (!this.form.name) { this.toast.warning('Nom requis.'); return; } this.saving.set(true); const call = this.editId() ? this.api.updateOrganization(this.editId()!, this.form) : this.api.createOrganization(this.form); call.subscribe({ next: () => { this.saving.set(false); this.showForm.set(false); this.toast.success('Sauvegardé.'); this.load(); }, error: (e) => { this.saving.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
  confirmDel(o: any): void { this.delTarget.set(o); this.showDel.set(true); }
  doDel(): void { this.deleting.set(true); this.api.deleteOrganization(this.delTarget()?.id).subscribe({ next: () => { this.deleting.set(false); this.showDel.set(false); this.toast.success('Supprimé.'); this.load(); }, error: (e) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
}
