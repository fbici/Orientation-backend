import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';

@Component({
  selector: 'app-teams', standalone: true, imports: [CommonModule, FormsModule, RouterLink, ModalComponent, ConfirmDialogComponent],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Équipes</h1><p>Gestion des équipes de travail</p></div></div>
        <div class="page-header-actions"><button class="btn btn-primary" (click)="openCreate()"><span class="material-symbols-rounded">group_add</span>Ajouter</button></div>
      </div>
      <div class="g3 stagger">
        @for (t of teams(); track t.id) {
          <div class="card anim-fade-up">
            <div class="card-body">
              <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px">
                <div style="display:flex;align-items:center;gap:10px"><span class="material-symbols-rounded" style="font-size:20px;color:var(--brand)">groups</span><span style="font-weight:600;color:var(--n-900)">{{ t.name }}</span></div>
                <div style="display:flex;gap:2px"><button class="btn btn-ghost btn-icon btn-sm" (click)="openEdit(t)"><span class="material-symbols-rounded" style="font-size:16px">edit</span></button><button class="btn btn-ghost btn-icon btn-sm" (click)="confirmDel(t)"><span class="material-symbols-rounded" style="font-size:16px;color:var(--red-500)">delete</span></button></div>
              </div>
              <p style="font-size:.8125rem;color:var(--n-600)">{{ t.description || '—' }}</p>
              <div style="font-size:.75rem;color:var(--n-500);margin-top:8px">{{ t.memberCount || 0 }} membres</div>
            </div>
          </div>
        } @empty {
          <div style="grid-column:span 3;text-align:center;padding:48px;color:var(--n-400)">
            <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">groups</span>Aucune équipe
          </div>
        }
      </div>
    </div>
    <app-modal [open]="showForm()" [title]="editId() ? 'Modifier' : 'Nouvelle équipe'" [confirmLoading]="saving()" (close)="showForm.set(false)" (confirm)="save()">
      <div class="form-group"><label class="form-label">Nom *</label><input type="text" class="form-input" [(ngModel)]="form.name"></div>
      <div class="form-group"><label class="form-label">Description</label><textarea class="form-input" rows="3" [(ngModel)]="form.description"></textarea></div>
    </app-modal>
    <app-confirm-dialog [open]="showDel()" title="Supprimer" [message]="'Supprimer l\'équipe « ' + (delTarget()?.name || '') + ' » ?'" [loading]="deleting()" (close)="showDel.set(false)" (confirm)="doDel()"></app-confirm-dialog>
  `
})
export class TeamsComponent implements OnInit {
  teams = signal<any[]>([]);
  loading = signal(false); saving = signal(false); deleting = signal(false);
  showForm = signal(false); showDel = signal(false); editId = signal<string | null>(null); delTarget = signal<any>(null);
  form: any = {};
  constructor(private api: ApiService, private toast: ToastService) {}
  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.api.getTeams().subscribe({ next: (r) => { this.teams.set(r?.content || r || []); this.loading.set(false); }, error: () => this.loading.set(false) }); }
  openCreate(): void { this.editId.set(null); this.form = { name: '', description: '' }; this.showForm.set(true); }
  openEdit(t: any): void { this.editId.set(t.id); this.form = { name: t.name, description: t.description }; this.showForm.set(true); }
  save(): void { if (!this.form.name) { this.toast.warning('Nom requis.'); return; } this.saving.set(true); const call = this.editId() ? this.api.updateTeam(this.editId()!, this.form) : this.api.createTeam(this.form); call.subscribe({ next: () => { this.saving.set(false); this.showForm.set(false); this.toast.success('Sauvegardé.'); this.load(); }, error: (e) => { this.saving.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
  confirmDel(t: any): void { this.delTarget.set(t); this.showDel.set(true); }
  doDel(): void { this.deleting.set(true); this.api.deleteTeam(this.delTarget()?.id).subscribe({ next: () => { this.deleting.set(false); this.showDel.set(false); this.toast.success('Supprimé.'); this.load(); }, error: (e) => { this.deleting.set(false); this.toast.error(e.error?.message || 'Erreur.'); } }); }
}
