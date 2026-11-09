import { Component, signal, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { timeout, Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AuthSubmitComponent } from '../shared/auth-submit.component';
import { authError, validEmail } from '../shared/auth-errors';
@Component({selector:'app-forgot-password',standalone:true,imports:[FormsModule,RouterLink,AuthSubmitComponent],template:`<div class="auth-form-screen"><h2>Mot de passe oublié ?</h2><p class="auth-subtitle">Recevez un lien pour réinitialiser votre mot de passe.</p>@if(!sent()){<form class="auth-form" (ngSubmit)="onSubmit()"><div class="auth-field"><label for="forgot-email">Adresse e-mail</label><input id="forgot-email" name="email" type="email" [(ngModel)]="email" required autocomplete="email"></div>@if(error()){<p class="auth-error" role="alert">{{error()}}</p>}<app-auth-submit label="Envoyer le lien" pending="Envoi en cours…" [state]="loading()?'loading':'idle'" /></form>}@else{<div class="auth-confirmation" role="status"><h3>Consultez votre messagerie</h3><p>Si un compte éligible existe pour cette adresse, vous recevrez un lien de réinitialisation.</p></div>}<p class="auth-switch"><a routerLink="/auth/login">Retour à la connexion ↗</a></p></div>`})
export class ForgotPasswordComponent implements OnDestroy {
 email='';loading=signal(false);error=signal('');sent=signal(false);private request?:Subscription;
 constructor(private auth:AuthService){}
 onSubmit(){if(this.loading())return;if(!validEmail(this.email)){this.error.set('Renseignez une adresse e-mail valide.');return;}this.loading.set(true);this.error.set('');this.request=this.auth.forgotPassword(this.email.trim()).pipe(timeout(60000)).subscribe({next:()=>{this.sent.set(true);this.loading.set(false);},error:e=>{this.error.set(authError(e,'Envoi indisponible. Veuillez réessayer.'));this.loading.set(false);}});}
 ngOnDestroy(){this.request?.unsubscribe();}
}
