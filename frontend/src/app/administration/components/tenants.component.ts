import { Component } from '@angular/core'; import { CommonModule } from '@angular/common'; import { RouterLink } from '@angular/router';
@Component({ selector: 'app-tenants', standalone: true, imports: [CommonModule, RouterLink], template: `
<div class="anim-fade-up"><div class="page-header"><div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Tenants</h1><p>Espaces isolés multi-organisation</p></div></div><button class="btn btn-primary"><span class="material-symbols-rounded">add</span>Nouveau tenant</button></div>
<div class="g3 stagger">@for(t of tenants;track t.name){<div class="card anim-fade-up"><div class="card-body"><div style="display:flex;align-items:center;gap:12px;margin-bottom:16px"><div class="avatar" [style.background]="t.color" style="width:44px;height:44px;font-size:.9rem">{{t.ini}}</div><div><div style="font-weight:600;color:var(--n-900)">{{t.name}}</div><div style="font-size:.75rem;color:var(--n-500)">{{t.code}}</div></div></div><div style="display:flex;justify-content:space-between;font-size:.8125rem;color:var(--n-600);padding:8px 0;border-top:1px solid var(--n-100)"><span>{{t.users}} utilisateurs</span><span class="badge" [class]="t.active?'badge-success':'badge-gray'">{{t.active?'Actif':'Inactif'}}</span></div></div></div>}</div></div>`, styles: [`:host{display:block}`] })
export class TenantsComponent {
  tenants = [
    { name:'Orientation Bénin', code:'orient-bj', ini:'OB', color:'#3b82f6', users:1240, active:true },
    { name:'UAC — Faculté des Sciences', code:'uac-sciences', ini:'US', color:'#22c55e', users:320, active:true },
    { name:'UNB — Droit', code:'unb-droit', ini:'UD', color:'#f97316', users:180, active:true },
  ];
}
