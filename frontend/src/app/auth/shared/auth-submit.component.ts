import { Component, input } from '@angular/core';
export type AuthSubmitState = 'idle'|'loading'|'success'|'error';
@Component({selector:'app-auth-submit',standalone:true,template:`
<button type="submit" class="auth-submit" [class.is-loading]="state()==='loading'" [class.is-success]="state()==='success'" [class.is-error]="state()==='error'" [disabled]="disabled() || state()==='loading' || state()==='success'" [attr.aria-busy]="state()==='loading'">
<span aria-live="polite">{{state()==='loading'?pending():state()==='success'?success():label()}}</span><span class="auth-submit-indicator" aria-hidden="true">@if(state()==='loading'){<i class="auth-spinner"></i>}@else if(state()==='success'){<span>✓</span>}@else{<span>↗</span>}</span></button>`})
export class AuthSubmitComponent {
 state=input<AuthSubmitState>('idle');disabled=input(false);label=input('Continuer');pending=input('Traitement en cours…');success=input('Terminé');
}
