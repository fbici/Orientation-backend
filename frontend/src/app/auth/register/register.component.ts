import { AuthIconComponent } from '../shared/auth-icon.component';
import { Component, signal, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { timeout, Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AuthSubmitComponent, AuthSubmitState } from '../shared/auth-submit.component';
import { authError, validEmail, validPassword } from '../shared/auth-errors';
@Component({selector:'app-register',standalone:true,imports:[FormsModule,RouterLink,AuthSubmitComponent,AuthIconComponent],template:`<div class="auth-form-screen"><span class="auth-eyebrow">UN NOUVEAU DÉPART</span><h2>Ouvrez vos horizons<span class="auth-dot">.</span></h2><p class="auth-subtitle">Créez votre compte candidat Orientia.</p>
@if(sent()){<div class="auth-confirmation" role="status"><span class="auth-confirmation-icon" aria-hidden="true">✉</span><h3>Consultez votre messagerie</h3><p>Si votre demande est éligible, un lien de vérification vous sera envoyé. Suivez ce lien pour activer votre compte.</p><p>Pour renouveler une demande non vérifiée, vous pouvez soumettre à nouveau le formulaire. Aucune connexion automatique n’est effectuée.</p><button type="button" class="auth-text-button" (click)="sent.set(false);state.set('idle')">Revenir au formulaire</button></div>}
@else{<form class="auth-form" (ngSubmit)="onSubmit()"><div class="auth-name-row"><div class="auth-field"><label for="register-first">Prénom</label><input id="register-first" name="firstName" [(ngModel)]="firstName" autocomplete="given-name" required maxlength="100"></div><div class="auth-field"><label for="register-last">Nom</label><input id="register-last" name="lastName" [(ngModel)]="lastName" autocomplete="family-name" required maxlength="100"></div></div>
<div class="auth-field"><label for="register-email">Adresse e-mail</label><input id="register-email" type="email" name="email" [(ngModel)]="email" required autocomplete="email"></div>
<div class="auth-field"><label for="register-password">Mot de passe</label><div class="auth-password"><input id="register-password" [type]="showPwd()?'text':'password'" name="password" [(ngModel)]="password" required minlength="12" maxlength="72" autocomplete="new-password" aria-describedby="password-help"><button type="button" (click)="showPwd.set(!showPwd())" [attr.aria-label]="showPwd()?'Masquer le mot de passe':'Afficher le mot de passe'" [attr.aria-pressed]="showPwd()"><app-auth-icon [name]="showPwd()?'eye-off':'eye'" /></button></div><p id="password-help" class="auth-hint">Au moins 12 caractères, au plus 72 octets. Une phrase unique est un bon choix.</p></div>
<div class="auth-field"><label for="register-confirm">Confirmez le mot de passe</label><input id="register-confirm" type="password" name="confirm" [(ngModel)]="confirm" required autocomplete="new-password"></div>
@if(error()){<p class="auth-error" role="alert">{{error()}}</p>}<app-auth-submit label="Créer mon compte" pending="Création en cours…" success="Demande reçue" [state]="state()" /></form>}
<p class="auth-switch">Déjà inscrit ? <a routerLink="/auth/login">Se connecter <span aria-hidden="true">↗</span></a></p></div>`})
export class RegisterComponent implements OnDestroy {
 firstName='';lastName='';email='';password='';confirm='';showPwd=signal(false);sent=signal(false);error=signal('');state=signal<AuthSubmitState>('idle');private request?:Subscription;
 constructor(private auth:AuthService){}
 onSubmit(){if(this.state()==='loading')return;if(!this.firstName.trim()||!this.lastName.trim()||!validEmail(this.email)||!validPassword(this.password)||this.password!==this.confirm){this.error.set('Vérifiez vos noms, votre e-mail et les deux mots de passe identiques (12 caractères minimum, 72 octets maximum).');return;}
 this.state.set('loading');this.error.set('');this.request=this.auth.register({firstName:this.firstName.trim(),lastName:this.lastName.trim(),email:this.email.trim(),password:this.password}).pipe(timeout(60000)).subscribe({next:()=>{this.state.set('success');this.sent.set(true);this.password='';this.confirm='';},error:e=>{this.error.set(authError(e,'La création du compte n’a pas pu être confirmée. Veuillez réessayer.'));this.state.set('error');}});}
 ngOnDestroy(){this.request?.unsubscribe();}
}
