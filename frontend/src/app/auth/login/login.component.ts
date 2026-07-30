import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
@Component({
  selector: 'app-login', standalone: true, imports: [FormsModule, CommonModule, RouterLink],
  template: `
    <div class="lp">
      <div class="bp"><div class="bi">
        <div class="bl"><div class="bicon"><span class="material-symbols-rounded filled">school</span></div><span class="btitle">Orientation</span></div>
        <p class="bdesc">Plateforme intelligente d'orientation universitaire. Aidez les bacheliers à trouver la voie qui leur correspond.</p>
        <div class="bf"><div class="bfi"><span class="material-symbols-rounded">check_circle</span> Recommandations basées sur le profil académique</div><div class="bfi"><span class="material-symbols-rounded">check_circle</span> Simulation de scénarios d'admission</div><div class="bfi"><span class="material-symbols-rounded">check_circle</span> Analyse intelligente des bulletins</div><div class="bfi"><span class="material-symbols-rounded">check_circle</span> Base de connaissances universitaire</div></div>
        <div class="bfoot"><span class="material-symbols-rounded" style="font-size:14px">shield</span> Connexion sécurisée — JWT + TLS</div>
      </div></div>
      <div class="fp"><div class="fc">
        <h2>Bienvenue</h2><p class="fs">Connectez-vous à votre espace d'administration</p>
        <form (ngSubmit)="onLogin()">
          <div class="form-group"><label class="form-label">Adresse email</label><div class="iw"><span class="material-symbols-rounded ii">mail</span><input type="email" class="form-input" style="padding-left:40px" [(ngModel)]="email" name="email" placeholder="admin@orientation.com" required></div></div>
          <div class="form-group"><label class="form-label">Mot de passe</label><div class="iw"><span class="material-symbols-rounded ii">lock</span><input [type]="showPwd()?'text':'password'" class="form-input" style="padding-left:40px;padding-right:40px" [(ngModel)]="password" name="password" placeholder="Votre mot de passe" required><button type="button" class="pt" (click)="showPwd.set(!showPwd())"><span class="material-symbols-rounded">{{ showPwd()?'visibility_off':'visibility' }}</span></button></div></div>
          <div class="frb"><label class="cl"><input type="checkbox" [(ngModel)]="remember" name="remember"> Se souvenir de moi</label><a routerLink="/auth/forgot-password" class="fl">Mot de passe oublié ?</a></div>
          @if (error()) { <div class="alert alert-error"><span class="material-symbols-rounded" style="font-size:18px">error</span><span>{{ error() }}</span></div> }
          <button type="submit" class="btn btn-primary btn-lg btn-block" [disabled]="loading()" style="margin-top:8px">@if(loading()){<span class="spinner"></span> Connexion…}@else{<span class="material-symbols-rounded">login</span> Se connecter}</button>
        </form>
        <p class="ffoot">Back Office — Plateforme Orientation v1.0</p>
      </div></div>
    </div>`,
  styles: [`
    .lp{display:flex;min-height:100vh}.bp{flex:0 0 44%;background:linear-gradient(160deg,#0f172a,#1e293b 40%,#1e3a5f);display:flex;align-items:center;justify-content:center;padding:60px;position:relative;overflow:hidden}.bp::before{content:'';position:absolute;top:-40%;right:-20%;width:500px;height:500px;background:radial-gradient(circle,rgba(37,99,235,.12),transparent 70%);pointer-events:none}.bi{position:relative;z-index:1;max-width:440px}.bl{display:flex;align-items:center;gap:14px;margin-bottom:28px}.bicon{width:48px;height:48px;background:var(--brand);border-radius:var(--radius-lg);display:flex;align-items:center;justify-content:center;box-shadow:0 4px 12px rgba(37,99,235,.4)}.bicon .material-symbols-rounded{font-size:26px;color:#fff}.btitle{font-size:1.625rem;font-weight:900;color:#fff;letter-spacing:-.03em}.bdesc{font-size:1rem;color:var(--n-400);line-height:1.7;margin-bottom:36px}.bf{display:flex;flex-direction:column;gap:14px;margin-bottom:48px}.bfi{display:flex;align-items:center;gap:10px;font-size:.875rem;color:var(--n-300)}.bfi .material-symbols-rounded{font-size:18px;color:var(--brand-light)}.bfoot{display:flex;align-items:center;gap:6px;font-size:.6875rem;color:var(--n-500)}.fp{flex:1;display:flex;align-items:center;justify-content:center;padding:40px;background:var(--n-50)}.fc{width:100%;max-width:400px}.fc h2{font-size:1.5rem;font-weight:800;color:var(--n-900);letter-spacing:-.02em}.fs{font-size:.875rem;color:var(--n-500);margin:4px 0 32px}.iw{position:relative}.ii{position:absolute;left:12px;top:50%;transform:translateY(-50%);font-size:18px;color:var(--n-400);z-index:1}.pt{position:absolute;right:8px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;padding:4px;color:var(--n-400);display:flex}.pt:hover{color:var(--n-600)}.pt .material-symbols-rounded{font-size:18px}.frb{display:flex;align-items:center;justify-content:space-between;margin-bottom:24px}.cl{display:flex;align-items:center;gap:8px;font-size:.8125rem;color:var(--n-600);cursor:pointer}.cl input{width:15px;height:15px;accent-color:var(--brand)}.fl{font-size:.8125rem;font-weight:600;color:var(--brand)}.alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);margin-bottom:18px;font-size:.8125rem}.alert-error{background:var(--red-50);color:var(--red-600);border:1px solid rgba(239,68,68,.15)}.spinner{width:18px;height:18px;border:2.5px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}@keyframes spin{to{transform:rotate(360deg)}}.ffoot{margin-top:48px;text-align:center;font-size:.6875rem;color:var(--n-400)}.btn-block{width:100%}.btn-lg{padding:11px 22px;font-size:.875rem}
    @media(max-width:1024px){.bp{display:none}}
  `]
})
export class LoginComponent {
  email=''; password=''; remember=false;
  loading=signal(false); error=signal(''); showPwd=signal(false);
  private ret:string;
  constructor(private auth:AuthService, private router:Router, private route:ActivatedRoute) { this.ret=this.route.snapshot.queryParams['returnUrl']||'/dashboard'; }
  onLogin(): void { this.loading.set(true); this.error.set(''); this.auth.login(this.email,this.password,this.remember).subscribe({ next:()=>this.router.navigateByUrl(this.ret), error:(e)=>{this.error.set(e.error?.message||'Identifiants incorrects.');this.loading.set(false); } }); }
}
