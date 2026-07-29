import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h2>Réinitialiser le mot de passe</h2>
        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Nouveau mot de passe</label>
            <input type="password" [(ngModel)]="password" name="password" required>
          </div>
          <div class="form-group">
            <label>Confirmer le mot de passe</label>
            <input type="password" [(ngModel)]="confirmPassword" name="confirmPassword" required>
          </div>
          @if (error()) {
            <div class="error">{{ error() }}</div>
          }
          @if (success()) {
            <div class="success">Mot de passe réinitialisé avec succès.</div>
          }
          <button type="submit" [disabled]="loading()">Réinitialiser</button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); }
    .login-card { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.2); width: 100%; max-width: 400px; }
    .login-card h2 { text-align: center; color: #333; margin-bottom: 30px; }
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; margin-bottom: 5px; }
    .form-group input { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
    button { width: 100%; padding: 12px; background: #1a1a2e; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .error { color: #e74c3c; margin-bottom: 15px; text-align: center; }
    .success { color: #27ae60; margin-bottom: 15px; text-align: center; }
  `]
})
export class ResetPasswordComponent {
  password = '';
  confirmPassword = '';
  loading = signal(false);
  error = signal('');
  success = signal(false);

  onSubmit(): void {
    if (this.password !== this.confirmPassword) {
      this.error.set('Les mots de passe ne correspondent pas');
      return;
    }
    this.loading.set(true);
    setTimeout(() => {
      this.success.set(true);
      this.loading.set(false);
    }, 1000);
  }
}
