import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ModalComponent, ConfirmDialogComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px">
          <a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a>
          <div><h1>Utilisateurs</h1><p>Gestion des comptes utilisateurs</p></div>
        </div>
        <div class="page-header-actions"><button class="btn btn-primary" disabled title="Creation administrative non implementee par le backend"><span class="material-symbols-rounded">person_add</span>Ajouter</button></div>
      </div>

      <p class="api-notice">La creation et les changements de role ne sont pas disponibles dans le service administratif actuel.</p>@if(error()){<div class="api-error" role="alert">{{error()}}</div>}<div class="card">
        <div class="card-header">
          <span style="font-size:.8125rem;color:var(--n-500)">{{ total() }} utilisateurs</span>
          <div style="display:flex;gap:8px">
            <input type="text" class="form-input" style="width:200px" placeholder="Rechercher…" aria-label="Rechercher les utilisateurs" [(ngModel)]="search" (keyup.enter)="page=0; load()">
            <button class="btn btn-secondary btn-sm" aria-label="Rechercher et actualiser" (click)="page=0; load()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button>
          </div>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) { <div style="padding:48px;text-align:center"><div class="spinner"></div></div> } @else {
            <table class="data-table">
              <thead><tr><th>Utilisateur</th><th>Email</th><th>Rôle</th><th>Tenant</th><th>Statut</th><th style="text-align:right">Actions</th></tr></thead>
              <tbody>
                @for (u of users(); track u.id) {
                  <tr>
                    <td><div style="display:flex;align-items:center;gap:10px"><div class="avatar" [style.background]="avatarColor(u)">{{ initials(u) }}</div><span style="font-weight:600">{{ u.firstName }} {{ u.lastName }}</span></div></td>
                    <td style="font-size:.8125rem;color:var(--n-600)">{{ u.email }}</td>
                    <td><span class="badge" [class]="roleClass(u)">{{ u.roles?.[0] || 'N/A' }}</span></td>
                    <td style="font-size:.8125rem">{{ u.tenantName || u.tenant?.name || '—' }}</td>
                    <td><div style="display:flex;align-items:center;gap:6px"><span class="dot" [class]="u.enabled !== false ? 'green' : 'gray'"></span><span style="font-size:.8125rem">{{ u.enabled !== false ? 'Actif' : 'Inactif' }}</span></div></td>
                    <td style="text-align:right"><div style="display:flex;gap:2px;justify-content:flex-end">
                      <button class="btn btn-ghost btn-icon btn-sm" (click)="openEdit(u)" aria-label="Modifier"><span class="material-symbols-rounded" style="font-size:18px">edit</span></button>
                      <button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDel(u)" aria-label="Desactiver"><span class="material-symbols-rounded" style="font-size:18px;color:var(--red-500)">delete</span></button>
                    </div></td>
                  </tr>
                } @empty { <tr><td colspan="6" style="text-align:center;padding:48px;color:var(--n-400)">Aucun utilisateur</td></tr> }
              </tbody>
            </table>
          }
        </div>
      </div>
      <div class="table-pagination"><button class="btn btn-secondary" [disabled]="loading() || page===0" (click)="page=page-1; load()">Precedent</button><span>Page {{page+1}} - {{total()}} utilisateurs</span><button class="btn btn-secondary" [disabled]="loading() || (page+1)*20>=total()" (click)="page=page+1; load()">Suivant</button></div>
    </div>

    <app-modal [open]="showForm()" [title]="editId() ? 'Modifier' : 'Nouvel utilisateur'" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:0 16px">
        <div class="form-group"><label class="form-label">Prénom *</label><input type="text" class="form-input" [(ngModel)]="form.firstName"></div>
        <div class="form-group"><label class="form-label">Nom *</label><input type="text" class="form-input" [(ngModel)]="form.lastName"></div>
        <div class="form-group"><label class="form-label">Email *</label><input type="email" class="form-input" [(ngModel)]="form.email" [readOnly]="!!editId()"></div>
        <div class="form-group"><label class="form-label">Téléphone</label><input type="tel" class="form-input" [(ngModel)]="form.phone"></div>
        @if (!editId()) { <div class="form-group" style="grid-column:span 2"><label class="form-label">Mot de passe *</label><input type="password" class="form-input" [(ngModel)]="form.password"></div> }
        <div class="form-group"><label class="form-label">Rôle *</label><select class="form-input" [(ngModel)]="form.roleCode" disabled><option value="">Sélectionner…</option>@for(r of roles();track r.code){<option [value]="r.code">{{r.code}}</option>}</select></div>
        <div class="form-group"><label class="form-label">Tenant *</label><select class="form-input" [(ngModel)]="form.tenantId" disabled><option value="">Sélectionner…</option>@for(t of tenants();track t.id){<option [value]="t.id">{{t.name}}</option>}</select></div>
      </div>
    </app-modal>

    <app-confirm-dialog [open]="showDel()" title="Supprimer l&#39;utilisateur" [message]="deleteMsg()" [loading]="deleting()" (close)="showDel.set(false)" (confirm)="doDel()"></app-confirm-dialog>
  `,
  styles: [`.spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}@keyframes spin{to{transform:rotate(360deg)}}`]
})
export class UsersComponent implements OnInit {
  users = signal<any[]>([]);
  roles = signal<any[]>([]);
  tenants = signal<any[]>([]);
  total = signal(0);
  loading = signal(false);
  saving = signal(false);
  deleting = signal(false);
  showForm = signal(false);
  showDel = signal(false);
  editId = signal<string | null>(null);
  delTarget = signal<any>(null);
  search = '';
  page = 0;
  error = signal('');
  form: any = {};
  private colors = ['#3b82f6','#8b5cf6','#f97316','#14b8a6','#ef4444','#22c55e'];

  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true); this.error.set('');
    this.api.getUsers(this.page, 20, this.search.trim()).subscribe({
      next: (r) => { this.users.set(r?.content || r || []); this.total.set(r?.totalElements ?? this.users().length); this.loading.set(false); },
      error: () => { this.loading.set(false); this.users.set([]); this.error.set('Chargement des utilisateurs refuse ou indisponible. Reessayez.'); }
    });
  }

  openCreate(): void { this.editId.set(null); this.form = { firstName: '', lastName: '', email: '', phone: '', password: '', roleCode: '', tenantId: '' }; this.showForm.set(true); }
  openEdit(u: any): void { this.tenants.set([{id:u.tenantId,name:u.tenantName}]); this.roles.set((u.roles || []).map((code:string)=>({code}))); this.editId.set(u.id); this.form = { firstName: u.firstName, lastName: u.lastName, email: u.email, phone: u.phone, roleCode: u.roles?.[0] || '', tenantId: u.tenantId || u.tenant?.id || '' }; this.showForm.set(true); }

  save(): void {
    if (!this.form.firstName?.trim() || !this.form.lastName?.trim() || !this.form.email || !this.form.tenantId) { this.toast.warning('Champs obligatoires manquants.'); return; }
    if (!this.editId()) { this.toast.warning('Creation administrative indisponible.'); return; }
    if (this.saving()) return;
    this.saving.set(true);
    const call = this.editId() ? this.api.updateUser(this.editId()!, {firstName:this.form.firstName.trim(),lastName:this.form.lastName.trim(),email:this.form.email,phone:this.form.phone,tenantId:this.form.tenantId}) : this.api.createUser(this.form);
    call.subscribe({
      next: () => { this.saving.set(false); this.showForm.set(false); this.toast.success('Utilisateur sauvegardé.'); this.load(); },
      error: (e) => { this.saving.set(false); this.toast.error(e.error?.message || 'Erreur.'); }
    });
  }

  confirmDel(u: any): void { this.delTarget.set(u); this.showDel.set(true); }
  doDel(): void {
    this.deleting.set(true);
    this.api.deleteUser(this.delTarget()?.id).subscribe({
      next: () => { this.deleting.set(false); this.showDel.set(false); this.toast.success('Utilisateur supprimé.'); this.load(); },
      error: (e) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur.'); }
    });
  }

  initials(u: any): string { return `${(u.firstName?.[0]||'').toUpperCase()}${(u.lastName?.[0]||'').toUpperCase()}`; }
  avatarColor(u: any): string { const h = (u.email||'').split('').reduce((a:number,c:string) => a + c.charCodeAt(0), 0); return this.colors[h % this.colors.length]; }
  roleClass(u: any): string { const r = u.roles?.[0]; if (r === 'SUPER_ADMIN') return 'badge-danger'; if (r === 'ADMIN') return 'badge-warning'; return 'badge-primary'; }
  deleteMsg(): string { const u = this.delTarget(); return `Supprimer "${u?.firstName || ''} ${u?.lastName || ''}" ?`; }
}
