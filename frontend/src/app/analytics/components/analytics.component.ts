import {Component,OnInit,signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApiService} from '../../core/services/api.service';
@Component({selector:'app-analytics',standalone:true,imports:[CommonModule],template:`
<div class="anim-fade-up"><div class="page-header"><div><h1>Analytique</h1><p>Disponibilité des indicateurs mesurés</p></div>
<button class="btn btn-secondary" (click)="load()">Actualiser</button></div>
<div class="card"><div class="card-body">
@if(error()){<p role="alert">{{error()}}</p>}@else{<p>{{message()}}</p>}
<p>Les taux d'admission, de satisfaction et les classements ne sont pas calculés sans données d'observation vérifiées.</p>
</div></div></div>`})
export class AnalyticsComponent implements OnInit {
 error=signal('');message=signal('Chargement…');constructor(private api:ApiService){}
 ngOnInit():void{this.load();}
 load():void{this.error.set('');this.api.getAnalytics().subscribe({next:data=>this.message.set(data.unavailableReason||'Indicateurs indisponibles.'),
 error:()=>{this.message.set('');this.error.set('Consultation refusée ou indisponible.');}});}
}
