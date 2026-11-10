import {TestBed} from '@angular/core/testing';
import {provideRouter,Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
import {roleGuard} from './role.guard';
import {backofficeGuard} from './backoffice.guard';
import {authGuard} from './auth.guard';
describe('Backoffice access guards',()=>{
 let auth:any;
 beforeEach(()=>{auth={authenticated:()=>true,hasRole:()=>false,hasAnyRole:()=>false};TestBed.configureTestingModule({providers:[provideRouter([]),{provide:AuthService,useValue:auth}]});});
 it('blocks a candidate before loading the administrative layout',()=>{const result=TestBed.runInInjectionContext(()=>backofficeGuard({} as any,{} as any));expect(TestBed.inject(Router).serializeUrl(result as any)).toBe('/mobile-access');});
 it('allows a super administrator through the staff boundary',()=>{auth.hasAnyRole=()=>true;expect(TestBed.runInInjectionContext(()=>backofficeGuard({} as any,{} as any))).toBeTrue();});
 it('redirects an authenticated user denied a role to the profile',()=>{const result=TestBed.runInInjectionContext(()=>roleGuard({data:{roles:['ADMIN']}} as any,{} as any));expect(TestBed.inject(Router).serializeUrl(result as any)).toBe('/profile');});
 it('allows an administrator on the administrative route',()=>{auth.hasRole=()=>true;expect(TestBed.runInInjectionContext(()=>roleGuard({data:{roles:['ADMIN']}} as any,{} as any))).toBeTrue();});
 it('preserves the target when authentication is absent',()=>{auth.authenticated=()=>false;const navigate=spyOn(TestBed.inject(Router),'navigate');expect(TestBed.runInInjectionContext(()=>authGuard({} as any,{url:'/documents'} as any))).toBeFalse();expect(navigate).toHaveBeenCalledWith(['/auth/login'],{queryParams:{returnUrl:'/documents'}});});
});
