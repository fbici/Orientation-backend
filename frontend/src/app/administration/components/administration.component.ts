import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-administration',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Administration</h1><p>Gestion des utilisateurs, rôles, organisations et équipes</p></div>
        <div class="page-header-actions"><button class="btn btn-primary"><span class="material-symbols-rounded">person_add</span>Ajouter un utilisateur</button></div>
      </div>
      <div class="tabs">
        @for (tab of tabs; track tab.route) {
          <a [routerLink]="tab.route" routerLinkActive="active" class="tab"><span class="material-symbols-rounded" style="font-size:18px">{{ tab.icon }}</span> {{ tab.label }}</a>
        }
      </div>

      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:20px;margin-bottom:22px" class="stagger">
        <div class="stat-card anim-fade-up">
          <div class="stat-icon" style="background:linear-gradient(135deg,#3b82f6,#1d4ed8)"><span class="material-symbols-rounded filled">group</span></div>
          <div class="stat-content"><div class="stat-label">Utilisateurs</div><div class="stat-value">{{ totalUsers() }}</div></div>
        </div>
        <div class="stat-card anim-fade-up">
          <div class="stat-icon" style="background:linear-gradient(135deg,#8b5cf6,#6d28d9)"><span class="material-symbols-rounded filled">shield</span></div>
          <div class="stat-content"><div class="stat-label">Rôles</div><div class="stat-value">{{ roles().length }}</div></div>
        </div>
        <div class="stat-card anim-fade-up">
          <div class="stat-icon" style="background:linear-gradient(135deg,#22c55e,#15803d)"><span class="material-symbols-rounded filled">business</span></div>
          <div class="stat-content"><div class="stat-label">Organisations</div><div class="stat-value">{{ orgs().length }}</div></div>
        </div>
        <div class="stat-card anim-fade-up">
          <div class="stat-icon" style="background:linear-gradient(135deg,#f97316,#ea580c)"><span class="material-symbols-rounded filled">apartment</span></div>
          <div class="stat-content"><div class="stat-label">Tenants</div><div class="stat-value">{{ tenants().length }}</div></div>
        </div>
      </div>

      <div class="card anim-fade-up">
        <div class="card-header">
          <h3>Utilisateurs</h3>
          <input type="text" class="form-input" style="width:220px" placeholder="Rechercher…" [(ngModel)]="filter">
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner-lg"></div></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Utilisateur</th><th>Email</th><th>Rôle</th><th>Tenant</th><th>Statut</th><th></th></tr></thead>
              <tbody>
                @for (u of filteredUsers(); track u.id) {
                  <tr>
                    <td><div style="display:flex;align-items:center;gap:10px"><div class="avatar" [style.background]="getAvatarColor(u)">{{ getInitials(u) }}</div><span style="font-weight:600">{{ u.firstName }} {{ u.lastName }}</span></div></td>
                    <td style="font-size:.8125rem;color:var(--n-600)">{{ u.email }}</td>
                    <td><span class="badge" [class]="roleClass(u)">{{ u.roles?.[0] || 'N/A' }}</span></td>
                    <td style="font-size:.8125rem">{{ u.tenantName || u.tenantId || '—' }}</td>
                    <td><div style="display:flex;align-items:center;gap:6px"><span class="dot" [class]="u.enabled !== false ? 'green' : 'gray'"></span><span style="font-size:.8125rem;color:var(--n-600)">{{ u.enabled !== false ? 'Actif' : 'Inactif' }}</span></div></td>
                    <td><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">more_vert</span></button></td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" style="text-align:center;padding:48px;color:var(--n-400)">Aucun utilisateur trouvé</td></tr>
                }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host{display:block}
    .tabs{display:flex;gap:2px;border-bottom:1px solid var(--n-200);margin-bottom:24px}
    .tab{display:flex;align-items:center;gap:6px;padding:10px 16px;font-size:.8125rem;font-weight:500;color:var(--n-500);background:none;border:none;border-bottom:2px solid transparent;cursor:pointer;transition:all var(--dur-fast);margin-bottom:-1px;text-decoration:none;font-family:inherit}
    .tab:hover{color:var(--n-700)}.tab.active{color:var(--brand);border-bottom-color:var(--brand);font-weight:600}
    .spinner-lg{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class AdministrationComponent implements OnInit {
  tabs = [
    { icon: 'group', label: 'Utilisateurs', route: '/administration/users' },
    { icon: 'shield', label: 'Rôles', route: '/administration/roles' },
    { icon: 'business', label: 'Organisations', route: '/administration/organizations' },
    { icon: 'apartment', label: 'Tenants', route: '/administration/tenants' },
    { icon: 'groups', label: 'Équipes', route: '/administration/teams' },
    { icon: 'corporate_fare', label: 'Départements', route: '/administration/departments' },
  ];

  filter = '';
  loading = signal(false);
  users = signal<any[]>([]);
  totalUsers = signal(0);
  roles = signal<any[]>([]);
  orgs = signal<any[]>([]);
  tenants = signal<any[]>([]);

  private colors = ['#3b82f6','#8b5cf6','#f97316','#14b8a6','#ef4444','#22c55e','#ec4899'];

  constructor(private api: ApiService, public auth: AuthService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading.set(true);
    this.api.getUsers().subscribe({
      next: (res) => {
        const items = res?.content || res || [];
        this.users.set(Array.isArray(items) ? items : []);
        this.totalUsers.set(res?.totalElements ?? this.users().length);
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
    this.api.getRoles().subscribe({ next: (res) => { this.roles.set(Array.isArray(res) ? res : res?.content || []); }, error: () => {} });
    this.api.getOrganizations().subscribe({ next: (res) => { this.orgs.set(Array.isArray(res) ? res : res?.content || []); }, error: () => {} });
    this.api.getTenants().subscribe({ next: (res) => { this.tenants.set(Array.isArray(res) ? res : res?.content || []); }, error: () => {} });
  }

  filteredUsers(): any[] {
    if (!this.filter) return this.users();
    const q = this.filter.toLowerCase();
    return this.users().filter(u =>
      `${u.firstName} ${u.lastName}`.toLowerCase().includes(q) ||
      (u.email || '').toLowerCase().includes(q)
    );
  }

  getInitials(u: any): string {
    return `${(u.firstName?.[0] || '').toUpperCase()}${(u.lastName?.[0] || '').toUpperCase()}`;
  }

  getAvatarColor(u: any): string {
    const hash = (u.email || '').split('').reduce((a: number, c: string) => a + c.charCodeAt(0), 0);
    return this.colors[hash % this.colors.length];
  }

  roleClass(u: any): string {
    const role = u.roles?.[0] || '';
    if (role === 'SUPER_ADMIN') return 'badge-danger';
    if (role === 'ADMIN') return 'badge-warning';
    return 'badge-primary';
  }
}
