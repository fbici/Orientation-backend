import {of,throwError} from 'rxjs';
import {UniversitiesComponent} from './universities.component';
describe('University catalogue response contract',()=>{
 it('preserves flattened country and city identifiers on edit',()=>{const api:any={getCitiesByCountry:()=>of([])};const page=new UniversitiesComponent(api,{} as any);page.openEdit({id:'u',name:'University',countryId:'country',cityId:'city'});expect(page.form.countryId).toBe('country');expect(page.form.cityId).toBe('city');});
 it('shows load failures separately from an empty catalogue',()=>{const page=new UniversitiesComponent({catalog:()=>throwError(()=>({status:503}))} as any,{} as any);page.load();expect(page.error()).toBeTruthy();expect(page.loading()).toBeFalse();});
});
