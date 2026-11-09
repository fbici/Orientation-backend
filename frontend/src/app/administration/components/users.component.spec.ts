import {of,Subject,throwError} from 'rxjs';
import {UsersComponent} from './users.component';
describe('User administration contracts',()=>{
 let api:any,toast:any,page:UsersComponent;
 beforeEach(()=>{api=jasmine.createSpyObj('ApiService',['getUsers','updateUser','createUser']);toast=jasmine.createSpyObj('ToastService',['warning','success','error']);api.getUsers.and.returnValue(of({content:[],totalElements:0}));page=new UsersComponent(api,toast);});
 it('sends the selected server page and search term',()=>{page.page=2;page.search=' candidate ';page.load();expect(api.getUsers).toHaveBeenCalledWith(2,20,'candidate');});
 it('shows an API error separately from an empty list',()=>{api.getUsers.and.returnValue(throwError(()=>({status:403})));page.load();expect(page.error()).toBeTruthy();expect(page.loading()).toBeFalse();});
 it('updates coordinates without sending roleCode or a fake password',()=>{
  const pending=new Subject();api.updateUser.and.returnValue(pending);
  page.openEdit({id:'user',firstName:'Test',lastName:'Candidate',email:'fixture@example.invalid',tenantId:'tenant',roles:['CANDIDAT']});
  page.save();page.save();expect(api.updateUser).toHaveBeenCalledTimes(1);
  expect(api.updateUser.calls.mostRecent().args[1]).toEqual({firstName:'Test',lastName:'Candidate',email:'fixture@example.invalid',phone:undefined,tenantId:'tenant'});
 });
 it('does not call the unimplemented administrative creation API',()=>{page.openCreate();Object.assign(page.form,{firstName:'Test',lastName:'Candidate',email:'fixture@example.invalid',tenantId:'tenant'});page.save();expect(api.createUser).not.toHaveBeenCalled();});
});
