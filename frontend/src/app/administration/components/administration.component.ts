import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-administration',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="administration">
      <h1>Administration</h1>
      <div class="admin-grid">
        <a routerLink="/administration/organizations" class="admin-card">
          <div class="card-icon">🏢</div>
          <h3>Organizations</h3>
          <p>Gérer les organisations et leurs configurations</p>
          <div class="card-stats">
            <span>CRUD</span>
            <span>Recherche</span>
            <span>Filtres</span>
          </div>
        </a>
        <a routerLink="/administration/tenants" class="admin-card">
          <div class="card-icon">🏠</div>
          <h3>Tenants</h3>
          <p>Gérer les tenants et les quotas</p>
          <div class="card-stats">
            <span>CRUD</span>
            <span>Quota</span>
            <span>Plan</span>
          </div>
        </a>
        <a routerLink="/administration/users" class="admin-card">
          <div class="card-icon">👥</div>
          <h3>Utilisateurs</h3>
          <p>Gérer les utilisateurs et leurs accès</p>
          <div class="card-stats">
            <span>CRUD</span>
            <span>Avatar</span>
            <span>Sessions</span>
          </div>
        </a>
        <a routerLink="/administration/roles" class="admin-card">
          <div class="card-icon">🔑</div>
          <h3>Rôles & Permissions</h3>
          <p>Gérer les rôles et la matrice de permissions</p>
          <div class="card-stats">
            <span>CRUD</span>
            <span>Cloner</span>
            <span>Export</span>
          </div>
        </a>
        <a routerLink="/administration/departments" class="admin-card">
          <div class="card-icon">🏛️</div>
          <h3>Départements</h3>
          <p>Gérer l'arborescence des départements</p>
          <div class="card-stats">
            <span>Arbre</span>
            <span>Drag&Drop</span>
            <span>Recherche</span>
          </div>
        </a>
        <a routerLink="/administration/teams" class="admin-card">
          <div class="card-icon">👨‍👩‍👧‍👦</div>
          <h3>Équipes</h3>
          <p>Gérer les équipes et les membres</p>
          <div class="card-stats">
            <span>Membres</span>
            <span>Leader</span>
            <span>Historique</span>
          </div>
        </a>
      </div>
    </div>
  `,
  styles: [`
    .administration { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .admin-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
    .admin-card { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); text-decoration: none; color: inherit; transition: all 0.2s; border: 1px solid transparent; }
    .admin-card:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(0,0,0,0.12); border-color: #1a1a2e; }
    .card-icon { font-size: 2.5rem; margin-bottom: 15px; }
    .admin-card h3 { color: #1a1a2e; margin: 0 0 10px; font-size: 1.2rem; }
    .admin-card p { color: #666; margin: 0 0 15px; font-size: 0.9rem; }
    .card-stats { display: flex; gap: 8px; flex-wrap: wrap; }
    .card-stats span { background: #f0f4ff; color: #1a1a2e; padding: 4px 10px; border-radius: 12px; font-size: 0.75rem; }
  `]
})
export class AdministrationComponent {}
