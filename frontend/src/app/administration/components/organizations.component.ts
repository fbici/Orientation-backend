import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-organizations',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="organizations">
      <div class="page-header">
        <h1>Organizations</h1>
        <button (click)="showCreateForm.set(true)">+ Nouvelle Organisation</button>
      </div>

      @if (showCreateForm()) {
        <div class="create-form">
          <h3>Créer une Organisation</h3>
          <div class="form-grid">
            <div class="form-group">
              <label>Nom</label>
              <input type="text" [(ngModel)]="newOrg.name" placeholder="Nom de l'organisation">
            </div>
            <div class="form-group">
              <label>Code</label>
              <input type="text" [(ngModel)]="newOrg.code" placeholder="Code">
            </div>
            <div class="form-group">
              <label>Email</label>
              <input type="email" [(ngModel)]="newOrg.email" placeholder="email@org.com">
            </div>
            <div class="form-group">
              <label>Pays</label>
              <input type="text" [(ngModel)]="newOrg.country" placeholder="Pays">
            </div>
          </div>
          <div class="form-actions">
            <button class="btn-cancel" (click)="showCreateForm.set(false)">Annuler</button>
            <button class="btn-save" (click)="createOrganization()">Créer</button>
          </div>
        </div>
      }

      <div class="data-table">
        <div class="table-header">
          <span class="col-name">Nom</span>
          <span class="col-code">Code</span>
          <span class="col-email">Email</span>
          <span class="col-tenants">Tenants</span>
          <span class="col-status">Statut</span>
          <span class="col-actions">Actions</span>
        </div>
        @for (org of organizations(); track org.id) {
          <div class="table-row">
            <span class="col-name">{{ org.name }}</span>
            <span class="col-code">{{ org.code }}</span>
            <span class="col-email">{{ org.email }}</span>
            <span class="col-tenants">{{ org.tenantCount }}</span>
            <span class="col-status">
              <span class="status-badge" [class.active]="org.active" [class.inactive]="!org.active">
                {{ org.active ? 'Actif' : 'Inactif' }}
              </span>
            </span>
            <span class="col-actions">
              <button class="btn-icon" title="Modifier">✏️</button>
              <button class="btn-icon" title="Supprimer">🗑️</button>
            </span>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .organizations { max-width: 1400px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
    .page-header h1 { color: #1a1a2e; margin: 0; }
    .page-header button { background: #1a1a2e; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
    .create-form { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); margin-bottom: 20px; }
    .form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin: 20px 0; }
    .form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
    .form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; }
    .form-actions { display: flex; gap: 10px; justify-content: flex-end; }
    .btn-cancel { background: #e9ecef; color: #333; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
    .btn-save { background: #1a1a2e; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
    .data-table { background: white; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); overflow: hidden; }
    .table-header { display: grid; grid-template-columns: 2fr 1fr 2fr 1fr 1fr 1fr; padding: 15px 20px; background: #1a1a2e; color: white; font-weight: 500; }
    .table-row { display: grid; grid-template-columns: 2fr 1fr 2fr 1fr 1fr 1fr; padding: 15px 20px; border-bottom: 1px solid #f0f0f0; align-items: center; }
    .table-row:hover { background: #f8f9fa; }
    .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 0.8rem; }
    .status-badge.active { background: #d4edda; color: #155724; }
    .status-badge.inactive { background: #f8d7da; color: #721c24; }
    .btn-icon { background: none; border: none; cursor: pointer; padding: 5px; font-size: 1.1rem; }
  `]
})
export class OrganizationsComponent implements OnInit {
  organizations = signal<any[]>([]);
  showCreateForm = signal(false);
  newOrg = { name: '', code: '', email: '', country: '' };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadOrganizations();
  }

  private loadOrganizations(): void {
    this.api.getOrganizations().subscribe({
      next: (data) => this.organizations.set(data.content || [])
    });
  }

  createOrganization(): void {
    this.api.createOrganization(this.newOrg).subscribe({
      next: () => {
        this.showCreateForm.set(false);
        this.loadOrganizations();
      }
    });
  }
}
