import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-tenants', standalone: true, imports: [CommonModule, FormsModule, RouterLink, ModalComponent, ConfirmDialogComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Tenants</h1><p>Espaces isolés multi-organisation</p></div></div>
        <div class="page-header-actions"><button class="btn btn-primary" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Ajouter</button></div>
      </div>
      <div class="g3 stagger">
        @for (t of tenants(); track t.id) {
          <div class="card anim-fade-up">
            <div class="card-body">
              <div style="display:flex;align-items:center;gap:12px;margin-bottom:14px">
                <div class="avatar" [style.background]="avatarColor(t)" style="width:44px;height:44px;font-size:.9rem">{{ initials(t) }}</div>
                <div style="flex:1"><div style="font-weight:600;color:var(--n-900)">{{ t.name }}</div><div style="font-size:.75rem;color:var(--n-500)">{{ t.code || '—' }}</div></div>
                <div style="display:flex;gap:2px">
                  <button class="btn btn-ghost btn-icon btn-sm" (click)="openEdit(t)"><span class="material-symbols-rounded" style="font-size:16px">edit</span></button>
                  <button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDel(t)"><span class="material-symbols-rounded" style="font-size:16px;color:var(--red-500)">delete</span></button>
                </div>
              </div>
              <div style="display:flex;justify-content:space-between;font-size:.8125rem;color:var(--n-600);padding:8px 0;border-top:1px solid var(--n-100)">
                <span>{{ t.userCount || 0 }} utilisateurs</span>
                <span class="badge" [class]="t.active !== false ? 'badge-success' : 'badge-gray'">{{ t.active !== false ? 'Actif' : 'Inactif' }}</span>
              </div>
            </div>
          </div>
        } @empty {
          <div style="grid-column:span 3;text-align:center;padding:48px;color:var(--n-400)">Aucun tenant</div>
        }
      </div>
    </div>
    <app-modal [open]="showForm()" [title]="editId() ? 'Modifier' : 'Nouveau tenant'" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div class="form-group"><label class="form-label">Nom *</label><input type="text" class="form-input" [(ngModel)]="form.name"></div>
      <div class="form-group"><label class="form-label">Code *</label><input type="text" class="form-input" [(ngModel)]="form.code" placeholder="ex: orient-bj"></div>
      <div class="form-group"><label class="form-label">Organisation</label><select class="form-input" [(ngModel)]="form.organizationId"><option value="">Sélectionner…</option>@for(o of orgs();track o.id){<option [value]="o.id">{{o.name}}</option>}</select></div>
    </app-modal>
    <app-confirm-dialog [open]="showDel()" title="Supprimer" [message]="'Supprimer « ' + (delTarget()?.name || '') + ' » ?'" [loading]="deleting()" (close)="showDel.set(false)" (confirm)="doDel()"></app-confirm-dialog>
  `
})
export class TenantsComponent implements OnInit {
  tenants = signal<any[]>([]); orgs = signal<any[]>([]);
  loading = signal(false); saving = signal(false); deleting = signal(false);
  showForm = signal(false); showDel = signal(false); editId = signal<string | null>(null); delTarget = signal<any>(null);
  form: any = {};
  private colors = ['#3b82f6','#8b5cf6','#f97316','#14b8a6','#ef4444','#22c55e'];
  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); this.api.getOrganizations().subscribe({ next: (r) => this.orgs.set(r?.content || r || []), error: () => {} }); }
  load(): void { this.loading.set(true); this.api.getTenants().subscribe({ next: (r) => { this.tenants.set(r?.content || r || []); this.loading.set(false); }, error: () => this.loading.set(false) }); }
  openCreate(): void { this.editId.set(null); this.form = { name: '', code: '', organizationId: '' }; this.showForm.set(true); }
  openEdit(t: any): void { this.editId.set(t.id); this.form = { name: t.name, code: t.code, organizationId: t.organizationId || '' }; this.showForm.set(true); }
  save(): void { if (!this.form.name) { this.toast.warning('Nom requis.'); return; } this.saving.set(true); const call = this.editId() ? this.api.updateTenant(this.editId()!, this.form) : this.api.createTenant(this.form); call.subscribe({ next: () => { this.saving.set(false); this.showForm.set(false); this.toast.success('Sauvegardé.'); this.load(); }, error: (e) => { this.saving.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
  confirmDel(t: any): void { this.delTarget.set(t); this.showDel.set(true); }
  doDel(): void { this.deleting.set(true); this.api.deleteTenant(this.delTarget()?.id).subscribe({ next: () => { this.deleting.set(false); this.showDel.set(false); this.toast.success('Supprimé.'); this.load(); }, error: (e) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
  initials(t: any): string { return (t.name || '').split(' ').map((w: string) => w[0]).join('').substring(0, 2).toUpperCase(); }
  avatarColor(t: any): string { const h = (t.name || '').split('').reduce((a: number, c: string) => a + c.charCodeAt(0), 0); return this.colors[h % this.colors.length]; }
}
