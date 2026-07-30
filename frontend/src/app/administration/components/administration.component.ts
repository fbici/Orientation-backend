import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

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
          <a [routerLink]="tab.route" routerLinkActive="active" class="tab">
            <span class="material-symbols-rounded" style="font-size:18px">{{ tab.icon }}</span> {{ tab.label }}
          </a>
        }
      </div>
      <div class="g4 stagger" style="margin-bottom:22px">
        @for (k of kpis; track k.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div>
            <div class="stat-content"><div class="stat-label">{{ k.label }}</div><div class="stat-value">{{ k.val }}</div></div>
          </div>
        }
      </div>
      <div class="card anim-fade-up">
        <div class="card-header">
          <h3>Utilisateurs récents</h3>
          <div style="display:flex;gap:8px"><input type="text" class="form-input" style="width:220px" placeholder="Rechercher…"><button class="btn btn-secondary btn-sm"><span class="material-symbols-rounded" style="font-size:16px">filter_list</span></button></div>
        </div>
        <div class="card-body" style="padding:0">
          <table class="data-table">
            <thead><tr><th>Utilisateur</th><th>Email</th><th>Rôle</th><th>Tenant</th><th>Statut</th><th>Dernière connexion</th><th></th></tr></thead>
            <tbody>
              @for (u of users; track u.email) {
                <tr>
                  <td><div style="display:flex;align-items:center;gap:10px"><div class="avatar" [style.background]="u.color">{{ u.ini }}</div><span style="font-weight:600">{{ u.name }}</span></div></td>
                  <td style="font-size:.8125rem;color:var(--n-600)">{{ u.email }}</td>
                  <td><span class="badge" [class]="u.rCls">{{ u.role }}</span></td>
                  <td style="font-size:.8125rem">{{ u.tenant }}</td>
                  <td><div style="display:flex;align-items:center;gap:6px"><span class="dot" [class]="u.online?'green':'gray'"></span><span style="font-size:.8125rem;color:var(--n-600)">{{ u.online?'En ligne':'Hors ligne' }}</span></div></td>
                  <td style="font-size:.8125rem;color:var(--n-500)">{{ u.last }}</td>
                  <td><div style="display:flex;gap:2px"><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">edit</span></button><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">more_vert</span></button></div></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host{display:block}
    .tabs{display:flex;gap:2px;border-bottom:1px solid var(--n-200);margin-bottom:24px}
    .tab{display:flex;align-items:center;gap:6px;padding:10px 16px;font-size:.8125rem;font-weight:500;color:var(--n-500);background:none;border:none;border-bottom:2px solid transparent;cursor:pointer;transition:all var(--dur-fast);margin-bottom:-1px;text-decoration:none;font-family:inherit}
    .tab:hover{color:var(--n-700)}.tab.active{color:var(--brand);border-bottom-color:var(--brand);font-weight:600}
  `]
})
export class AdministrationComponent {
  tabs = [
    { icon: 'group', label: 'Utilisateurs', route: '/administration/users' },
    { icon: 'shield', label: 'Rôles', route: '/administration/roles' },
    { icon: 'business', label: 'Organisations', route: '/administration/organizations' },
    { icon: 'apartment', label: 'Tenants', route: '/administration/tenants' },
    { icon: 'groups', label: 'Équipes', route: '/administration/teams' },
    { icon: 'corporate_fare', label: 'Départements', route: '/administration/departments' },
  ];
  kpis = [
    { icon: 'group', label: 'Utilisateurs', val: '156', g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'shield', label: 'Rôles', val: '6', g: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
    { icon: 'business', label: 'Organisations', val: '12', g: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'apartment', label: 'Tenants actifs', val: '8', g: 'linear-gradient(135deg,#f97316,#ea580c)' },
  ];
  users = [
    { name: 'Admin Principal', ini: 'AP', color: '#3b82f6', email: 'admin@orientation.com', role: 'SUPER_ADMIN', rCls: 'badge-danger', tenant: 'Orientation Bénin', online: true, last: 'Maintenant' },
    { name: 'Marie Koudjo', ini: 'MK', color: '#8b5cf6', email: 'marie.k@univ.edu', role: 'ADMIN', rCls: 'badge-warning', tenant: 'UAC', online: true, last: 'Il y a 10 min' },
    { name: 'Jean Dupont', ini: 'JD', color: '#22c55e', email: 'jean.d@student.com', role: 'CANDIDAT', rCls: 'badge-primary', tenant: 'Orientation Bénin', online: false, last: 'Il y a 2h' },
    { name: 'Prof. Diallo', ini: 'PD', color: '#f97316', email: 'prof.diallo@univ.edu', role: 'ADMIN', rCls: 'badge-warning', tenant: 'UNB', online: false, last: 'Hier' },
    { name: 'Fatima Bello', ini: 'FB', color: '#14b8a6', email: 'fatima.b@student.com', role: 'CANDIDAT', rCls: 'badge-primary', tenant: 'Orientation Bénin', online: true, last: 'Il y a 30 min' },
  ];
}
