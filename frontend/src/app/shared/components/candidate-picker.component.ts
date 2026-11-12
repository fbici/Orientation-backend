import {Component,Input,Output,EventEmitter,signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ApiService} from '../../core/services/api.service';
@Component({selector:'app-candidate-picker',standalone:true,imports:[FormsModule],template:`
<div class="candidate-picker">
 <label class="form-label" [for]="inputId">Rechercher un candidat</label>
 <div class="search-row"><input [id]="inputId" class="form-input" [(ngModel)]="query" placeholder="Nom ou adresse e-mail" (keydown.enter)="search()">
 <button type="button" class="btn btn-secondary" (click)="search()" [disabled]="busy()">Rechercher</button></div>
 @if(busy()){<p role="status">Recherche…</p>}
 @if(error()){<p role="alert">{{error()}}</p>}
 @if(searched && !busy()){
  <div class="candidate-results" role="list" aria-label="Candidats trouvés">
   @for(row of rows();track row.id){<button type="button" role="listitem" class="candidate-choice" [class.chosen]="value===row.id" (click)="choose(row)">
     <span class="material-symbols-rounded" aria-hidden="true">person</span><span><strong>{{row.firstName}} {{row.lastName}}</strong><small>{{row.email}}</small></span>
     @if(value===row.id){<span class="material-symbols-rounded" aria-hidden="true">check_circle</span>}
   </button>}@empty{<p>Aucun candidat trouvé. Essayez un autre nom.</p>}
  </div><small>Jusqu’à 50 résultats : précisez votre recherche si nécessaire.</small>
 }
 @if(selectedName){<p class="selected-candidate" role="status">Candidat sélectionné : <strong>{{selectedName}}</strong></p>}
</div>`,styles:[`
 .candidate-picker{margin-bottom:20px}.search-row{display:flex;gap:12px}.search-row input{flex:1;min-width:0}
 .candidate-results{max-height:260px;overflow:auto;margin:14px 0;border:1px solid #e4e9f2;border-radius:12px}
 .candidate-choice{display:flex;align-items:center;gap:12px;width:100%;padding:14px;text-align:left;background:white;border:0;border-bottom:1px solid #eef1f6;cursor:pointer;color:#24324b}
 .candidate-choice:hover,.candidate-choice.chosen{background:#eef2ff}.candidate-choice small{display:block;color:#64748b;margin-top:4px}.selected-candidate{padding:12px;background:#eef7f2;border-radius:10px;margin-top:12px}
 @media(max-width:600px){.search-row{flex-direction:column}}
`]})
export class CandidatePickerComponent {
 private static sequence=0;inputId='candidate-search-'+CandidatePickerComponent.sequence++;
 @Input() value='';@Output() valueChange=new EventEmitter<string>();
 query='';selectedName='';searched=false;busy=signal(false);error=signal('');rows=signal<any[]>([]);
 constructor(private api:ApiService){}
 search(){if(this.busy())return;this.busy.set(true);this.error.set('');this.searched=true;
  this.api.searchCandidates(this.query.trim()).subscribe({next:rows=>{this.rows.set(rows);this.busy.set(false);},error:()=>{this.rows.set([]);this.error.set('Recherche indisponible ou accès non autorisé.');this.busy.set(false);}});}
 choose(row:any){this.selectedName=[row.firstName,row.lastName].filter(Boolean).join(' ') || row.email;this.value=row.id;this.valueChange.emit(row.id);}
}
