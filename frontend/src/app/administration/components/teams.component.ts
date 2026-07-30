import { Component } from '@angular/core'; import { CommonModule } from '@angular/common'; import { RouterLink } from '@angular/router';
@Component({ selector: 'app-teams', standalone: true, imports: [CommonModule, RouterLink], template: `
<div class="anim-fade-up"><div class="page-header"><div style="display:flex;align-items:center;gap:12px"><a routerLink="/administration" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a><div><h1>Équipes</h1><p>Gestion des équipes de travail</p></div></div><button class="btn btn-primary"><span class="material-symbols-rounded">group_add</span>Nouvelle équipe</button></div>
<div class="card"><div class="card-body"><div class="empty-state"><span class="material-symbols-rounded">groups</span><h3>Aucune équipe</h3><p>Connectez l'API pour charger les équipes</p></div></div></div></div>`, styles: [`:host{display:block}`] })
export class TeamsComponent {}
