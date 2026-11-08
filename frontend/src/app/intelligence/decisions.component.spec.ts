import {of,throwError,Subject} from 'rxjs';
import {DecisionsComponent} from './decisions.component';
import {ApiService} from '../core/services/api.service';
describe('Decision administration',()=>{
 const id='11111111-1111-4111-8111-111111111111', p1='22222222-2222-4222-8222-222222222222',p2='33333333-3333-4333-8333-333333333333';
 let api:jasmine.SpyObj<ApiService>,c:DecisionsComponent;
 beforeEach(()=>{api=jasmine.createSpyObj('ApiService',['simulateDecision','compareDecision','scholarshipDecision','decisionHistory','createDecisionChat','askDecisionChat','exportDecision']);c=new DecisionsComponent(api);c.candidateId=id;});
 it('sends explicit hypothetical values without a profile write',()=>{api.simulateDecision.and.returnValue(of({kind:'SIMULATION'}));c.average=13;c.simulate();expect(api.simulateDecision).toHaveBeenCalledWith({candidateId:id,modifiedGeneralAverage:13,programIds:null});});
 it('rejects missing simulation values',()=>{c.simulate();expect(api.simulateDecision).not.toHaveBeenCalled();expect(c.error()).toBeTruthy();});
 it('rejects duplicate comparison selections',()=>{c.programIds=p1+','+p1;c.compare();expect(api.compareDecision).not.toHaveBeenCalled();});
 it('sends selected programmes and candidate to the real comparator',()=>{api.compareDecision.and.returnValue(of({}));c.programIds=p1+','+p2;c.compare();expect(api.compareDecision).toHaveBeenCalledWith({candidateId:id,programIds:[p1,p2]});});
 it('clears prior data on access refusal',()=>{api.scholarshipDecision.and.returnValue(throwError(()=>({status:403})));c.result.set({eligibilityStatus:'ELIGIBLE'});c.funding();expect(c.result()).toBeNull();expect(c.error()).toBeTruthy();expect(c.busy()).toBeFalse();});
 it('loads paginated history using an explicit decision kind',()=>{api.decisionHistory.and.returnValue(of({content:[],totalElements:0}));c.kind='COMPARISON';c.page=2;c.load();expect(api.decisionHistory).toHaveBeenCalledWith(id,'COMPARISON',2);});
 it('starts a new conversation when the candidate changes',()=>{api.createDecisionChat.and.returnValue(of({sessionId:p1}));api.askDecisionChat.and.returnValue(of({status:'ANSWERED'}));c.question='Bourses';c.ask();c.candidateId=p2;c.resetContext();c.ask();expect(api.createDecisionChat.calls.count()).toBe(2);expect(api.createDecisionChat.calls.mostRecent().args).toEqual([p2]);});
 it('does not display a late response belonging to the previous candidate',()=>{const pending=new Subject<any>();api.scholarshipDecision.and.returnValue(pending);c.funding();c.candidateId=p2;c.resetContext();pending.next({private:'old candidate'});pending.complete();expect(c.result()).toBeNull();});

 it('shows simulation outcomes and deltas from the stored result',()=>{const row={programId:p1,eligibilityStatus:'INSUFFICIENT_DATA',score:null};const delta={programId:p1,scoreDelta:null};c.result.set({kind:'SIMULATION',result:{after:[row],differences:[delta]}});expect(c.rows()).toEqual([row]);expect(c.changes()).toEqual([delta]);});
 it('uses actual comparison and scholarship payloads without manufacturing rows',()=>{c.result.set({programs:[{programId:p1}]});expect(c.rows().length).toBe(1);c.result.set({result:{scholarships:[]}});expect(c.rows()).toEqual([]);});
});
