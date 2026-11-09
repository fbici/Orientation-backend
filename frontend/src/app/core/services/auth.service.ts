import { Injectable, signal, computed } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, finalize, shareReplay, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  id: string; email: string; firstName: string; lastName: string; phone?: string;
  roles: string[]; permissions: string[]; tenantId: string; tenantName?: string;
  emailVerified?: boolean; mfaEnabled?: boolean;
}
export interface AuthResponse {
  accessToken: string; refreshToken: string; expiresIn: number; tokenType: string; user: User;
}
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private currentUser = signal<User | null>(null);
  private isAuthenticated = signal(false);
  private tokenRefreshTimer?: ReturnType<typeof setTimeout>;
  private generation = 0;
  private refreshing?: Observable<AuthResponse | null>;
  user = this.currentUser.asReadonly();
  authenticated = this.isAuthenticated.asReadonly();
  logoutWarning = signal('');
  fullName = computed(() => this.user() ? `${this.user()!.firstName} ${this.user()!.lastName}` : '');
  initials = computed(() => `${this.user()?.firstName?.[0] || ''}${this.user()?.lastName?.[0] || ''}`.toUpperCase());

  constructor(private http: HttpClient, private router: Router) {
    try {
      const stored = this.storage().getItem('user');
      if (stored && this.getToken()) {
        this.currentUser.set(JSON.parse(stored));
        this.isAuthenticated.set(true);
        queueMicrotask(() => {
          this.getCurrentUser().subscribe({ error: () => {} });
          this.scheduleTokenRefresh();
        });
      }
    } catch { this.clearStorage(); }
  }
  private storage(): Storage {
    return localStorage.getItem('persistent') === 'true' ? localStorage : sessionStorage;
  }
  isApiUrl(url: string): boolean {
    const api = new URL(environment.apiUrl || '/', window.location.origin);
    const target = new URL(url, window.location.origin);
    return target.origin === api.origin && (api.pathname === '/' || target.pathname.startsWith(api.pathname + '/'));
  }
  login(email: string, password: string, rememberMe = false): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, { email, password })
      .pipe(tap(response => {
        this.generation++;
        this.storeTokens(response, rememberMe);
        this.currentUser.set(response.user); this.isAuthenticated.set(true);
        this.logoutWarning.set(''); this.scheduleTokenRefresh();
      }));
  }
  logout(): void {
    const refreshToken = this.getRefreshToken();
    const accessToken = this.getToken();
    this.endSession();
    if (refreshToken && accessToken) {
      this.http.post(`${this.API_URL}/logout`, { refreshToken }, {
        headers: new HttpHeaders({ Authorization: `Bearer ${accessToken}` })
      }).subscribe({ error: () => this.logoutWarning.set('D?connexion locale. La r?vocation serveur n?a pas pu ?tre confirm?e.') });
    }
  }
  private endSession(): void {
    this.generation++;
    this.clearStorage(); this.currentUser.set(null); this.isAuthenticated.set(false);
    if (this.tokenRefreshTimer) clearTimeout(this.tokenRefreshTimer);
    this.router.navigate(['/auth/login']);
  }
  refreshToken(): Observable<AuthResponse | null> {
    if (this.refreshing) return this.refreshing;
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) { this.endSession(); return of(null); }
    const generation = this.generation;
    const persistent = localStorage.getItem('persistent') === 'true';
    this.refreshing = this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken }).pipe(
      map(response => generation === this.generation ? response : null),
      tap(response => {
        if (!response) return;
        this.storeTokens(response, persistent);
        this.currentUser.set(response.user); this.isAuthenticated.set(true); this.scheduleTokenRefresh();
      }),
      catchError(() => { if (generation === this.generation) this.endSession(); return of(null); }),
      finalize(() => { this.refreshing = undefined; }),
      shareReplay({ bufferSize: 1, refCount: false })
    );
    return this.refreshing;
  }
  getCurrentUser(): Observable<User> {
    const token = this.getToken();
    return this.http.get<User>(`${this.API_URL}/me`).pipe(tap(user => {
      if (token && this.getToken() === token) { this.currentUser.set(user); this.isAuthenticated.set(true); }
    }));
  }
  updateProfile(data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/me`, data).pipe(tap(user => {
      this.currentUser.set(user); this.storage().setItem('user', JSON.stringify(user));
    }));
  }
  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.API_URL}/me/password`, { currentPassword: oldPassword, newPassword })
      .pipe(tap(() => this.endSession()));
  }
  register(data: {firstName:string;lastName:string;email:string;password:string}): Observable<any> {
    return this.http.post(`${this.API_URL}/register`, data);
  }
  verifyEmail(token:string): Observable<any> {
    return this.http.get(`${this.API_URL}/verify`, {params:{token}});
  }
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.API_URL}/forgot-password`, { email });
  }
  resetPassword(token: string, password: string): Observable<any> {
    return this.http.post(`${this.API_URL}/reset-password`, { token, newPassword: password });
  }
  hasRole(role: string): boolean { return this.user()?.roles?.includes(role) ?? false; }
  hasAnyRole(roles: string[]): boolean { return roles.some(role => this.hasRole(role)); }
  hasPermission(permission: string): boolean { return this.user()?.permissions?.includes(permission) ?? false; }
  getToken(): string | null { return this.storage().getItem('accessToken'); }
  private getRefreshToken(): string | null { return this.storage().getItem('refreshToken'); }
  private storeTokens(response: AuthResponse, persistent: boolean): void {
    this.clearStorage();
    if (persistent) localStorage.setItem('persistent', 'true');
    const storage = persistent ? localStorage : sessionStorage;
    storage.setItem('accessToken', response.accessToken); storage.setItem('refreshToken', response.refreshToken);
    storage.setItem('user', JSON.stringify(response.user));
    storage.setItem('expiresAt', String(Date.now() + response.expiresIn * 1000));
  }
  private clearStorage(): void {
    for (const storage of [localStorage, sessionStorage])
      for (const key of ['accessToken','refreshToken','user','persistent','expiresAt']) storage.removeItem(key);
  }
  private scheduleTokenRefresh(): void {
    if (this.tokenRefreshTimer) clearTimeout(this.tokenRefreshTimer);
    if (!this.getRefreshToken()) return;
    const expires = Number(this.storage().getItem('expiresAt') || 0);
    this.tokenRefreshTimer = setTimeout(() => this.refreshToken().subscribe(),
      Math.max(0, Math.min(2147483647, expires - Date.now() - 60000)));
  }
}
