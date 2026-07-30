import { Component } from '@angular/core'; import { CommonModule } from '@angular/common'; import { RouterLink } from '@angular/router';
@Component({ selector: 'app-organizations', standalone: true, imports: [CommonModule, RouterLink], template: `
<div class="anim-fade-up"><div class="page-header"><div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Organisations</h1><p>Gestion des organisations partenaires</p></div></div><button class="btn btn-primary"><span class="material-symbols-rounded">add</span>Nouvelle organisation</button></div>
<div class="card"><div class="card-body" style="padding:0"><table class="data-table"><thead><tr><th>Organisation</th><th>Pays</th><th>Tenants</th><th>Statut</th><th>Actions</th></tr></thead><tbody>@for(org of orgs;track org.name){<tr><td style="font-weight:500">{{org.name}}</td><td>{{org.country}}</td><td>{{org.tenants}}</td><td><span class="badge badge-success">Active</span></td><td><button class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded" style="font-size:18px">edit</span></button></td></tr>}</tbody></table></div></div></div>`, styles: [`:host{display:block}`] })
export class OrganizationsComponent {
  orgs = [
    { name:"Ministère de l'Enseignement Supérieur", country:'Bénin', tenants:3 },
    { name:"Université d'Abomey-Calavi", country:'Bénin', tenants:1 },
    { name:'Université de Parakou', country:'Bénin', tenants:1 },
  ];
}
