import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';

@Component({
  selector: 'app-roles', standalone: true, imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Rôles & Permissions</h1><p>Gestion des rôles système</p></div></div>
      </div>
      <div class="g3 stagger">
        @for (r of roles(); track r.id || r.code) {
          <div class="card anim-fade-up">
            <div class="card-header">
              <div style="display:flex;align-items:center;gap:10px"><span class="material-symbols-rounded" style="font-size:20px;color:var(--brand)">shield</span><h3>{{ r.code || r.name }}</h3></div>
              <span class="badge badge-primary">{{ r.userCount || 0 }} users</span>
            </div>
            <div class="card-body">
              <p style="font-size:.8125rem;color:var(--n-600);margin-bottom:12px">{{ r.description || '—' }}</p>
              <div style="display:flex;flex-wrap:wrap;gap:4px">
                @for (p of r.permissions || []; track p.code || p) { <span class="badge badge-gray">{{ p.code || p }}</span> }
              </div>
            </div>
          </div>
        } @empty {
          <div style="grid-column:span 3;text-align:center;padding:48px;color:var(--n-400)">Aucun rôle configuré</div>
        }
      </div>
    </div>
  `
})
export class RolesComponent implements OnInit {
  roles = signal<any[]>([]);
  constructor(private api: ApiService) {}
  ngOnInit(): void { this.api.getRoles().subscribe({ next: (r) => this.roles.set(Array.isArray(r) ? r : r?.content || []), error: () => {} }); }
}
