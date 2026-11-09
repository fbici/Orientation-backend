import { AuthIconComponent } from './auth-icon.component';
import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
@Component({selector:'app-auth-shell',standalone:true,imports:[RouterOutlet,RouterLink,AuthIconComponent],template:`
<main class="auth-experience"><div class="auth-ambient" aria-hidden="true"><i></i><i></i><div class="auth-grid"></div></div>
<div class="auth-layout"><aside class="auth-story"><a class="auth-wordmark" routerLink="/auth/login"><span class="auth-emblem" aria-hidden="true"><app-auth-icon name="school" /></span> Orientia<span class="auth-dot">.</span></a>
<div class="auth-story-copy"><span class="auth-eyebrow">VOTRE AVENIR COMMENCE ICI</span><h1>Le bon parcours.<br>Votre prochaine<br><em>grande étape.</em></h1><p>Explorez les formations et les bourses qui ouvrent vos horizons. Au Bénin, puis dans le monde.</p><div class="auth-story-line"><span></span> Une ambition. De nouvelles possibilités.</div></div>
<div class="auth-story-foot">Orientation universitaire · Bourses · Accompagnement</div></aside>
<section class="auth-panel" aria-label="Espace d’authentification"><div class="auth-panel-top"><span class="auth-panel-brand">ORIENTIA / ACCÈS</span><span class="auth-lock"><app-auth-icon name="lock" /> Espace personnel</span></div><div class="auth-panel-content"><router-outlet /></div><footer class="auth-panel-foot">Vos identifiants restent personnels. Ne les partagez jamais.</footer></section>
</div></main>`})
export class AuthShellComponent {}
