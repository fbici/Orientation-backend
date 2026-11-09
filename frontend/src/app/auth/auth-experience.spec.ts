import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Subject } from 'rxjs';
import { AuthService } from '../core/services/auth.service';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { VerifyComponent } from './verify/verify.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { AuthSubmitComponent } from './shared/auth-submit.component';
import { safeReturnUrl, validPassword } from './shared/auth-errors';

describe('Auth Experience 2.0 real request states',()=>{
 let auth:any;let router:any;let route:any;let response:Subject<any>;
 beforeEach(()=>{response=new Subject();auth={login:jasmine.createSpy().and.returnValue(response),register:jasmine.createSpy().and.returnValue(response),verifyEmail:jasmine.createSpy().and.returnValue(response),resetPassword:jasmine.createSpy().and.returnValue(response),hasAnyRole:()=>false,logoutWarning:()=>''};router={navigate:jasmine.createSpy(),navigateByUrl:jasmine.createSpy()};route={snapshot:{queryParams:{},queryParamMap:{get:()=>null}}};});
 it('prevents duplicate login and waits for backend success before redirect',fakeAsync(()=>{const c=new LoginComponent(auth,router,route);c.email='test@example.invalid';c.password='valid-password';c.onLogin();c.onLogin();expect(auth.login).toHaveBeenCalledTimes(1);expect(c.state()).toBe('loading');expect(router.navigateByUrl).not.toHaveBeenCalled();response.next({});expect(c.state()).toBe('success');tick(550);expect(router.navigateByUrl).toHaveBeenCalledWith('/profile');c.ngOnDestroy();}));
 it('login failure never emits success and permits retry',()=>{const c=new LoginComponent(auth,router,route);c.email='test@example.invalid';c.password='valid-password';c.onLogin();response.error({status:401});expect(c.state()).toBe('error');expect(c.loading()).toBeFalse();expect(router.navigateByUrl).not.toHaveBeenCalled();});
 it('login timeout permits a retry without success',fakeAsync(()=>{const c=new LoginComponent(auth,router,route);c.email='test@example.invalid';c.password='valid-password';c.onLogin();tick(60000);expect(c.state()).toBe('error');expect(c.error()).toContain('trop lente');c.ngOnDestroy();}));
 it('does not call login with invalid input',()=>{const c=new LoginComponent(auth,router,route);c.onLogin();expect(auth.login).not.toHaveBeenCalled();});
 it('register sends only public candidate fields',()=>{const c=new RegisterComponent(auth);Object.assign(c,{firstName:' Test ',lastName:' Candidate ',email:'candidate@example.invalid',password:'long-password-123',confirm:'long-password-123'});c.onSubmit();c.onSubmit();expect(auth.register).toHaveBeenCalledOnceWith({firstName:'Test',lastName:'Candidate',email:'candidate@example.invalid',password:'long-password-123'});expect(c.sent()).toBeFalse();response.next({});expect(c.sent()).toBeTrue();expect(c.password).toBe('');c.ngOnDestroy();});
 it('register refuses mismatched passwords',()=>{const c=new RegisterComponent(auth);Object.assign(c,{firstName:'Test',lastName:'Candidate',email:'candidate@example.invalid',password:'long-password-123',confirm:'different'});c.onSubmit();expect(auth.register).not.toHaveBeenCalled();});
 it('register does not claim delivery on server failure',()=>{const c=new RegisterComponent(auth);Object.assign(c,{firstName:'Test',lastName:'Candidate',email:'candidate@example.invalid',password:'long-password-123',confirm:'long-password-123'});c.onSubmit();response.error({status:503});expect(c.sent()).toBeFalse();expect(c.state()).toBe('error');});
 it('uses the actual 43 character verification token, not a numeric OTP',()=>{const c=new VerifyComponent(auth,router,route);c.token='123456';c.onSubmit();expect(auth.verifyEmail).not.toHaveBeenCalled();c.token='a'.repeat(43);c.onSubmit();expect(c.state()).toBe('loading');response.next({});expect(c.state()).toBe('success');expect(c.token).toBe('');c.ngOnDestroy();});
 it('invalid verification never triggers the success animation',()=>{const c=new VerifyComponent(auth,router,route);c.token='a'.repeat(43);c.onSubmit();response.error({status:400});expect(c.state()).toBe('error');expect(c.error()).toContain('expiré');});
 it('removes the sensitive token from the browser URL without consuming it automatically',()=>{route.snapshot.queryParamMap.get=()=> 'a'.repeat(43);const c=new VerifyComponent(auth,router,route);expect(router.navigate).toHaveBeenCalledWith([],{queryParams:{token:null},queryParamsHandling:'merge',replaceUrl:true});expect(auth.verifyEmail).not.toHaveBeenCalled();c.ngOnDestroy();});
 it('reset rejects missing token and clears secrets only on success',()=>{const invalid=new ResetPasswordComponent(auth,router,route);expect(invalid.hasToken).toBeFalse();invalid.onSubmit();expect(auth.resetPassword).not.toHaveBeenCalled();route.snapshot.queryParamMap.get=()=> 'a'.repeat(43);const c=new ResetPasswordComponent(auth,router,route);c.password=c.confirm='long-password-123';c.onSubmit();response.next({});expect(c.state()).toBe('success');expect(c.password).toBe('');c.ngOnDestroy();});
 it('rejects external and backslash return URLs',()=>{for(const s of ['https://example.invalid','//example.invalid','/\\example.invalid','/auth/login','/dashboard\n'])expect(safeReturnUrl(s)).toBe('/dashboard');expect(safeReturnUrl('/profile')).toBe('/profile');});
 it('respects the bcrypt UTF-8 limit',()=>{expect(validPassword('é'.repeat(37))).toBeFalse();expect(validPassword('correct-password')).toBeTrue();});
});

describe('Auth accessible rendered controls',()=>{
 beforeEach(()=>TestBed.configureTestingModule({providers:[provideRouter([]),{provide:AuthService,useValue:{logoutWarning:()=>''}}]}));
 it('labels both fields and keeps the eye inside the password field',async()=>{const f=TestBed.createComponent(LoginComponent);f.detectChanges();await f.whenStable();const el:HTMLElement=f.nativeElement;expect(el.querySelector('label[for="login-email"]')).toBeTruthy();const eye=el.querySelector<HTMLButtonElement>('.auth-password button')!;eye.click();f.detectChanges();expect(el.querySelector<HTMLInputElement>('#login-password')!.type).toBe('text');expect(eye.getAttribute('aria-pressed')).toBe('true');eye.click();f.detectChanges();expect(el.querySelector<HTMLInputElement>('#login-password')!.type).toBe('password');});
 it('disabled Google clearly explains its unconfigured state',()=>{const f=TestBed.createComponent(LoginComponent);f.detectChanges();const google=f.nativeElement.querySelector('.auth-google');expect(google.disabled).toBeTrue();expect(f.nativeElement.querySelector('#google-status').textContent).toContain('pas encore configurée');});
 it('announces loading and disables repeated submission',()=>{const f=TestBed.createComponent(AuthSubmitComponent);f.componentRef.setInput('state','loading');f.detectChanges();const button=f.nativeElement.querySelector('button');expect(button.disabled).toBeTrue();expect(button.getAttribute('aria-busy')).toBe('true');expect(button.textContent).toContain('Traitement en cours');f.componentRef.setInput('state','error');f.detectChanges();expect(button.disabled).toBeFalse();});
});

describe('Public auth HTTP contracts',()=>{
 let auth:AuthService;let http:HttpTestingController;
 beforeEach(()=>{localStorage.clear();sessionStorage.clear();TestBed.configureTestingModule({providers:[provideRouter([]),provideHttpClient(),provideHttpClientTesting()]});auth=TestBed.inject(AuthService);http=TestBed.inject(HttpTestingController);});
 afterEach(()=>http.verify());
 it('register does not authenticate or send role/tenant claims',()=>{const data={firstName:'Test',lastName:'Candidate',email:'candidate@example.invalid',password:'long-password-123'};auth.register(data).subscribe();const req=http.expectOne('/api/v1/auth/register');expect(req.request.method).toBe('POST');expect(req.request.body).toEqual(data);req.flush({message:'accepted'},{status:202,statusText:'Accepted'});expect(auth.authenticated()).toBeFalse();});
 it('verifies using the existing GET token endpoint',()=>{auth.verifyEmail('a'.repeat(43)).subscribe();const req=http.expectOne(r=>r.url==='/api/v1/auth/verify');expect(req.request.method).toBe('GET');expect(req.request.params.get('token')).toBe('a'.repeat(43));req.flush({});});
});
