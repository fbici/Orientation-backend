import {OrganizationsComponent} from './organizations.component';
import {of} from 'rxjs';
describe('Organization form preserves backend fields',()=>{
 it('retains the code and contacts when opening an edit',()=>{const page=new OrganizationsComponent({} as any,{} as any);page.openEdit({id:'o',name:'Synthetic',code:'ORG',country:'Benin',email:'fixture@example.invalid',phone:'123',website:'https://example.invalid'});expect(page.form.code).toBe('ORG');expect(page.form.email).toBe('fixture@example.invalid');expect(page.form.phone).toBe('123');});
 it('does not send an organization without the required code',()=>{const api=jasmine.createSpyObj('Api',['createOrganization']);const toast=jasmine.createSpyObj('Toast',['warning']);const page=new OrganizationsComponent(api,toast);page.openCreate();page.form.name='Synthetic';page.save();expect(api.createOrganization).not.toHaveBeenCalled();});
});
