import {TestBed} from '@angular/core/testing';
import {PaginationComponent} from './pagination.component';
describe('Shared pagination',()=>{
 it('shows the actual result range and navigates to the last page',()=>{
  const f=TestBed.createComponent(PaginationComponent);f.componentInstance.total=43;f.componentInstance.page=1;
  const event=spyOn(f.componentInstance.pageChange,'emit');f.detectChanges();
  expect(f.nativeElement.textContent).toContain('21–40 sur 43');
  f.nativeElement.querySelector('[aria-label="Dernière page"]').click();expect(event).toHaveBeenCalledWith(2);
 });
 it('blocks navigation while a server request is pending',()=>{
  const f=TestBed.createComponent(PaginationComponent);f.componentInstance.total=200;f.componentInstance.loading=true;
  const event=spyOn(f.componentInstance.pageChange,'emit');f.detectChanges();
  f.nativeElement.querySelector('[aria-label="Page suivante"]').click();expect(event).not.toHaveBeenCalled();
 });
 it('keeps a large page count compact and does not invent results on an empty list',()=>{
  const f=TestBed.createComponent(PaginationComponent);f.componentInstance.total=20000;f.componentInstance.page=500;f.detectChanges();
  expect(f.nativeElement.querySelectorAll('button').length).toBeLessThan(12);
  f.componentInstance.total=0;f.componentInstance.page=0;f.detectChanges();expect(f.nativeElement.textContent).toContain('0–0 sur 0');
 });
});
