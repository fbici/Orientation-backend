import {TestBed} from '@angular/core/testing';
import {DataTableComponent} from './data-table.component';
import {ModalComponent} from './modal.component';

describe('Shared administrative controls',()=>{
 it('sorts numeric zero and ten numerically instead of lexically',()=>{
  const table=new DataTableComponent();table.data=[{id:1,n:10},{id:2,n:2},{id:3,n:0}];table.columns=[{key:'n',label:'Nombre'}];
  table.onSort('n');expect(table.filteredData.map(x=>x.n)).toEqual([0,2,10]);
  table.searchQuery='0';expect(table.filteredData.length).toBe(2);
 });
 it('renders the empty state when a search has no result',()=>{
  TestBed.configureTestingModule({imports:[DataTableComponent]});const f=TestBed.createComponent(DataTableComponent);
  f.componentInstance.data=[{id:1,name:'Alpha'}];f.componentInstance.columns=[{key:'name',label:'Nom'}];f.componentInstance.searchQuery='absent';f.detectChanges();
  expect(f.nativeElement.textContent).toContain('Aucune');
 });
 it('does not emit an invalid page or navigate while loading',()=>{
  const table=new DataTableComponent();table.totalPages=3;const change=spyOn(table.pageChange,'emit');
  table.goToPage(-1);table.goToPage(3);table.loading=true;table.goToPage(1);expect(change).not.toHaveBeenCalled();
  table.loading=false;table.goToPage(1);expect(change).toHaveBeenCalledWith(1);
 });
 it('blocks duplicate confirmation and closing while saving',()=>{
  TestBed.configureTestingModule({imports:[ModalComponent]});const f=TestBed.createComponent(ModalComponent);
  f.componentInstance.open=true;f.componentInstance.confirmLoading=true;
  const confirm=spyOn(f.componentInstance.confirm,'emit'),close=spyOn(f.componentInstance.close,'emit');f.detectChanges();
  f.nativeElement.querySelector('.modal-footer .btn-primary').click();f.nativeElement.querySelector('.modal-overlay').click();
  expect(confirm).not.toHaveBeenCalled();expect(close).not.toHaveBeenCalled();expect(f.nativeElement.querySelector('[role=dialog]')).not.toBeNull();
 });
});
