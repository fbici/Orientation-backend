import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-forgot-password', standalone: true, imports: [FormsModule, RouterLink, CommonModule],
  template: `
    <div class="lp"><div class="bp"><div class="bi"><div class="bl"><div class="bicon"><span class="material-symbols-rounded filled">school</span></div><span class="btitle">Orientation</span></div><p class="bdesc">Réinitialisez votre mot de passe en toute sécurité.</p></div></div>
    <div class="fp"><div class="fc"><h2>Mot de passe oublié</h2><p class="fs">Entrez votre adresse email pour recevoir un lien de réinitialisation</p>
    @if(!sent()){<form (ngSubmit)="onSubmit()"><div class="form-group"><label class="form-label">Adresse email</label><div class="iw"><span class="material-symbols-rounded ii">mail</span><input type="email" class="form-input" style="padding-left:40px" [(ngModel)]="email" name="email" placeholder="votre@email.com" required></div></div>
    @if(error()){<div class="alert alert-error"><span class="material-symbols-rounded" style="font-size:18px">error</span><span>{{error()}}</span></div>}
    <button type="submit" class="btn btn-primary btn-lg btn-block" [disabled]="loading()">@if(loading()){<span class="spinner"></span> Envoi…}@else{<span class="material-symbols-rounded">send</span> Envoyer le lien}</button></form>}
    @else{<div style="text-align:center;padding:24px 0"><span class="material-symbols-rounded" style="font-size:48px;color:var(--green-500);margin-bottom:16px">mark_email_read</span><h3 style="font-size:1rem;font-weight:600;color:var(--n-900);margin-bottom:8px">Email envoyé</h3><p style="font-size:.875rem;color:var(--n-500)">Si un compte existe pour <strong>{{email}}</strong>, vous recevrez un lien.</p></div>}
    <div style="text-align:center;margin-top:24px"><a routerLink="/auth/login" style="font-size:.8125rem;font-weight:500;display:flex;align-items:center;justify-content:center;gap:4px"><span class="material-symbols-rounded" style="font-size:16px">arrow_back</span> Retour à la connexion</a></div></div></div></div>`,
  styles: [`.lp{display:flex;min-height:100vh}.bp{flex:0 0 44%;background:linear-gradient(160deg,#0f172a,#1e293b 40%,#1e3a5f);display:flex;align-items:center;justify-content:center;padding:60px}.bi{max-width:420px}.bl{display:flex;align-items:center;gap:14px;margin-bottom:24px}.bicon{width:48px;height:48px;background:var(--brand);border-radius:var(--radius-lg);display:flex;align-items:center;justify-content:center}.bicon .material-symbols-rounded{font-size:26px;color:#fff}.btitle{font-size:1.625rem;font-weight:900;color:#fff}.bdesc{font-size:1rem;color:var(--n-400);line-height:1.7}.fp{flex:1;display:flex;align-items:center;justify-content:center;padding:40px;background:var(--n-50)}.fc{width:100%;max-width:400px}.fc h2{font-size:1.5rem;font-weight:800;color:var(--n-900)}.fs{font-size:.875rem;color:var(--n-500);margin:4px 0 32px}.iw{position:relative}.ii{position:absolute;left:12px;top:50%;transform:translateY(-50%);font-size:18px;color:var(--n-400)}.alert{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:var(--radius-sm);margin-bottom:18px;font-size:.8125rem}.alert-error{background:var(--red-50);color:var(--red-600)}.spinner{width:18px;height:18px;border:2.5px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}@keyframes spin{to{transform:rotate(360deg)}}.btn-block{width:100%}.btn-lg{padding:11px 22px;font-size:.875rem}@media(max-width:1024px){.bp{display:none}}`]
})
export class ForgotPasswordComponent {
  email=''; loading=signal(false); error=signal(''); sent=signal(false);
  constructor(private http:HttpClient){}
  onSubmit(){this.loading.set(true);this.error.set('');this.http.post('/auth/forgot-password',{email:this.email}).subscribe({next:()=>{this.sent.set(true);this.loading.set(false)},error:()=>{this.sent.set(true);this.loading.set(false)}});}
}
