import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-departments', standalone: true, imports: [CommonModule, FormsModule, RouterLink, ModalComponent, ConfirmDialogComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Départements</h1><p>Structure organisationnelle</p></div></div>
        <div class="page-header-actions"><button class="btn btn-primary" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Ajouter</button></div>
      </div>
      <div class="card">
        <div class="card-body" style="padding:0">
          @if (loading()) { <div style="padding:48px;text-align:center"><div class="spinner"></div></div> } @else {
            <table class="data-table">
              <thead><tr><th>Nom</th><th>Description</th><th>Membres</th><th style="text-align:right">Actions</th></tr></thead>
              <tbody>
                @for (d of depts(); track d.id) {
                  <tr>
                    <td style="font-weight:600">{{ d.name }}</td>
                    <td style="font-size:.8125rem;color:var(--n-600)">{{ d.description || '—' }}</td>
                    <td>{{ d.memberCount || 0 }}</td>
                    <td style="text-align:right"><div style="display:flex;gap:2px;justify-content:flex-end">
                      <button class="btn btn-ghost btn-icon btn-sm" (click)="openEdit(d)"><span class="material-symbols-rounded" style="font-size:18px">edit</span></button>
                      <button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDel(d)"><span class="material-symbols-rounded" style="font-size:18px;color:var(--red-500)">delete</span></button>
                    </div></td>
                  </tr>
                } @empty { <tr><td colspan="4" style="text-align:center;padding:48px;color:var(--n-400)">Aucun département</td></tr> }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>
    <app-modal [open]="showForm()" [title]="editId() ? 'Modifier' : 'Nouveau département'" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div class="form-group"><label class="form-label">Nom *</label><input type="text" class="form-input" [(ngModel)]="form.name"></div>
      <div class="form-group"><label class="form-label">Description</label><textarea class="form-input" rows="3" [(ngModel)]="form.description"></textarea></div>
    </app-modal>
    <app-confirm-dialog [open]="showDel()" title="Supprimer" [message]="'Supprimer « ' + (delTarget()?.name || '') + ' » ?'" [loading]="deleting()" (close)="showDel.set(false)" (confirm)="doDel()"></app-confirm-dialog>
  `,
  styles: [`.spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}@keyframes spin{to{transform:rotate(360deg)}}`]
})
export class DepartmentsComponent implements OnInit {
  depts = signal<any[]>([]);
  loading = signal(false); saving = signal(false); deleting = signal(false);
  showForm = signal(false); showDel = signal(false); editId = signal<string | null>(null); delTarget = signal<any>(null);
  form: any = {};
  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.api.getDepartments().subscribe({ next: (r) => { this.depts.set(r?.content || r || []); this.loading.set(false); }, error: () => this.loading.set(false) }); }
  openCreate(): void { this.editId.set(null); this.form = { name: '', description: '' }; this.showForm.set(true); }
  openEdit(d: any): void { this.editId.set(d.id); this.form = { name: d.name, description: d.description }; this.showForm.set(true); }
  save(): void { if (!this.form.name) { this.toast.warning('Nom requis.'); return; } this.saving.set(true); const call = this.editId() ? this.api.updateDepartment(this.editId()!, this.form) : this.api.createDepartment(this.form); call.subscribe({ next: () => { this.saving.set(false); this.showForm.set(false); this.toast.success('Sauvegardé.'); this.load(); }, error: (e) => { this.saving.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
  confirmDel(d: any): void { this.delTarget.set(d); this.showDel.set(true); }
  doDel(): void { this.deleting.set(true); this.api.deleteDepartment(this.delTarget()?.id).subscribe({ next: () => { this.deleting.set(false); this.showDel.set(false); this.toast.success('Supprimé.'); this.load(); }, error: (e) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
}
