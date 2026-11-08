import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  if (!auth.isApiUrl(req.url)) return next(req);
  const token = auth.getToken();
  const withToken = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(withToken).pipe(catchError((error: HttpErrorResponse) => {
    const noRefresh = /\/auth\/(login|refresh|logout|register|forgot-password|reset-password|verify)(\?|$)/.test(req.url);
    if (error.status !== 401 || noRefresh) return throwError(() => error);
    return auth.refreshToken().pipe(switchMap(response => response
      ? next(req.clone({ setHeaders: { Authorization: `Bearer ${response.accessToken}` } }))
      : throwError(() => error)));
  }));
};
