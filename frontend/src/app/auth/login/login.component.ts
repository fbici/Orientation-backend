import { AuthIconComponent } from '../shared/auth-icon.component';
import { Component, signal, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuthSubmitComponent, AuthSubmitState } from '../shared/auth-submit.component';
import { authError, safeReturnUrl, validEmail } from '../shared/auth-errors';
import { timeout, Subscription } from 'rxjs';
@Component({selector:'app-login',standalone:true,imports:[FormsModule,RouterLink,AuthSubmitComponent,AuthIconComponent],template:`
<div class="auth-form-screen"><h2>Connexion</h2><p class="auth-subtitle">Accédez à votre espace Orientia.</p>
@if(auth.logoutWarning()){<div class="auth-message" role="status">{{auth.logoutWarning()}}</div>}
<form (ngSubmit)="onLogin()" class="auth-form"><div class="auth-field"><label for="login-email">Adresse e-mail</label><input id="login-email" type="email" [(ngModel)]="email" name="email" required autocomplete="username" placeholder="votre@email.com" [readOnly]="loading()"></div>
<div class="auth-field"><div class="auth-label-row"><label for="login-password">Mot de passe</label><a routerLink="/auth/forgot-password">Mot de passe oublié ?</a></div><div class="auth-password"><input id="login-password" [type]="showPwd()?'text':'password'" [(ngModel)]="password" name="password" placeholder="Votre mot de passe" required autocomplete="current-password" [readOnly]="loading()"><button type="button" (click)="showPwd.set(!showPwd())" [attr.aria-label]="showPwd()?'Masquer le mot de passe':'Afficher le mot de passe'" [attr.aria-pressed]="showPwd()"><app-auth-icon [name]="showPwd()?'eye-off':'eye'" /></button></div></div>
<label class="auth-remember"><input type="checkbox" [(ngModel)]="remember" name="remember" [disabled]="loading()"> Rester connecté sur cet appareil</label>
@if(error()){<p class="auth-error" role="alert">{{error()}}</p>}
<app-auth-submit label="Se connecter" pending="Connexion en cours…" success="Connexion réussie" [state]="state()" /></form>
<div class="auth-divider"><span>Ou continuer avec</span></div><button type="button" class="auth-google" disabled aria-describedby="google-status"><img src="assets/brand/google-g.png" alt="" width="20" height="20"> Continuer avec Google</button><p id="google-status" class="auth-hint">Connexion Web Google en cours de configuration.</p>
<p class="auth-switch">Nouveau sur Orientia ? <a routerLink="/auth/register">Créer un compte <span aria-hidden="true">↗</span></a></p></div>`})
export class LoginComponent implements OnDestroy {
 email='';password='';remember=false;loading=signal(false);error=signal('');showPwd=signal(false);state=signal<AuthSubmitState>('idle');
 private ret:string;private redirect?:ReturnType<typeof setTimeout>;private request?:Subscription;
 constructor(public auth:AuthService,private router:Router,route:ActivatedRoute){this.ret=safeReturnUrl(route.snapshot.queryParams['returnUrl']);}
 onLogin():void {
  if(this.loading())return;
  if(!validEmail(this.email)||!this.password){this.error.set('Renseignez une adresse e-mail valide et votre mot de passe.');return;}
  this.loading.set(true);this.state.set('loading');this.error.set('');
  this.request=this.auth.login(this.email.trim(),this.password,this.remember).pipe(timeout(60000)).subscribe({next:()=>{this.state.set('success');this.password='';this.redirect=setTimeout(()=>{const target=this.ret==='/dashboard'&&!this.auth.hasAnyRole(['ADMIN','SUPER_ADMIN'])?'/profile':this.ret;this.router.navigateByUrl(target);},window.matchMedia('(prefers-reduced-motion: reduce)').matches?0:550);},error:e=>{this.error.set(authError(e,'Connexion refusée. Vérifiez vos identifiants et la vérification de votre adresse e-mail.'));this.loading.set(false);this.state.set('error');}});
 }
 ngOnDestroy(){clearTimeout(this.redirect);this.request?.unsubscribe();}
}
