import {Component,Input,Output,EventEmitter} from '@angular/core';
@Component({selector:'app-pagination',standalone:true,template:`
<div class="pagination-bar"><span role="status">{{start}}–{{end}} sur {{total}} résultats</span>
 <nav aria-label="Pagination"><button type="button" class="btn btn-ghost btn-icon" aria-label="Première page" title="Première page" [disabled]="loading||page<=0" (click)="go(0)"><span class="material-symbols-rounded" aria-hidden="true">first_page</span></button>
 <button type="button" class="btn btn-ghost btn-icon" aria-label="Page précédente" title="Page précédente" [disabled]="loading||page<=0" (click)="go(page-1)"><span class="material-symbols-rounded" aria-hidden="true">chevron_left</span></button>
 @for(p of pages;track $index){@if(p===null){<span aria-hidden="true">…</span>}@else{<button type="button" class="btn btn-sm" [class.btn-primary]="p===page" [class.btn-ghost]="p!==page" [attr.aria-current]="p===page?'page':null" [attr.aria-label]="'Page '+(p+1)" [disabled]="loading" (click)="go(p)">{{p+1}}</button>}}
 <button type="button" class="btn btn-ghost btn-icon" aria-label="Page suivante" title="Page suivante" [disabled]="loading||page>=count-1" (click)="go(page+1)"><span class="material-symbols-rounded" aria-hidden="true">chevron_right</span></button>
 <button type="button" class="btn btn-ghost btn-icon" aria-label="Dernière page" title="Dernière page" [disabled]="loading||page>=count-1" (click)="go(count-1)"><span class="material-symbols-rounded" aria-hidden="true">last_page</span></button></nav></div>`})
export class PaginationComponent {
 @Input() page=0;@Input() total=0;@Input() size=20;@Input() loading=false;
 @Output() pageChange=new EventEmitter<number>();
 get count(){return Math.max(1,Math.ceil(this.total/Math.max(1,this.size)));}
 get start(){return this.total===0?0:this.page*this.size+1;}
 get end(){return Math.min(this.total,(this.page+1)*this.size);}
 get pages():(number|null)[]{const values=[...new Set([0,this.page-1,this.page,this.page+1,this.count-1])].filter(p=>p>=0&&p<this.count).sort((a,b)=>a-b);const result:(number|null)[]=[];for(const p of values){const previous=result[result.length-1];if(typeof previous==='number'&&p>previous+1)result.push(null);result.push(p);}return result;}
 go(page:number){if(!this.loading&&Number.isInteger(page)&&page>=0&&page<this.count&&page!==this.page)this.pageChange.emit(page);}
}
