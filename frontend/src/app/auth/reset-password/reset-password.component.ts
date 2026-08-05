import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-reset-password', standalone: true, imports: [FormsModule, RouterLink, CommonModule],
  template: `
    <div class="lp"><div class="bp"><div class="bi"><div class="bl"><div class="bicon"><span class="material-symbols-rounded filled">school</span></div><span class="btitle">Orientation</span></div><p class="bdesc">Choisissez un nouveau mot de passe sécurisé.</p></div></div>
    <div class="fp"><div class="fc"><h2>Nouveau mot de passe</h2><p class="fs">Choisissez un mot de passe fort pour votre compte</p>
    <form (ngSubmit)="onSubmit()"><div class="form-group"><label class="form-label">Nouveau mot de passe</label><input type="password" class="form-input" [(ngModel)]="password" name="password" placeholder="Minimum 8 caractères" required></div>
    <div class="form-group"><label class="form-label">Confirmer</label><input type="password" class="form-input" [(ngModel)]="confirm" name="confirm" placeholder="Retapez le mot de passe" required></div>
    @if(error()){<div class="alert alert-error"><span class="material-symbols-rounded" style="font-size:18px">error</span><span>{{error()}}</span></div>}
    <button type="submit" class="btn btn-primary btn-lg btn-block" [disabled]="loading()">@if(loading()){<span class="spinner"></span>} Réinitialiser</button></form>
    <div style="text-align:center;margin-top:24px"><a routerLink="/auth/login" style="font-size:.8125rem;font-weight:500;display:flex;align-items:center;justify-content:center;gap:4px"><span class="material-symbols-rounded" style="font-size:16px">arrow_back</span> Retour</a></div></div></div></div>`,
  styles: [`.lp{display:flex;min-height:100vh}.bp{flex:0 0 44%;background:linear-gradient(160deg,#0f172a,#1e293b 40%,#1e3a5f);display:flex;align-items:center;justify-content:center;padding:60px}.bi{max-width:420px}.bl{display:flex;align-items:center;gap:14px;margin-bottom:24px}.bicon{width:48px;height:48px;background:var(--brand);border-radius:var(--radius-lg);display:flex;align-items:center;justify-content:center}.bicon .material-symbols-rounded{font-size:26px;color:#fff}.btitle{font-size:1.625rem;font-weight:900;color:#fff}.bdesc{font-size:1rem;color:var(--n-400);line-height:1.7}.fp{flex:1;display:flex;align-items:center;justify-content:center;padding:40px;background:var(--n-50)}.fc{width:100%;max-width:400px}.fc h2{font-size:1.5rem;font-weight:800;color:var(--n-900)}.fs{font-size:.875rem;color:var(--n-500);margin:4px 0 32px}.alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);margin-bottom:18px;font-size:.8125rem}.alert-error{background:var(--red-50);color:var(--red-600)}.spinner{width:18px;height:18px;border:2.5px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite;display:inline-block}@keyframes spin{to{transform:rotate(360deg)}}.btn-block{width:100%}.btn-lg{padding:11px 22px;font-size:.875rem}@media(max-width:1024px){.bp{display:none}}`]
})
export class ResetPasswordComponent {
  password=''; confirm=''; loading=signal(false); error=signal('');
  constructor(private http:HttpClient, private router:Router){}
  onSubmit(){if(this.password!==this.confirm){this.error.set('Les mots de passe ne correspondent pas.');return;}this.loading.set(true);this.error.set('');this.http.post('/auth/reset-password',{password:this.password}).subscribe({next:()=>this.router.navigate(['/auth/login']),error:()=>{this.error.set('Erreur lors de la réinitialisation.');this.loading.set(false);}});}
}
