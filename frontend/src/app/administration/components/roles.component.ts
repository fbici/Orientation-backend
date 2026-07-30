import { Component } from '@angular/core'; import { CommonModule } from '@angular/common'; import { RouterLink } from '@angular/router';
@Component({ selector: 'app-roles', standalone: true, imports: [CommonModule, RouterLink], template: `
<div class="anim-fade-up"><div class="page-header"><div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Rôles & Permissions</h1><p>Gestion des rôles et permissions système</p></div></div><button class="btn btn-primary"><span class="material-symbols-rounded">add</span>Nouveau rôle</button></div>
<div class="g3 stagger">@for(role of roles;track role.name){<div class="card anim-fade-up"><div class="card-header"><div style="display:flex;align-items:center;gap:10px"><span class="material-symbols-rounded" [style.color]="role.color" style="font-size:20px">{{role.icon}}</span><h3>{{role.name}}</h3></div><span class="badge badge-primary">{{role.users}} users</span></div><div class="card-body"><p style="font-size:.8125rem;color:var(--n-600);margin-bottom:16px">{{role.desc}}</p><div style="display:flex;flex-wrap:wrap;gap:6px">@for(perm of role.permissions;track perm){<span class="badge badge-gray">{{perm}}</span>}</div></div></div>}</div></div>`, styles: [`:host{display:block}`] })
export class RolesComponent {
  roles = [
    { name:'SUPER_ADMIN', icon:'admin_panel_settings', color:'#ef4444', users:2, desc:'Accès total au système, gestion de tous les tenants.', permissions:['Tous les accès','Gestion tenants','Gestion rôles'] },
    { name:'ADMIN', icon:'shield', color:'#f97316', users:8, desc:'Administration d\'un tenant spécifique.', permissions:['Gestion utilisateurs','Imports','Rapports','Configuration'] },
    { name:'CANDIDAT', icon:'person', color:'#3b82f6', users:146, desc:'Accès candidat — profil, recommandations, simulation.', permissions:['Profil','Recommandations','Simulation','Documents'] },
  ];
}
