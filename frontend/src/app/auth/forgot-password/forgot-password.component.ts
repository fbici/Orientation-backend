import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h2>Mot de passe oublié</h2>
        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="email" name="email" placeholder="votre@email.com" required>
          </div>
          @if (success()) {
            <div class="success">Un email de réinitialisation a été envoyé.</div>
          }
          <button type="submit" [disabled]="loading()">Envoyer</button>
        </form>
        <a routerLink="/auth/login">Retour à la connexion</a>
      </div>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); }
    .login-card { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.2); width: 100%; max-width: 400px; }
    .login-card h2 { text-align: center; color: #333; margin-bottom: 30px; }
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; margin-bottom: 5px; color: #333; }
    .form-group input { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
    button { width: 100%; padding: 12px; background: #1a1a2e; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
    .success { color: #27ae60; margin-bottom: 15px; text-align: center; }
    a { display: block; text-align: center; margin-top: 15px; color: #1a1a2e; }
  `]
})
export class ForgotPasswordComponent {
  email = '';
  loading = signal(false);
  success = signal(false);

  onSubmit(): void {
    this.loading.set(true);
    // Simulate API call
    setTimeout(() => {
      this.success.set(true);
      this.loading.set(false);
    }, 1000);
  }
}
