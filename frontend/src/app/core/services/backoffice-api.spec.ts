import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting,HttpTestingController} from '@angular/common/http/testing';
import {ApiService} from './api.service';
import {environment} from '../../../environments/environment';
describe('Administrative HTTP contracts',()=>{
 let api:ApiService,http:HttpTestingController;
 beforeEach(()=>{TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});api=TestBed.inject(ApiService);http=TestBed.inject(HttpTestingController);});afterEach(()=>http.verify());
 it('preserves search and the requested users page',()=>{api.getUsers(2,20,'Alice').subscribe();const r=http.expectOne(r=>r.url===environment.apiUrl+'/admin/users');expect(r.request.params.get('search')).toBe('Alice');expect(r.request.params.get('page')).toBe('2');r.flush({content:[],totalElements:0});});
 it('loads the real audit center endpoint',()=>{api.getActivityLogs(1,20).subscribe();const r=http.expectOne(r=>r.url===environment.apiUrl+'/backoffice/audit');expect(r.request.params.get('page')).toBe('1');r.flush({events:[],totalEvents:0,page:1,size:20});});
 it('does not truncate tenant navigation to the first page',()=>{api.getTenants(3,20).subscribe();const r=http.expectOne(r=>r.url===environment.apiUrl+'/admin/tenants');expect(r.request.params.get('page')).toBe('3');r.flush({content:[],totalElements:0});});
});
