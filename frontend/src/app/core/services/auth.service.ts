import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  roles: string[];
  permissions: string[];
  tenantId: string;
  tenantName?: string;
  emailVerified?: boolean;
  mfaEnabled?: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;

  private currentUser = signal<User | null>(null);
  private isAuthenticated = signal<boolean>(false);
  private tokenRefreshTimer: any;

  user = this.currentUser.asReadonly();
  authenticated = this.isAuthenticated.asReadonly();

  fullName = computed(() => {
    const u = this.currentUser();
    return u ? `${u.firstName} ${u.lastName}` : '';
  });

  initials = computed(() => {
    const u = this.currentUser();
    return u ? `${(u.firstName?.[0] || '').toUpperCase()}${(u.lastName?.[0] || '').toUpperCase()}` : '';
  });

  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromStorage();
    this.scheduleTokenRefresh();
  }

  login(email: string, password: string, rememberMe: boolean = false): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, { email, password, rememberMe })
      .pipe(
        tap(response => {
          this.storeTokens(response, rememberMe);
          this.currentUser.set(response.user);
          this.isAuthenticated.set(true);
          this.scheduleTokenRefresh();
        })
      );
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.API_URL}/logout`, { refreshToken }).subscribe({ error: () => {} });
    }
    this.clearStorage();
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    if (this.tokenRefreshTimer) {
      clearTimeout(this.tokenRefreshTimer);
    }
    this.router.navigate(['/auth/login']);
  }

  refreshToken(): Observable<AuthResponse | null> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return of(null);
    }
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          this.storeTokens(response, false);
          this.currentUser.set(response.user);
          this.scheduleTokenRefresh();
        }),
        catchError(() => {
          this.logout();
          return of(null);
        })
      );
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me`)
      .pipe(
        tap(user => {
          this.currentUser.set(user);
          this.isAuthenticated.set(true);
        })
      );
  }

  updateProfile(data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/me`, data)
      .pipe(
        tap(user => this.currentUser.set(user))
      );
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.API_URL}/me/password`, { oldPassword, newPassword });
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.API_URL}/forgot-password`, { email });
  }

  resetPassword(token: string, password: string): Observable<any> {
    return this.http.post(`${this.API_URL}/reset-password`, { token, password });
  }

  hasRole(role: string): boolean {
    return this.currentUser()?.roles?.includes(role) ?? false;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(r => this.hasRole(r));
  }

  hasPermission(permission: string): boolean {
    return this.currentUser()?.permissions?.includes(permission) ?? false;
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
  }

  private getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken');
  }

  private storeTokens(response: AuthResponse, persistent: boolean): void {
    const storage = persistent ? localStorage : sessionStorage;
    storage.setItem('accessToken', response.accessToken);
    storage.setItem('refreshToken', response.refreshToken);
    localStorage.setItem('user', JSON.stringify(response.user));
    if (persistent) {
      localStorage.setItem('persistent', 'true');
    }
  }

  private clearStorage(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    localStorage.removeItem('persistent');
    sessionStorage.removeItem('accessToken');
    sessionStorage.removeItem('refreshToken');
  }

  private loadUserFromStorage(): void {
    const userStr = localStorage.getItem('user');
    const token = this.getToken();
    if (userStr && token) {
      try {
        this.currentUser.set(JSON.parse(userStr));
        this.isAuthenticated.set(true);
        // Refresh user data from server
        this.getCurrentUser().subscribe({ error: () => {} });
      } catch {
        this.clearStorage();
      }
    }
  }

  private scheduleTokenRefresh(): void {
    if (this.tokenRefreshTimer) {
      clearTimeout(this.tokenRefreshTimer);
    }
    // Refresh 2 minutes before expiry (default 24h = 86400s, refresh at 86280s)
    const refreshMs = 23 * 60 * 60 * 1000; // 23 hours
    this.tokenRefreshTimer = setTimeout(() => {
      this.refreshToken().subscribe({ error: () => {} });
    }, refreshMs);
  }
}
