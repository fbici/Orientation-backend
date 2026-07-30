import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  template: `
    <div class="login-page">
      <!-- Animated background -->
      <div class="bg">
        <div class="bg-gradient"></div>
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
        <div class="grid-overlay"></div>
      </div>

      <!-- Login card -->
      <div class="login-wrapper">
        <div class="login-card">
          <!-- Logo -->
          <div class="card-logo">
            <div class="logo-icon">
              <span class="material-symbols-rounded filled">school</span>
            </div>
          </div>

          <!-- Header -->
          <h1 class="card-title">Bon retour parmi nous</h1>
          <p class="card-subtitle">Connectez-vous à votre espace d'administration</p>

          <!-- Form -->
          <form (ngSubmit)="onLogin()" class="login-form">
            <!-- Email -->
            <div class="field">
              <label class="field-label">Adresse email</label>
              <div class="field-input">
                <span class="material-symbols-rounded field-icon">mail</span>
                <input type="email"
                       [(ngModel)]="email" name="email"
                       placeholder="admin&#64;orientation.com"
                       required autocomplete="email">
              </div>
            </div>

            <!-- Password -->
            <div class="field">
              <div class="field-label-row">
                <label class="field-label">Mot de passe</label>
                <a routerLink="/auth/forgot-password" class="field-link">Mot de passe oublié ?</a>
              </div>
              <div class="field-input">
                <span class="material-symbols-rounded field-icon">lock</span>
                <input [type]="showPwd() ? 'text' : 'password'"
                       [(ngModel)]="password" name="password"
                       placeholder="Entrez votre mot de passe"
                       required autocomplete="current-password">
                <button type="button" class="field-action" (click)="showPwd.set(!showPwd())">
                  <span class="material-symbols-rounded">{{ showPwd() ? 'visibility_off' : 'visibility' }}</span>
                </button>
              </div>
            </div>

            <!-- Remember -->
            <div class="field-row">
              <label class="checkbox">
                <input type="checkbox" [(ngModel)]="remember" name="remember">
                <span class="checkbox-mark">
                  <span class="material-symbols-rounded">check</span>
                </span>
                <span class="checkbox-text">Se souvenir de moi pendant 30 jours</span>
              </label>
            </div>

            <!-- Error -->
            @if (error()) {
              <div class="alert">
                <span class="material-symbols-rounded">error</span>
                <span>{{ error() }}</span>
              </div>
            }

            <!-- Submit -->
            <button type="submit" class="submit-btn" [disabled]="loading()">
              @if (loading()) {
                <span class="spinner"></span>
                <span>Connexion en cours…</span>
              } @else {
                <span>Se connecter</span>
                <span class="material-symbols-rounded">arrow_forward</span>
              }
            </button>
          </form>

          <!-- Footer -->
          <div class="card-footer">
            <div class="footer-badge">
              <span class="material-symbols-rounded">shield</span>
              Connexion sécurisée SSL
            </div>
          </div>
        </div>

        <!-- Bottom text -->
        <p class="bottom-text">Back Office — Plateforme Orientation v1.0</p>
      </div>
    </div>
  `,
  styles: [`
    /* ─── Page ─── */
    .login-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      overflow: hidden;
      padding: 24px;
    }

    /* ─── Animated Background ─── */
    .bg {
      position: fixed;
      inset: 0;
      z-index: 0;
    }

    .bg-gradient {
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, #0c1222 0%, #0f172a 30%, #1e293b 60%, #0f172a 100%);
    }

    .orb {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      opacity: .4;
      animation: float 20s ease-in-out infinite;
    }

    .orb-1 {
      width: 500px;
      height: 500px;
      background: radial-gradient(circle, #2563eb, transparent 70%);
      top: -10%;
      right: -5%;
      animation-delay: 0s;
    }

    .orb-2 {
      width: 400px;
      height: 400px;
      background: radial-gradient(circle, #7c3aed, transparent 70%);
      bottom: -10%;
      left: -5%;
      animation-delay: -7s;
      animation-duration: 25s;
    }

    .orb-3 {
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, #0ea5e9, transparent 70%);
      top: 40%;
      left: 50%;
      animation-delay: -14s;
      animation-duration: 30s;
    }

    @keyframes float {
      0%, 100% { transform: translate(0, 0) scale(1); }
      25% { transform: translate(30px, -40px) scale(1.05); }
      50% { transform: translate(-20px, 20px) scale(.95); }
      75% { transform: translate(15px, 35px) scale(1.02); }
    }

    .grid-overlay {
      position: absolute;
      inset: 0;
      background-image:
        linear-gradient(rgba(255,255,255,.02) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255,255,255,.02) 1px, transparent 1px);
      background-size: 60px 60px;
    }

    /* ─── Wrapper ─── */
    .login-wrapper {
      position: relative;
      z-index: 1;
      width: 100%;
      max-width: 440px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    /* ─── Card ─── */
    .login-card {
      width: 100%;
      background: rgba(255, 255, 255, .03);
      backdrop-filter: blur(24px);
      -webkit-backdrop-filter: blur(24px);
      border: 1px solid rgba(255, 255, 255, .08);
      border-radius: 20px;
      padding: 40px 36px 32px;
      box-shadow:
        0 0 0 1px rgba(255,255,255,.04),
        0 20px 60px rgba(0, 0, 0, .4),
        inset 0 1px 0 rgba(255,255,255,.06);
      animation: cardIn .6s cubic-bezier(.16,1,.3,1) both;
    }

    @keyframes cardIn {
      from { opacity: 0; transform: translateY(24px) scale(.97); }
      to { opacity: 1; transform: translateY(0) scale(1); }
    }

    /* ─── Logo ─── */
    .card-logo {
      display: flex;
      justify-content: center;
      margin-bottom: 28px;
    }

    .logo-icon {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow:
        0 8px 24px rgba(37, 99, 235, .35),
        0 0 0 1px rgba(255,255,255,.1) inset;
    }

    .logo-icon .material-symbols-rounded {
      font-size: 28px;
      color: #fff;
    }

    /* ─── Title ─── */
    .card-title {
      font-size: 1.5rem;
      font-weight: 800;
      color: #fff;
      text-align: center;
      letter-spacing: -.03em;
      margin-bottom: 6px;
    }

    .card-subtitle {
      font-size: .875rem;
      color: rgba(255, 255, 255, .45);
      text-align: center;
      margin-bottom: 32px;
    }

    /* ─── Form ─── */
    .login-form {
      display: flex;
      flex-direction: column;
      gap: 0;
    }

    .field {
      margin-bottom: 20px;
    }

    .field-label-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
    }

    .field-label {
      display: block;
      font-size: .8125rem;
      font-weight: 600;
      color: rgba(255, 255, 255, .7);
      margin-bottom: 8px;
    }

    .field-label-row .field-label {
      margin-bottom: 0;
    }

    .field-link {
      font-size: .75rem;
      font-weight: 600;
      color: #60a5fa;
      text-decoration: none;
      transition: color .15s;
    }

    .field-link:hover {
      color: #93c5fd;
    }

    .field-input {
      position: relative;
      display: flex;
      align-items: center;
    }

    .field-icon {
      position: absolute;
      left: 14px;
      font-size: 19px;
      color: rgba(255, 255, 255, .25);
      pointer-events: none;
      z-index: 1;
      transition: color .15s;
    }

    .field-input input {
      width: 100%;
      padding: 13px 14px 13px 44px;
      font-size: .875rem;
      font-family: inherit;
      font-weight: 500;
      color: #fff;
      background: rgba(255, 255, 255, .05);
      border: 1px solid rgba(255, 255, 255, .1);
      border-radius: 12px;
      outline: none;
      transition: all .2s;
    }

    .field-input input::placeholder {
      color: rgba(255, 255, 255, .2);
      font-weight: 400;
    }

    .field-input input:focus {
      background: rgba(255, 255, 255, .08);
      border-color: rgba(59, 130, 246, .5);
      box-shadow: 0 0 0 3px rgba(59, 130, 246, .15), 0 0 20px rgba(59, 130, 246, .1);
    }

    .field-input input:focus ~ .field-icon,
    .field-input input:focus + .field-icon {
      color: #60a5fa;
    }

    .field-input:focus-within .field-icon {
      color: #60a5fa;
    }

    .field-action {
      position: absolute;
      right: 10px;
      background: none;
      border: none;
      cursor: pointer;
      padding: 6px;
      color: rgba(255, 255, 255, .25);
      display: flex;
      border-radius: 8px;
      transition: all .15s;
    }

    .field-action:hover {
      color: rgba(255, 255, 255, .6);
      background: rgba(255, 255, 255, .06);
    }

    .field-action .material-symbols-rounded {
      font-size: 19px;
    }

    /* ─── Checkbox ─── */
    .field-row {
      margin-bottom: 24px;
    }

    .checkbox {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      user-select: none;
    }

    .checkbox input {
      display: none;
    }

    .checkbox-mark {
      width: 20px;
      height: 20px;
      border: 1.5px solid rgba(255, 255, 255, .2);
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: all .15s;
    }

    .checkbox-mark .material-symbols-rounded {
      font-size: 14px;
      color: #fff;
      opacity: 0;
      transform: scale(.5);
      transition: all .15s;
    }

    .checkbox input:checked + .checkbox-mark {
      background: #2563eb;
      border-color: #2563eb;
      box-shadow: 0 0 12px rgba(37, 99, 235, .3);
    }

    .checkbox input:checked + .checkbox-mark .material-symbols-rounded {
      opacity: 1;
      transform: scale(1);
    }

    .checkbox-text {
      font-size: .8125rem;
      color: rgba(255, 255, 255, .5);
      font-weight: 500;
    }

    /* ─── Alert ─── */
    .alert {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 16px;
      background: rgba(239, 68, 68, .1);
      border: 1px solid rgba(239, 68, 68, .2);
      border-radius: 10px;
      margin-bottom: 20px;
      font-size: .8125rem;
      font-weight: 500;
      color: #fca5a5;
    }

    .alert .material-symbols-rounded {
      font-size: 18px;
      color: #f87171;
    }

    /* ─── Submit ─── */
    .submit-btn {
      width: 100%;
      padding: 14px 24px;
      font-size: .9375rem;
      font-weight: 700;
      font-family: inherit;
      color: #fff;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      border: none;
      border-radius: 12px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: all .2s;
      position: relative;
      overflow: hidden;
      box-shadow: 0 4px 16px rgba(37, 99, 235, .3), 0 0 0 1px rgba(255,255,255,.1) inset;
    }

    .submit-btn::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, transparent, rgba(255,255,255,.1), transparent);
      opacity: 0;
      transition: opacity .3s;
    }

    .submit-btn:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 8px 24px rgba(37, 99, 235, .4), 0 0 0 1px rgba(255,255,255,.15) inset;
    }

    .submit-btn:hover:not(:disabled)::before {
      opacity: 1;
    }

    .submit-btn:active:not(:disabled) {
      transform: translateY(0);
    }

    .submit-btn:disabled {
      opacity: .5;
      cursor: not-allowed;
    }

    .submit-btn .material-symbols-rounded {
      font-size: 20px;
      transition: transform .2s;
    }

    .submit-btn:hover:not(:disabled) .material-symbols-rounded {
      transform: translateX(3px);
    }

    /* ─── Spinner ─── */
    .spinner {
      width: 18px;
      height: 18px;
      border: 2.5px solid rgba(255, 255, 255, .25);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin .6s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    /* ─── Footer ─── */
    .card-footer {
      margin-top: 28px;
      display: flex;
      justify-content: center;
    }

    .footer-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 14px;
      background: rgba(255, 255, 255, .04);
      border: 1px solid rgba(255, 255, 255, .06);
      border-radius: 9999px;
      font-size: .6875rem;
      font-weight: 600;
      color: rgba(255, 255, 255, .3);
    }

    .footer-badge .material-symbols-rounded {
      font-size: 14px;
      color: #34d399;
    }

    .bottom-text {
      margin-top: 24px;
      font-size: .6875rem;
      color: rgba(255, 255, 255, .15);
      text-align: center;
    }

    /* ─── Responsive ─── */
    @media (max-width: 480px) {
      .login-card {
        padding: 32px 24px 24px;
        border-radius: 16px;
      }
      .card-title {
        font-size: 1.25rem;
      }
    }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  remember = false;
  loading = signal(false);
  error = signal('');
  showPwd = signal(false);
  private ret: string;

  constructor(private auth: AuthService, private router: Router, private route: ActivatedRoute) {
    this.ret = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
  }

  onLogin(): void {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.email, this.password, this.remember).subscribe({
      next: () => this.router.navigateByUrl(this.ret),
      error: (e) => {
        this.error.set(e.error?.message || 'Identifiants incorrects. Veuillez réessayer.');
        this.loading.set(false);
      }
    });
  }
}
