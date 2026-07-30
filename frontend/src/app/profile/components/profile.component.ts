import { Component, signal } from '@angular/core';
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
        <div class="page-header-actions">
          <button class="btn btn-primary" (click)="save()" [disabled]="saving()">
            @if (saving()) { <span class="spinner" style="width:14px;height:14px;border-width:2px"></span> } @else { <span class="material-symbols-rounded">save</span> }
            Sauvegarder
          </button>
        </div>
      </div>

      @if (saved()) {
        <div class="alert alert-success" style="margin-bottom:20px"><span class="material-symbols-rounded">check_circle</span> Profil mis à jour avec succès.</div>
      }
      @if (error()) {
        <div class="alert alert-error" style="margin-bottom:20px"><span class="material-symbols-rounded">error</span> {{ error() }}</div>
      }

      <div style="display:grid;grid-template-columns:300px 1fr;gap:20px">
        <!-- Sidebar card -->
        <div class="card anim-fade-up">
          <div class="card-body" style="text-align:center;padding:36px 24px">
            <div class="avatar avatar-lg" style="width:80px;height:80px;font-size:1.5rem;background:var(--brand);margin:0 auto 16px">{{ auth.initials() }}</div>
            <h3 style="font-size:1.0625rem;font-weight:700;color:var(--n-900);margin-bottom:2px">{{ auth.fullName() }}</h3>
            <p style="font-size:.8125rem;color:var(--n-500);margin-bottom:14px">{{ auth.user()?.email }}</p>
            @for (role of auth.user()?.roles || []; track role) {
              <span class="badge badge-primary" style="margin-right:4px">{{ role }}</span>
            }
            <div class="divider"></div>
            <div style="text-align:left">
              <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:.8125rem">
                <span style="color:var(--n-500)">Tenant</span>
                <span style="font-weight:600;color:var(--n-800)">{{ auth.user()?.tenantName || auth.user()?.tenantId }}</span>
              </div>
              <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:.8125rem">
                <span style="color:var(--n-500)">Email vérifié</span>
                <span class="badge" [class]="auth.user()?.emailVerified ? 'badge-success' : 'badge-warning'">{{ auth.user()?.emailVerified ? 'Oui' : 'Non' }}</span>
              </div>
              <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:.8125rem">
                <span style="color:var(--n-500)">MFA</span>
                <span class="badge" [class]="auth.user()?.mfaEnabled ? 'badge-success' : 'badge-gray'">{{ auth.user()?.mfaEnabled ? 'Activé' : 'Désactivé' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Edit form -->
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Informations personnelles</h3></div>
          <div class="card-body">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:0 20px">
              <div class="form-group"><label class="form-label">Prénom</label><input type="text" class="form-input" [(ngModel)]="firstName"></div>
              <div class="form-group"><label class="form-label">Nom</label><input type="text" class="form-input" [(ngModel)]="lastName"></div>
              <div class="form-group"><label class="form-label">Email</label><input type="email" class="form-input" [value]="auth.user()?.email" disabled style="opacity:.6"></div>
              <div class="form-group"><label class="form-label">Téléphone</label><input type="tel" class="form-input" [(ngModel)]="phone" placeholder="+229 XX XX XX XX"></div>
            </div>

            <div class="divider"></div>

            <h4 style="font-size:.9375rem;font-weight:700;color:var(--n-900);margin-bottom:16px">Changer le mot de passe</h4>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:0 20px">
              <div class="form-group"><label class="form-label">Mot de passe actuel</label><input type="password" class="form-input" [(ngModel)]="oldPassword" placeholder="••••••••"></div>
              <div class="form-group"><label class="form-label">Nouveau mot de passe</label><input type="password" class="form-input" [(ngModel)]="newPassword" placeholder="••••••••"></div>
            </div>

            <div class="divider"></div>

            <h4 style="font-size:.9375rem;font-weight:700;color:var(--n-900);margin-bottom:16px">Authentification à deux facteurs</h4>
            <div style="display:flex;align-items:center;justify-content:space-between;padding:16px;background:var(--n-50);border-radius:var(--radius-sm)">
              <div>
                <div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">MFA (TOTP)</div>
                <div style="font-size:.6875rem;color:var(--n-500)">Ajouter une couche de sécurité supplémentaire</div>
              </div>
              @if (auth.user()?.mfaEnabled) {
                <span class="badge badge-success">Activé</span>
              } @else {
                <button class="btn btn-secondary btn-sm"><span class="material-symbols-rounded" style="font-size:16px">security</span>Configurer</button>
              }
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host{display:block}
    .spinner{width:18px;height:18px;border:2.5px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
    .alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);font-size:.8125rem}
    .alert-success{background:var(--green-50);color:var(--green-700);border:1px solid rgba(22,163,74,.15)}
    .alert-error{background:var(--red-50);color:var(--red-600);border:1px solid rgba(239,68,68,.15)}
    .alert .material-symbols-rounded{font-size:18px}
  `]
})
export class ProfileComponent {
  firstName = '';
  lastName = '';
  phone = '';
  oldPassword = '';
  newPassword = '';
  saving = signal(false);
  saved = signal(false);
  error = signal('');

  constructor(public auth: AuthService) {
    const u = auth.user();
    if (u) {
      this.firstName = u.firstName || '';
      this.lastName = u.lastName || '';
      this.phone = u.phone || '';
    }
  }

  save(): void {
    this.saving.set(true);
    this.saved.set(false);
    this.error.set('');

    // Update profile
    this.auth.updateProfile({
      firstName: this.firstName,
      lastName: this.lastName,
      phone: this.phone
    }).subscribe({
      next: () => {
        // Change password if provided
        if (this.oldPassword && this.newPassword) {
          this.auth.changePassword(this.oldPassword, this.newPassword).subscribe({
            next: () => { this.done(); },
            error: (e) => { this.error.set(e.error?.message || 'Erreur lors du changement de mot de passe.'); this.saving.set(false); }
          });
        } else {
          this.done();
        }
      },
      error: (e) => {
        this.error.set(e.error?.message || 'Erreur lors de la mise à jour du profil.');
        this.saving.set(false);
      }
    });
  }

  private done(): void {
    this.saving.set(false);
    this.saved.set(true);
    this.oldPassword = '';
    this.newPassword = '';
    setTimeout(() => this.saved.set(false), 3000);
  }
}
