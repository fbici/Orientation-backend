import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService, AuthResponse } from './auth.service';
import { authInterceptor } from '../interceptors/auth.interceptor';
import { ForgotPasswordComponent } from '../../auth/forgot-password/forgot-password.component';

describe('F1 secure authentication contracts', () => {
  let auth: AuthService;
  let requests: HttpTestingController;
  let http: HttpClient;
  const response: AuthResponse = {
    accessToken: 'test-access', refreshToken: 'test-refresh', expiresIn: 900, tokenType: 'Bearer',
    user: { id: 'test-user', email: 'user@example.test', firstName: 'Test', lastName: 'User',
      roles: ['CANDIDAT'], permissions: [], tenantId: 'test-tenant' }
  };
  function login(persistent = false): void {
    auth.login('user@example.test', 'only-a-test-password', persistent).subscribe();
    requests.expectOne('/api/v1/auth/login').flush(response);
  }
  beforeEach(() => {
    localStorage.clear(); sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(),
      { provide: Router, useValue: { navigate: jasmine.createSpy().and.returnValue(Promise.resolve(true)) } }
    ] });
    auth = TestBed.inject(AuthService); requests = TestBed.inject(HttpTestingController);
    http = TestBed.inject(HttpClient);
  });
  afterEach(() => {
    clearTimeout((auth as any).tokenRefreshTimer);
    requests.verify(); localStorage.clear(); sessionStorage.clear();
  });
  it('uses session storage unless remember me is requested', () => {
    login(); expect(sessionStorage.getItem('refreshToken')).toBe('test-refresh');
    expect(localStorage.getItem('accessToken')).toBeNull();
  });
  it('keeps remembered sessions persistent across rotation', () => {
    login(true); auth.refreshToken().subscribe();
    requests.expectOne('/api/v1/auth/refresh').flush({ ...response, accessToken: 'rotated', refreshToken: 'next' });
    expect(localStorage.getItem('refreshToken')).toBe('next');
    expect(sessionStorage.getItem('refreshToken')).toBeNull();
    expect(auth.getToken()).toBe('rotated');
  });
  it('shares a single refresh request between concurrent subscribers', () => {
    login(); let results = 0;
    auth.refreshToken().subscribe(() => results++); auth.refreshToken().subscribe(() => results++);
    const pending = requests.match('/api/v1/auth/refresh');
    expect(pending.length).toBe(1); pending[0].flush({ ...response, refreshToken: 'next' });
    expect(results).toBe(2);
  });
  it('does not recursively refresh when renewal is refused', () => {
    login(); auth.refreshToken().subscribe();
    requests.expectOne('/api/v1/auth/refresh').flush({}, { status: 401, statusText: 'Unauthorized' });
    requests.expectNone('/api/v1/auth/refresh'); expect(auth.getToken()).toBeNull();
  });
  it('retries concurrent protected calls once with the same rotated credential', () => {
    login(); http.get('/api/v1/first').subscribe(); http.get('/api/v1/second').subscribe();
    requests.expectOne('/api/v1/first').flush({}, { status: 401, statusText: 'Unauthorized' });
    requests.expectOne('/api/v1/second').flush({}, { status: 401, statusText: 'Unauthorized' });
    requests.expectOne('/api/v1/auth/refresh').flush({ ...response, accessToken: 'new-access', refreshToken: 'next' });
    for (const url of ['/api/v1/first', '/api/v1/second']) {
      const retry = requests.expectOne(url);
      expect(retry.request.headers.get('Authorization')).toBe('Bearer new-access'); retry.flush({});
    }
  });
  it('never adds bearer credentials to an external service', () => {
    login(); http.get('https://external.example.test/data').subscribe();
    const request = requests.expectOne('https://external.example.test/data');
    expect(request.request.headers.has('Authorization')).toBeFalse(); request.flush({});
  });
  it('clears the local session and sends its refresh credential to logout', () => {
    login(); auth.logout();
    const request = requests.expectOne('/api/v1/auth/logout');
    expect(request.request.body.refreshToken).toBe('test-refresh');
    expect(request.request.headers.get('Authorization')).toBe('Bearer test-access');
    expect(auth.getToken()).toBeNull(); request.flush({});
  });
  it('reports unconfirmed logout without starting a refresh loop', () => {
    login(); auth.logout();
    requests.expectOne('/api/v1/auth/logout').flush({}, { status: 401, statusText: 'Unauthorized' });
    requests.expectNone('/api/v1/auth/refresh'); expect(auth.logoutWarning()).not.toBe('');
  });
  it('sends the recovery token and newPassword contract', () => {
    auth.resetPassword('test-reset-token', 'only-a-test-password').subscribe();
    const request = requests.expectOne('/api/v1/auth/reset-password');
    expect(request.request.body).toEqual({ token: 'test-reset-token', newPassword: 'only-a-test-password' });
    request.flush({});
  });
  it('does not display a sent-email success when delivery is unavailable', () => {
    const page = new ForgotPasswordComponent(auth); page.email = 'user@example.test'; page.onSubmit();
    requests.expectOne('/api/v1/auth/forgot-password').flush({ message: 'Delivery unavailable' },
      { status: 503, statusText: 'Unavailable' });
    expect(page.sent()).toBeFalse(); expect(page.error()).toBe('Delivery unavailable');
  });
  it('does not restore a session when refresh completes after logout', () => {
    login(); auth.refreshToken().subscribe(); const pending = requests.expectOne('/api/v1/auth/refresh');
    auth.logout(); requests.expectOne('/api/v1/auth/logout').flush({});
    pending.flush({ ...response, accessToken: 'late-token', refreshToken: 'late-refresh' });
    expect(auth.getToken()).toBeNull(); expect(auth.authenticated()).toBeFalse();
  });
  it('does not restore cached identity from a profile response after logout', () => {
    login(); auth.getCurrentUser().subscribe(); const pending = requests.expectOne('/api/v1/auth/me');
    auth.logout(); requests.expectOne('/api/v1/auth/logout').flush({});
    pending.flush(response.user); expect(auth.authenticated()).toBeFalse();
  });
});
