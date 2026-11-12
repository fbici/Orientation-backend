import {Component,Input,Output,EventEmitter,signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ApiService} from '../../core/services/api.service';
@Component({selector:'app-program-picker',standalone:true,imports:[FormsModule],template:`
<label class="form-label" for="program-search">Formations à comparer (2 à 6)</label>
<div class="search-row"><input id="program-search" class="form-input" [(ngModel)]="query" placeholder="Rechercher une formation" (keydown.enter)="search()"><button type="button" class="btn btn-secondary" (click)="search()" [disabled]="busy()">Rechercher</button></div>
<div class="selected-programs">@for(row of selected;track row.id){<button type="button" class="btn btn-secondary" (click)="toggle(row)">{{row.name}} <span aria-hidden="true">×</span><span class="sr-only">Retirer</span></button>}</div>
@if(error()){<p role="alert">{{error()}}</p>}
@if(busy()){<p role="status">Recherche…</p>}
@if(searched){<div class="program-results">@for(row of rows();track row.id){<label class="program-option"><input type="checkbox" [checked]="has(row.id)" [disabled]="!has(row.id)&&selected.length>=6" (change)="toggle(row)"><span><strong>{{row.name}}</strong><small>{{row.universityName || row.studyField || ''}}</small></span></label>}@empty{<p>Aucune formation trouvée.</p>}</div>
@if(hasNext){<button type="button" class="btn btn-secondary" (click)="search(true)" [disabled]="busy()">Voir plus de formations</button>}}
`,styles:[`.search-row{display:flex;gap:12px}.search-row input{min-width:0;flex:1}.program-results{max-height:240px;overflow:auto;margin:16px 0;background:white;border-radius:12px}.program-option{display:flex;align-items:center;gap:12px;padding:14px;border-bottom:1px solid #edf0f6;cursor:pointer}.program-option small{display:block;color:#64748b}.selected-programs{display:flex;gap:8px;flex-wrap:wrap;margin-top:12px}.sr-only{position:absolute;width:1px;height:1px;overflow:hidden;clip-path:inset(50%)}@media(max-width:600px){.search-row{flex-direction:column}}`]})
export class ProgramPickerComponent {
 @Input() value='';@Output() valueChange=new EventEmitter<string>();
 query='';selected:any[]=[];page=0;hasNext=false;searched=false;rows=signal<any[]>([]);busy=signal(false);error=signal('');private applied='';
 constructor(private api:ApiService){}
 has(id:string){return this.value.split(',').includes(id);}
 toggle(row:any){if(this.has(row.id)){this.selected=this.selected.filter(r=>r.id!==row.id);}else if(this.selected.length<6){this.selected=[...this.selected,row];}this.value=this.selected.map(r=>r.id).join(',');this.valueChange.emit(this.value);}
 search(more=false){if(this.busy())return;const next=more?this.page+1:0;if(!more){this.applied=this.query.trim();this.rows.set([]);}this.busy.set(true);this.error.set('');this.searched=true;
 this.api.catalog('programs',next,{search:this.applied}).subscribe({next:r=>{this.rows.set([...this.rows(),...r.content]);this.page=next;this.hasNext=!r.last&&next+1<r.totalPages;this.busy.set(false);},error:()=>{this.error.set('Recherche indisponible. Réessayez.');this.busy.set(false);}});}
}
