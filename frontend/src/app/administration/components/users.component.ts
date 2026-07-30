import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
@Component({ selector: 'app-users', standalone: true, imports: [CommonModule, RouterLink], template: `
<div class="anim-fade-up"><div class="page-header"><div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Gestion des utilisateurs</h1><p>Créer, modifier et gérer les comptes utilisateurs</p></div></div><button class="btn btn-primary"><span class="material-symbols-rounded">person_add</span>Nouvel utilisateur</button></div>
<div class="card"><div class="card-body" style="padding:0"><table class="data-table"><thead><tr><th>Nom</th><th>Email</th><th>Rôle</th><th>Statut</th><th>Actions</th></tr></thead><tbody><tr><td colspan="5" style="text-align:center;padding:48px;color:var(--n-400)"><span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px">group</span>Liste complète — connectez l'API pour charger les données</td></tr></tbody></table></div></div></div>`, styles: [`:host{display:block}`] })
export class UsersComponent {}
