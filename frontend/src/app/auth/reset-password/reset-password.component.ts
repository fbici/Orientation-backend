import { AuthIconComponent } from '../shared/auth-icon.component';
import { Component, signal, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { timeout, Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AuthSubmitComponent, AuthSubmitState } from '../shared/auth-submit.component';
import { authError, validPassword } from '../shared/auth-errors';
@Component({selector:'app-reset-password',standalone:true,imports:[FormsModule,RouterLink,AuthSubmitComponent,AuthIconComponent],template:`<div class="auth-form-screen"><h2>Nouveau mot de passe</h2><p class="auth-subtitle">Choisissez une phrase unique que vous seul connaissez.</p>
@if(state()==='success'){<div class="auth-confirmation" role="status"><h3>Mot de passe réinitialisé</h3><p>Connectez-vous avec votre nouveau mot de passe.</p></div>}
@else if(!hasToken){<div class="auth-error" role="alert">Ce lien est incomplet. <a routerLink="/auth/forgot-password">Demandez un nouveau lien</a>.</div>}
@else{<form class="auth-form" (ngSubmit)="onSubmit()"><div class="auth-field"><label for="reset-password">Nouveau mot de passe</label><div class="auth-password"><input id="reset-password" [type]="showPwd()?'text':'password'" name="password" [(ngModel)]="password" required minlength="12" maxlength="72" autocomplete="new-password" aria-describedby="reset-help"><button type="button" (click)="showPwd.set(!showPwd())" [attr.aria-label]="showPwd()?'Masquer le mot de passe':'Afficher le mot de passe'" [attr.aria-pressed]="showPwd()"><app-auth-icon [name]="showPwd()?'eye-off':'eye'" /></button></div><p id="reset-help" class="auth-hint">12 caractères minimum, 72 octets maximum.</p></div><div class="auth-field"><label for="reset-confirm">Confirmez le mot de passe</label><input id="reset-confirm" type="password" name="confirm" [(ngModel)]="confirm" required autocomplete="new-password"></div>@if(error()){<p class="auth-error" role="alert">{{error()}}</p>}<app-auth-submit label="Réinitialiser le mot de passe" pending="Réinitialisation?" [state]="state()" /></form>}<p class="auth-switch"><a routerLink="/auth/login">Retour à la connexion ↗</a></p></div>`})
export class ResetPasswordComponent implements OnDestroy {
 password='';confirm='';loading=signal(false);error=signal('');state=signal<AuthSubmitState>('idle');showPwd=signal(false);private token:string;readonly hasToken:boolean;private request?:Subscription;
 constructor(private auth:AuthService,private router:Router,route:ActivatedRoute){this.token=route.snapshot.queryParamMap.get('token')||'';this.hasToken=/^[A-Za-z0-9_-]{43}$/.test(this.token);if(this.token)this.router.navigate([],{queryParams:{token:null},queryParamsHandling:'merge',replaceUrl:true});}
 onSubmit(){if(this.loading()||this.state()==='success')return;if(!this.hasToken||!validPassword(this.password)||this.password!==this.confirm){this.error.set('Vérifiez le lien et les deux mots de passe identiques (12 caractères minimum, 72 octets maximum).');return;}this.loading.set(true);this.state.set('loading');this.error.set('');this.request=this.auth.resetPassword(this.token,this.password).pipe(timeout(60000)).subscribe({next:()=>{this.password='';this.confirm='';this.token='';this.state.set('success');this.loading.set(false);},error:e=>{this.error.set(authError(e,'Ce lien est invalide, expiré ou déjà utilisé. Demandez un nouveau lien.'));this.loading.set(false);this.state.set('error');}});}
 ngOnDestroy(){this.request?.unsubscribe();}
}
