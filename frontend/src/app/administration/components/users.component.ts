import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="users">
      <div class="page-header">
        <h1>Utilisateurs</h1>
        <button>+ Nouvel Utilisateur</button>
      </div>

      <div class="filters">
        <input type="text" placeholder="Rechercher un utilisateur..." [(ngModel)]="searchQuery">
        <select [(ngModel)]="statusFilter">
          <option value="">Tous les statuts</option>
          <option value="ACTIVE">Actif</option>
          <option value="INACTIVE">Inactif</option>
        </select>
      </div>

      <div class="data-table">
        <div class="table-header">
          <span class="col-avatar">Avatar</span>
          <span class="col-name">Nom</span>
          <span class="col-email">Email</span>
          <span class="col-role">Rôle</span>
          <span class="col-status">Statut</span>
          <span class="col-actions">Actions</span>
        </div>
        @for (user of users(); track user.id) {
          <div class="table-row">
            <span class="col-avatar">
              <div class="avatar">{{ user.firstName?.charAt(0) }}{{ user.lastName?.charAt(0) }}</div>
            </span>
            <span class="col-name">{{ user.firstName }} {{ user.lastName }}</span>
            <span class="col-email">{{ user.email }}</span>
            <span class="col-role">{{ user.roles?.[0] }}</span>
            <span class="col-status">
              <span class="status-badge" [class.active]="user.status === 'ACTIVE'">
                {{ user.status }}
              </span>
            </span>
            <span class="col-actions">
              <button class="btn-icon">✏️</button>
              <button class="btn-icon">🔑</button>
            </span>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .users { max-width: 1400px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
    .page-header h1 { color: #1a1a2e; margin: 0; }
    .page-header button { background: #1a1a2e; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
    .filters { display: flex; gap: 15px; margin-bottom: 20px; }
    .filters input, .filters select { padding: 10px; border: 1px solid #ddd; border-radius: 6px; }
    .data-table { background: white; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); overflow: hidden; }
    .table-header { display: grid; grid-template-columns: 60px 2fr 2fr 1fr 1fr 1fr; padding: 15px 20px; background: #1a1a2e; color: white; font-weight: 500; }
    .table-row { display: grid; grid-template-columns: 60px 2fr 2fr 1fr 1fr 1fr; padding: 15px 20px; border-bottom: 1px solid #f0f0f0; align-items: center; }
    .table-row:hover { background: #f8f9fa; }
    .avatar { width: 40px; height: 40px; background: #1a1a2e; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.85rem; }
    .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 0.8rem; }
    .status-badge.active { background: #d4edda; color: #155724; }
    .btn-icon { background: none; border: none; cursor: pointer; padding: 5px; font-size: 1.1rem; }
  `]
})
export class UsersComponent implements OnInit {
  users = signal<any[]>([]);
  searchQuery = '';
  statusFilter = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.api.getUsers().subscribe({
      next: (data) => this.users.set(data.content || [])
    });
  }
}
