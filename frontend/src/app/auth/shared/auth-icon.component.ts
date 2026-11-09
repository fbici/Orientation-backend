import { Component, input } from '@angular/core';
@Component({selector:'app-auth-icon',standalone:true,template:`<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
@if(name()==='school'){<path d="M2 8 12 3 22 8 12 13Z M6 10v7q6 5 12 0v-7 M22 8v8"/>}
@else if(name()==='lock'){<rect x="5" y="10" width="14" height="11" rx="3"/><path d="M8 10V7a4 4 0 0 1 8 0v3 M12 14v3"/>}
@else{<path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/>@if(name()==='eye-off'){<path d="m3 3 18 18"/>}}
</svg>`})
export class AuthIconComponent {name=input<'school'|'lock'|'eye'|'eye-off'>('eye');}
