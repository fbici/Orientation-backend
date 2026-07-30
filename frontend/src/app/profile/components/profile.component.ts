import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Mon profil</h1><p>Gérer vos informations personnelles</p></div>
        <div class="page-header-actions"><button class="btn btn-primary"><span class="material-symbols-rounded">save</span>Sauvegarder</button></div>
      </div>
      <div class="g3" style="grid-template-columns:300px 1fr">
        <div class="card anim-fade-up">
          <div class="card-body" style="text-align:center;padding:36px 24px">
            <div class="avatar avatar-lg" style="width:80px;height:80px;font-size:1.5rem;background:var(--brand);margin:0 auto 16px">{{ initials() }}</div>
            <h3 style="font-size:1.0625rem;font-weight:700;color:var(--n-900);margin-bottom:2px">{{ auth.user()?.firstName }} {{ auth.user()?.lastName }}</h3>
            <p style="font-size:.8125rem;color:var(--n-500);margin-bottom:14px">{{ auth.user()?.email }}</p>
            <span class="badge badge-primary">{{ auth.user()?.roles?.[0] }}</span>
            <div class="divider"></div>
            <div style="text-align:left">
              <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:.8125rem"><span style="color:var(--n-500)">Tenant</span><span style="font-weight:600;color:var(--n-800)">{{ auth.user()?.tenantId }}</span></div>
              <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:.8125rem"><span style="color:var(--n-500)">Membre depuis</span><span style="font-weight:600;color:var(--n-800)">Janvier 2026</span></div>
            </div>
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Informations personnelles</h3></div>
          <div class="card-body">
            <div class="g2">
              <div class="form-group"><label class="form-label">Prénom</label><input type="text" class="form-input" [value]="auth.user()?.firstName"></div>
              <div class="form-group"><label class="form-label">Nom</label><input type="text" class="form-input" [value]="auth.user()?.lastName"></div>
              <div class="form-group"><label class="form-label">Email</label><input type="email" class="form-input" [value]="auth.user()?.email"></div>
              <div class="form-group"><label class="form-label">Téléphone</label><input type="tel" class="form-input" placeholder="+229 XX XX XX XX"></div>
            </div>
            <div class="divider"></div>
            <h4 style="font-size:.9375rem;font-weight:700;color:var(--n-900);margin-bottom:16px">Changer le mot de passe</h4>
            <div class="g2">
              <div class="form-group"><label class="form-label">Mot de passe actuel</label><input type="password" class="form-input" placeholder="••••••••"></div>
              <div class="form-group"><label class="form-label">Nouveau mot de passe</label><input type="password" class="form-input" placeholder="••••••••"></div>
            </div>
            <div class="divider"></div>
            <h4 style="font-size:.9375rem;font-weight:700;color:var(--n-900);margin-bottom:16px">Authentification à deux facteurs</h4>
            <div style="display:flex;align-items:center;justify-content:space-between;padding:16px;background:var(--n-50);border-radius:var(--radius-sm)">
              <div><div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">MFA (TOTP)</div><div style="font-size:.6875rem;color:var(--n-500)">Ajouter une couche de sécurité supplémentaire</div></div>
              <button class="btn btn-secondary btn-sm"><span class="material-symbols-rounded" style="font-size:16px">security</span>Configurer</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class ProfileComponent {
  initials = computed(() => { const u = this.auth.user(); return (u?.firstName?.[0] ?? '') + (u?.lastName?.[0] ?? ''); });
  constructor(public auth: AuthService) {}
}
