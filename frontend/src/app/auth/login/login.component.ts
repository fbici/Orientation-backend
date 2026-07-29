import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h1>Orientation Platform</h1>
        <h2>Connexion</h2>
        <form (ngSubmit)="onLogin()">
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="email" name="email" placeholder="votre@email.com" required>
          </div>
          <div class="form-group">
            <label>Mot de passe</label>
            <input type="password" [(ngModel)]="password" name="password" placeholder="••••••••" required>
          </div>
          <div class="form-group">
            <label><input type="checkbox" [(ngModel)]="rememberMe" name="rememberMe"> Se souvenir de moi</label>
          </div>
          @if (error()) {
            <div class="error">{{ error() }}</div>
          }
          <button type="submit" [disabled]="loading()">Se connecter</button>
        </form>
        <a routerLink="/auth/forgot-password">Mot de passe oublié ?</a>
      </div>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); }
    .login-card { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.2); width: 100%; max-width: 400px; }
    .login-card h1 { text-align: center; color: #1a1a2e; margin-bottom: 5px; }
    .login-card h2 { text-align: center; color: #666; font-weight: normal; margin-bottom: 30px; }
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; margin-bottom: 5px; color: #333; }
    .form-group input { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; box-sizing: border-box; }
    .form-group input:focus { border-color: #1a1a2e; outline: none; }
    button { width: 100%; padding: 12px; background: #1a1a2e; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
    button:disabled { background: #ccc; }
    .error { color: #e74c3c; margin-bottom: 15px; text-align: center; }
    a { display: block; text-align: center; margin-top: 15px; color: #1a1a2e; }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  rememberMe = false;
  loading = signal(false);
  error = signal('');

  private returnUrl: string;

  constructor(private auth: AuthService, private router: Router, private route: ActivatedRoute) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
  }

  onLogin(): void {
    this.loading.set(true);
    this.error.set('');

    this.auth.login(this.email, this.password, this.rememberMe).subscribe({
      next: () => {
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Erreur de connexion');
        this.loading.set(false);
      }
    });
  }
}
