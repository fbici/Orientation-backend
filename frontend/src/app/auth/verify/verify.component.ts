import { Component, signal, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { timeout, Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AuthSubmitComponent, AuthSubmitState } from '../shared/auth-submit.component';
import { authError } from '../shared/auth-errors';
@Component({selector:'app-verify',standalone:true,imports:[FormsModule,RouterLink,AuthSubmitComponent],template:`<div class="auth-form-screen"><h2>Vérifier mon e-mail</h2><p class="auth-subtitle">Utilisez le lien reçu dans votre messagerie.</p>
@if(state()==='success'){<div class="auth-verification is-verified" role="status"><div class="auth-orbit" aria-hidden="true"><i></i><i></i><i></i><i></i><b>✓</b></div><h3>Vérification réussie</h3><p>Votre adresse est confirmée. Vous pouvez vous connecter.</p></div>}
@else{<form class="auth-form" (ngSubmit)="onSubmit()"><div class="auth-field"><label for="verification-token">Jeton du lien de vérification</label><input id="verification-token" type="text" name="token" [(ngModel)]="token" required minlength="43" maxlength="43" autocomplete="off" spellcheck="false" autocapitalize="none" [readOnly]="state()==='loading'" aria-describedby="verify-help"><p id="verify-help" class="auth-hint">43 caractères. Aucun code numérique n’est envoyé par le serveur actuel.</p></div>@if(error()){<p class="auth-error" role="alert">{{error()}}</p>}<app-auth-submit label="Vérifier mon adresse" pending="Vérification en cours…" [state]="state()" /></form>}
<p class="auth-switch"><a routerLink="/auth/login">Retour à la connexion ↗</a></p><p class="auth-hint">Lien expiré ? <a routerLink="/auth/register">Renouvelez votre demande d’inscription</a>.</p></div>`})
export class VerifyComponent implements OnDestroy {
 token='';state=signal<AuthSubmitState>('idle');error=signal('');private request?:Subscription;private redirect?:ReturnType<typeof setTimeout>;
 constructor(private auth:AuthService,private router:Router,route:ActivatedRoute){this.token=route.snapshot.queryParamMap.get('token')||'';if(this.token)router.navigate([],{queryParams:{token:null},queryParamsHandling:'merge',replaceUrl:true});}
 onSubmit(){if(this.state()==='loading'||this.state()==='success')return;if(!/^[A-Za-z0-9_-]{43}$/.test(this.token)){this.error.set('Le lien est incomplet ou invalide. Utilisez le lien reçu par e-mail.');return;}this.state.set('loading');this.error.set('');this.request=this.auth.verifyEmail(this.token).pipe(timeout(60000)).subscribe({next:()=>{this.token='';this.state.set('success');this.redirect=setTimeout(()=>this.router.navigate(['/auth/login']),window.matchMedia('(prefers-reduced-motion: reduce)').matches?0:1700);},error:e=>{this.state.set('error');this.error.set(authError(e,'Ce lien est invalide, expiré ou déjà utilisé. Renouvelez votre demande.'));}});}
 ngOnDestroy(){clearTimeout(this.redirect);this.request?.unsubscribe();}
}
