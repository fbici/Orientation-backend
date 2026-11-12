import {TestBed} from '@angular/core/testing';
import {of,throwError} from 'rxjs';
import {ApiService} from '../../core/services/api.service';
import {CandidatePickerComponent} from './candidate-picker.component';
import {DecisionsComponent} from '../../intelligence/decisions.component';
describe('Business navigation',()=>{
 it('shows report history without simulation inputs',()=>{TestBed.configureTestingModule({providers:[{provide:ApiService,useValue:{}}]});const f=TestBed.createComponent(DecisionsComponent);f.componentInstance.reportsOnly=true;f.detectChanges();expect(f.nativeElement.textContent).toContain('Rapports et exports');expect(f.nativeElement.querySelector('#decision-average')).toBeNull();expect(f.nativeElement.querySelector('#decision-kind')).not.toBeNull();});
 it('selects the actual candidate identifier and clears denied search results',()=>{const api={searchCandidates:jasmine.createSpy().and.returnValue(of([{id:'candidate-id',firstName:'Amina',lastName:'Test',email:'synthetic@example.test'}]))};TestBed.configureTestingModule({providers:[{provide:ApiService,useValue:api}]});const f=TestBed.createComponent(CandidatePickerComponent);const event=spyOn(f.componentInstance.valueChange,'emit');f.componentInstance.query='Amina';f.componentInstance.search();f.detectChanges();f.nativeElement.querySelector('.candidate-choice').click();expect(event).toHaveBeenCalledWith('candidate-id');api.searchCandidates.and.returnValue(throwError(()=>({status:403})));f.componentInstance.search();f.detectChanges();expect(f.nativeElement.querySelector('.candidate-choice')).toBeNull();expect(f.nativeElement.querySelector('[role=alert]')).not.toBeNull();});
});
