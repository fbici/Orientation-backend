import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-roles', standalone: true, imports: [CommonModule, FormsModule, RouterLink, ModalComponent, ConfirmDialogComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Roles et Permissions</h1><p>Configuration des roles et permissions du systeme</p></div></div>
        <div class="page-header-actions"><button class="btn btn-primary" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Nouveau role</button></div>
      </div>

      @if (loading()) { <div style="padding:48px;text-align:center"><div class="spinner"></div></div> } @else {
        <div class="g3 stagger">
          @for (r of roles(); track r.id || r.code) {
            <div class="card anim-fade-up">
              <div class="card-header">
                <div style="display:flex;align-items:center;gap:10px">
                  <span class="material-symbols-rounded" [style.color]="roleColor(r.code)" style="font-size:20px">shield</span>
                  <h3>{{ r.code || r.name }}</h3>
                </div>
                <div style="display:flex;gap:4px">
                  <button class="btn btn-ghost btn-icon btn-sm" (click)="openEdit(r)"><span class="material-symbols-rounded" style="font-size:16px">edit</span></button>
                  <button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDel(r)"><span class="material-symbols-rounded" style="font-size:16px;color:var(--red-500)">delete</span></button>
                </div>
              </div>
              <div class="card-body">
                <p style="font-size:.8125rem;color:var(--n-600);margin-bottom:12px">{{ r.description || '-' }}</p>
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px">
                  <span class="badge badge-primary">{{ r.userCount || 0 }} utilisateurs</span>
                </div>
                <div style="border-top:1px solid var(--n-100);padding-top:12px">
                  <span style="font-size:.6875rem;font-weight:600;color:var(--n-500);text-transform:uppercase;letter-spacing:.05em">Permissions</span>
                  <div style="display:flex;flex-wrap:wrap;gap:4px;margin-top:8px">
                    @for (p of r.permissions || []; track p.code || p) {
                      <span class="badge badge-gray">{{ p.code || p.name || p }}</span>
                    } @empty {
                      <span style="font-size:.75rem;color:var(--n-400)">Aucune permission</span>
                    }
                  </div>
                </div>
              </div>
            </div>
          } @empty {
            <div style="grid-column:span 3;text-align:center;padding:48px;color:var(--n-400)">
              <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">shield</span>
              <p style="font-weight:600;color:var(--n-600)">Aucun role</p>
              <p style="font-size:.8125rem;margin-bottom:16px">Creez votre premier role pour commencer.</p>
              <button class="btn btn-primary btn-sm" (click)="openCreate()"><span class="material-symbols-rounded">add</span>Nouveau role</button>
            </div>
          }
        </div>
      }
    </div>

    <!-- Create/Edit Role Modal -->
    <app-modal [open]="showForm()" [title]="editId() ? 'Modifier le role' : 'Nouveau role'" size="600px" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div class="form-group">
        <label class="form-label">Code du role *</label>
        <input type="text" class="form-input" [(ngModel)]="form.code" placeholder="ex: ADMIN, CANDIDAT, FORMATEUR">
        <div style="font-size:.6875rem;color:var(--n-400);margin-top:4px">Le code est unique et identifie le role dans le systeme.</div>
      </div>
      <div class="form-group">
        <label class="form-label">Description</label>
        <textarea class="form-input" rows="2" [(ngModel)]="form.description" placeholder="Description du role..."></textarea>
      </div>
      <div class="form-group">
        <label class="form-label">Permissions</label>
        <div style="max-height:300px;overflow-y:auto;border:1px solid var(--n-200);border-radius:var(--radius-sm);padding:12px">
          @for (cat of permissionCategories; track cat.name) {
            <div style="margin-bottom:16px">
              <span style="font-size:.75rem;font-weight:600;color:var(--n-600);text-transform:uppercase;letter-spacing:.05em">{{ cat.name }}</span>
              <div style="display:flex;flex-wrap:wrap;gap:6px;margin-top:8px">
                @for (perm of cat.permissions; track perm) {
                  <label class="perm-toggle">
                    <input type="checkbox" [checked]="isSelected(perm)" (change)="togglePerm(perm)">
                    <span class="perm-badge" [class.selected]="isSelected(perm)">{{ perm }}</span>
                  </label>
                }
              </div>
            </div>
          }
        </div>
        <div style="font-size:.6875rem;color:var(--n-400);margin-top:4px">{{ form.permissions.length }} permissions selectionnees</div>
      </div>
    </app-modal>

    <!-- Delete Confirmation -->
    <app-confirm-dialog [open]="showDel()" title="Supprimer le role" [message]="delMsg()" [loading]="deleting()" (close)="showDel.set(false)" (confirm)="doDel()"></app-confirm-dialog>
  `,
  styles: [`
    .spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
    .perm-toggle{cursor:pointer}.perm-toggle input{display:none}
    .perm-badge{display:inline-block;padding:3px 10px;font-size:.6875rem;font-weight:500;border-radius:9999px;border:1px solid var(--n-200);color:var(--n-500);background:var(--n-50);transition:all .15s}
    .perm-badge.selected{background:var(--brand-100);color:var(--brand-dark);border-color:var(--brand-200)}
    .perm-toggle:hover .perm-badge{border-color:var(--brand-300)}
  `]
})
export class RolesComponent implements OnInit {
  roles = signal<any[]>([]);
  loading = signal(false);
  saving = signal(false);
  deleting = signal(false);
  showForm = signal(false);
  showDel = signal(false);
  editId = signal<string | null>(null);
  delTarget = signal<any>(null);
  form: any = { code: '', description: '', permissions: [] as string[] };

  permissionCategories = [
    { name: 'Utilisateurs', permissions: ['users:read', 'users:create', 'users:update', 'users:delete'] },
    { name: 'Universites', permissions: ['universities:read', 'universities:create', 'universities:update', 'universities:delete'] },
    { name: 'Programmes', permissions: ['programs:read', 'programs:create', 'programs:update', 'programs:delete'] },
    { name: 'Candidats', permissions: ['candidates:read', 'candidates:create', 'candidates:update'] },
    { name: 'Recommandations', permissions: ['recommendations:read', 'recommendations:generate', 'recommendations:simulate'] },
    { name: 'Documents', permissions: ['documents:read', 'documents:upload', 'documents:delete'] },
    { name: 'Imports', permissions: ['imports:read', 'imports:create', 'imports:rollback'] },
    { name: 'Systeme', permissions: ['settings:read', 'settings:update', 'audit:read', 'monitoring:read'] },
  ];

  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.getRoles().subscribe({
      next: (r) => { this.roles.set(Array.isArray(r) ? r : r?.content || []); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  openCreate(): void { this.editId.set(null); this.form = { code: '', description: '', permissions: [] }; this.showForm.set(true); }
  openEdit(r: any): void { this.editId.set(r.id); this.form = { code: r.code || r.name, description: r.description || '', permissions: (r.permissions || []).map((p: any) => p.code || p.name || p) }; this.showForm.set(true); }

  isSelected(perm: string): boolean { return this.form.permissions.includes(perm); }
  togglePerm(perm: string): void {
    if (this.isSelected(perm)) { this.form.permissions = this.form.permissions.filter((p: string) => p !== perm); }
    else { this.form.permissions.push(perm); }
  }

  save(): void {
    if (!this.form.code) { this.toast.warning('Code requis.'); return; }
    this.saving.set(true);
    const payload = { ...this.form };
    const call = this.editId() ? this.api.updateRole(this.editId()!, payload) : this.api.createRole(payload);
    call.subscribe({
      next: () => { this.saving.set(false); this.showForm.set(false); this.toast.success('Role sauvegarde.'); this.load(); },
      error: (e: any) => { this.saving.set(false); this.toast.error(e.error?.message || 'Erreur.'); }
    });
  }

  confirmDel(r: any): void { this.delTarget.set(r); this.showDel.set(true); }
  delMsg(): string { return `Supprimer le role "${this.delTarget()?.code || ''}" ?`; }
  doDel(): void {
    this.deleting.set(true);
    this.api.deleteRole(this.delTarget()?.id).subscribe({
      next: () => { this.deleting.set(false); this.showDel.set(false); this.toast.success('Role supprime.'); this.load(); },
      error: (e: any) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur.'); }
    });
  }

  roleColor(code: string): string {
    if (code === 'SUPER_ADMIN') return '#ef4444';
    if (code === 'ADMIN') return '#f97316';
    return '#3b82f6';
  }
}
