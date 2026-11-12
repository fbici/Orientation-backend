import {ProgramPickerComponent} from '../shared/components/program-picker.component';
import {CandidatePickerComponent} from '../shared/components/candidate-picker.component';
import {PaginationComponent} from '../shared/components/pagination.component';
import {Component,Input,signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ApiService} from '../core/services/api.service';
import {finalize,Observable,switchMap} from 'rxjs';
export interface DecisionView {id:string;kind:string;computedAt:string;profileChanged:boolean;sourceCurrent?:boolean;result:unknown;}
@Component({
 selector:'app-decisions',standalone:true,imports:[CommonModule,FormsModule,CandidatePickerComponent,PaginationComponent,ProgramPickerComponent],
 template:`
 <div class="anim-fade-up">
 <div class="page-header"><div><h1>{{reportsOnly ? "Rapports et exports" : "Aide à la décision"}}</h1><p>{{reportsOnly ? "Retrouvez les décisions enregistrées et téléchargez leurs résultats." : "Comparez des formations et explorez les possibilités du candidat."}}</p></div></div>
 <div class="card"><div class="card-body">
 <app-candidate-picker [(value)]="candidateId" (valueChange)="resetContext()" />
 <p>Compatibilité calculée, sans classement officiel ni garantie d'admission. Les simulations conservent le profil confirmé.</p>
 @if(!reportsOnly){<section class="decision-tools"><h2>Explorer une hypothèse</h2><app-program-picker [(value)]="programIds" />
 <button class="btn btn-secondary" (click)="compare()" [disabled]="busy()">Comparer</button>
 <button class="btn btn-secondary" (click)="funding()" [disabled]="busy()">Évaluer les bourses</button>
 <label class="form-label" for="decision-average">Moyenne hypothétique dans le barème confirmé</label>
 <input id="decision-average" type="number" step="any" class="form-input" [(ngModel)]="average">
 <button class="btn btn-primary" (click)="simulate()" [disabled]="busy()">Simuler</button>
 <label class="form-label" for="decision-question">Question à l'assistant</label>
 <input id="decision-question" class="form-input" maxlength="2000" [(ngModel)]="question">
 <button class="btn btn-secondary" (click)="ask()" [disabled]="busy()">Interroger</button>
 <p>Pour une simulation ou une comparaison, l'assistant utilise les paramètres explicites renseignés ci-dessus.</p>
 </section>}
 @if(error()){<p role="alert">{{error()}}</p>}
 @if(busy()){<p role="status">Chargement…</p>}
 </div></div>
 <div class="card"><div class="card-header"><h2>Résultat, conditions et sources</h2></div><div class="card-body">
 @if(result()){
 <table class="data-table"><thead><tr><th>Programme ou bourse</th><th>Université</th><th>Éligibilité</th><th>Compatibilité</th><th>Conditions et sources</th></tr></thead><tbody>
 @for(row of rows();track $index){<tr>
 <td>{{row.name || row.programName || row.programId || row.scholarshipId}}<br>Année : {{row.academicYearId || 'Inconnue'}}<br>
 Frais : {{row.tuitionFee ?? 'Inconnus'}} {{row.tuitionCurrency || ''}}<br>
 @if(row.availability){Ouverture : {{row.availability}}<br>Échéance : {{row.deadline || 'Inconnue'}}}
 </td><td>{{row.university?.name || row.universityName || 'Inconnue'}}</td>
 <td>{{row.eligibilityStatus || row.eligibility?.status || 'INSUFFICIENT_DATA'}}</td>
 <td>{{row.score === null || row.score === undefined ? 'Indéterminée' : row.score + ' /100'}}</td>
 <td><details><summary>Voir les conditions et citations</summary>
 @for(c of row.eligibility?.criteria || [];track $index){
 <p>{{c.criterionName}} : {{c.status}}<br>Attendu : {{c.expectedValue}}<br>Observé : {{c.actualValue ?? 'Manquant'}}<br>
 {{c.sourcePassage}}<br>Page {{c.sourcePage ?? 'inconnue'}} / guide {{c.guideVersionId || 'inconnu'}} / fait {{c.sourceFactId || 'inconnu'}}</p>
 }
 @for(source of row.sources || [];track $index){<p>{{source.passage || source.sourcePassage}}<br>Page {{source.pageNumber ?? source.sourcePage ?? 'inconnue'}} / fait {{source.factId || source.sourceFactId || 'inconnu'}}</p>}
 </details></td></tr>}
 </tbody></table>
 @if(changes().length){<h3>Avant et après simulation</h3><table class="data-table"><thead><tr><th>Programme</th><th>Avant</th><th>Après</th><th>Delta de compatibilité</th></tr></thead><tbody>
 @for(change of changes();track change.programId){<tr><td>{{change.programId}}</td><td>{{change.beforeEligibility || 'Inconnu'}}</td><td>{{change.afterEligibility || 'Inconnu'}}</td><td>{{change.scoreDelta ?? 'Indéterminé'}}</td></tr>}
 </tbody></table>}
 <details><summary>Instantané et provenance complets</summary><pre style="white-space:pre-wrap;overflow-wrap:anywhere">{{result() | json}}</pre></details>
 }@else{<p>Aucun calcul sélectionné.</p>}
 <p>Les valeurs nulles correspondent à des informations inconnues. Les sources sont des données documentaires.</p>
 </div></div>
 <div class="card"><div class="card-body">
 <label for="decision-kind" class="form-label">Historique</label>
 <select id="decision-kind" class="form-input" [(ngModel)]="kind" (ngModelChange)="page=0;load()">
 <option value="SIMULATION">Simulations</option><option value="COMPARISON">Comparaisons</option><option value="SCHOLARSHIP">Bourses</option>
 </select>
 <button class="btn btn-secondary" (click)="page=0;load()" [disabled]="busy()">Consulter</button>
 <table class="data-table"><thead><tr><th>Date</th><th>Type</th><th>Profil</th><th>Actions</th></tr></thead><tbody>
 @for(row of history();track row.id){<tr><td>{{row.computedAt|date:'short'}}</td><td>{{row.kind}}</td>
 <td>{{row.profileChanged?'Modifié depuis le calcul':'Version conservée'}}<br>{{row.sourceCurrent?'Sources encore actives':'Sources historiques ou indéterminées'}}</td><td>
 <button class="btn btn-ghost btn-icon" (click)="result.set(row)" aria-label="Détails" title="Détails"><span class="material-symbols-rounded" aria-hidden="true">visibility</span></button>
 <button class="btn btn-secondary" (click)="download(row.id,'pdf')">PDF</button>
 <button class="btn btn-secondary" (click)="download(row.id,'csv')">CSV</button>
 <button class="btn btn-secondary" (click)="download(row.id,'excel')">XLSX</button>
 </td></tr>}@empty{<tr><td colspan="4">Aucun résultat chargé.</td></tr>}
 </tbody></table>
 <app-pagination [page]="page" [total]="total()" [loading]="busy()" (pageChange)="page=$event;load()" />
 </div></div></div>`
})
export class DecisionsComponent {
 @Input() reportsOnly=false;
 candidateId='';programIds='';average:number|null=null;question='';kind='SIMULATION';page=0;
 busy=signal(false);error=signal('');result=signal<unknown>(null);history=signal<DecisionView[]>([]);total=signal(0);
 private session:string|null=null;
 constructor(private api:ApiService){}
 resetContext():void {this.session=null;this.result.set(null);this.history.set([]);this.total.set(0);this.page=0;}
 private valid():boolean {
  if(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(this.candidateId))return true;
  this.error.set('Indiquez un UUID candidat valide.');return false;
 }
 rows():any[] {const v=this.result() as any;const p=v?.result??v?.data?.result??v?.data??v;return p?.programs??p?.after??p?.scholarships??(Array.isArray(p)?p:[]);}
 changes():any[] {const v=this.result() as any;const d=v?.result?.differences??v?.differences;return Array.isArray(d)?d:[];}
 private ids():string[]{return this.programIds.split(',').map(v=>v.trim()).filter(Boolean);}
 private run(request:Observable<unknown>,done?:(data:any)=>void):void {
  const candidate=this.candidateId;this.busy.set(true);this.error.set('');
  request.pipe(finalize(()=>this.busy.set(false))).subscribe({
   next:data=>{if(candidate!==this.candidateId)return;this.result.set(data);done?.(data);},
   error:e=>{if(candidate!==this.candidateId)return;this.result.set(null);this.error.set(e.error?.message||'Action refusée ou données insuffisantes.');}
  });
 }
 simulate():void {if(!this.valid())return;if(this.average===null||!Number.isFinite(this.average)){this.error.set('Une moyenne explicite est requise.');return;}
  this.run(this.api.simulateDecision({candidateId:this.candidateId,modifiedGeneralAverage:this.average,programIds:this.ids().length?this.ids():null}));}
 compare():void {if(!this.valid())return;const ids=this.ids();if(ids.length<2||ids.length>6||new Set(ids).size!==ids.length){this.error.set('Sélectionnez deux à six programmes distincts.');return;}
  this.run(this.api.compareDecision({candidateId:this.candidateId,programIds:ids}));}
 funding():void {if(this.valid())this.run(this.api.scholarshipDecision({candidateId:this.candidateId,programId:this.ids()[0]||null}));}
 load():void {if(!this.valid())return;this.history.set([]);this.total.set(0);
  this.run(this.api.decisionHistory(this.candidateId,this.kind,this.page),data=>{this.history.set(data.content);this.total.set(data.totalElements);});}
 ask():void {
  if(!this.valid()||!this.question.trim())return;
  const body={message:this.question,programIds:this.ids(),simulation:this.average===null?null:{candidateId:this.candidateId,modifiedGeneralAverage:this.average}};
  if(this.session){this.run(this.api.askDecisionChat(this.session,body));return;}
  const candidate=this.candidateId;
  this.run(this.api.createDecisionChat(candidate).pipe(switchMap(data=>{
   if(candidate!==this.candidateId)throw new Error('Candidate context changed');
   this.session=data.sessionId;return this.api.askDecisionChat(this.session!,body);
  })));
 }
 download(id:string,format:'pdf'|'csv'|'excel'):void {
  this.error.set('');this.api.exportDecision(id,format).subscribe({next:blob=>{const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download='orientia_'+id+'.'+(format==='excel'?'xlsx':format);a.click();URL.revokeObjectURL(url);},
   error:()=>this.error.set('Export refusé ou indisponible.')});
 }
}
