import {of,Subject,throwError} from 'rxjs';
import {UsersComponent} from './users.component';
describe('User administration contracts',()=>{
 let api:any,toast:any,page:UsersComponent;
 beforeEach(()=>{api=jasmine.createSpyObj('ApiService',['getUsers','updateUser','createUser','deleteUser']);toast=jasmine.createSpyObj('ToastService',['warning','success','error']);api.getUsers.and.returnValue(of({content:[],totalElements:0}));page=new UsersComponent(api,toast);});
 it('sends the selected server page and search term',()=>{page.page=2;page.search=' candidate ';page.load();expect(api.getUsers).toHaveBeenCalledWith(2,20,'candidate');});
 it('shows an API error separately from an empty list',()=>{api.getUsers.and.returnValue(throwError(()=>({status:403})));page.load();expect(page.error()).toBeTruthy();expect(page.loading()).toBeFalse();});
 it('updates coordinates without sending roleCode or a fake password',()=>{
  const pending=new Subject();api.updateUser.and.returnValue(pending);
  page.openEdit({id:'user',firstName:'Test',lastName:'Candidate',email:'fixture@example.invalid',tenantId:'tenant',roles:['CANDIDAT']});
  page.save();page.save();expect(api.updateUser).toHaveBeenCalledTimes(1);
  expect(api.updateUser.calls.mostRecent().args[1]).toEqual({firstName:'Test',lastName:'Candidate',email:'fixture@example.invalid',phone:undefined,tenantId:'tenant'});
 });
 it('does not call the unimplemented administrative creation API',()=>{page.openCreate();Object.assign(page.form,{firstName:'Test',lastName:'Candidate',email:'fixture@example.invalid',tenantId:'tenant'});page.save();expect(api.createUser).not.toHaveBeenCalled();});
 it('describes administrative deletion as suspension and sends only one request',()=>{
  const pending=new Subject();api.deleteUser.and.returnValue(pending);
  page.confirmDel({id:'u',firstName:'Test',lastName:'Candidate',enabled:true});
  expect(page.deleteMsg()).toContain('ne déclenche pas');page.doDel();page.doDel();expect(api.deleteUser).toHaveBeenCalledTimes(1);
  pending.next({});expect(toast.success).toHaveBeenCalledWith('Accès désactivé. Le compte est conservé.');
 });
 it('does not offer suspension for a pending personal deletion',()=>{
  page.confirmDel({id:'u',enabled:false,purgeAfter:'2026-12-11T00:00:00Z'});expect(page.showDel()).toBeFalse();expect(api.deleteUser).not.toHaveBeenCalled();
 });
});
