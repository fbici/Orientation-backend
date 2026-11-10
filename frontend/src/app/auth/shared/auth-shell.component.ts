import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
@Component({
 selector:'app-auth-shell',standalone:true,imports:[RouterOutlet,RouterLink],
 template:`<main class="auth-experience">
 <div class="auth-layout"><section class="auth-panel" aria-label="Espace d’authentification">
 <a class="auth-wordmark" routerLink="/auth/login" aria-label="Orientia, accueil de connexion"><img src="assets/brand/orientia-transparent.png" alt="Orientia" width="144" height="144"></a>
 <div class="auth-panel-content"><router-outlet /></div>
 </section><footer class="auth-contact"><a href="mailto:lestudioviral@gmail.com">Besoin d’aide ? Contactez-nous</a><span>© Orientia</span></footer></div>
 </main>`
})
export class AuthShellComponent {}
