import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  template: `
    <div class="profile">
      <h1>Profil Utilisateur</h1>
      <div class="content-card">
        <h3>Informations du Profil</h3>
        @if (auth.user()) {
          <div class="profile-info">
            <div class="info-item"><strong>Nom:</strong> {{ auth.user()?.firstName }} {{ auth.user()?.lastName }}</div>
            <div class="info-item"><strong>Email:</strong> {{ auth.user()?.email }}</div>
            <div class="info-item"><strong>Rôles:</strong> {{ auth.user()?.roles?.join(', ') }}</div>
            <div class="info-item"><strong>Tenant:</strong> {{ auth.user()?.tenantId }}</div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .profile { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .content-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    .profile-info { display: flex; flex-direction: column; gap: 10px; }
    .info-item { padding: 10px; background: #f8f9fa; border-radius: 4px; }
  `]
})
export class ProfileComponent {
  constructor(public auth: AuthService) {}
}
