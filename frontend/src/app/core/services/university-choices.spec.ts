import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting,HttpTestingController} from '@angular/common/http/testing';
import {ApiService} from './api.service';
describe('University choices across countries',()=>{
 let api:ApiService,http:HttpTestingController;
 beforeEach(()=>{TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});api=TestBed.inject(ApiService);http=TestBed.inject(HttpTestingController);});
 afterEach(()=>http.verify());
 it('loads later pages instead of silently limiting university choices to one hundred',()=>{
  let choices:any[]=[];api.allUniversityChoices('benin-id').subscribe(rows=>choices=rows);
  const first=http.expectOne(r=>r.url.endsWith('/universities')&&r.params.get('page')==='0');
  expect(first.request.params.get('countryId')).toBe('benin-id');
  first.flush({content:[{id:'first'}],number:0,totalPages:2,last:false});expect(choices).toEqual([]);
  const next=http.expectOne(r=>r.params.get('page')==='1');next.flush({content:[{id:'later'}],number:1,totalPages:2,last:true});
  expect(choices.map(row=>row.id)).toEqual(['first','later']);
 });
 it('reports a failed later page rather than exposing a partial selector as complete',()=>{
  let failed=false;api.allUniversityChoices().subscribe({next:()=>fail('No partial success'),error:()=>failed=true});
  http.expectOne(r=>r.params.get('page')==='0').flush({content:[{id:'first'}],number:0,totalPages:2,last:false});
  http.expectOne(r=>r.params.get('page')==='1').flush({}, {status:503,statusText:'Unavailable'});expect(failed).toBeTrue();
 });
});
