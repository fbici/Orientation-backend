import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/layout.component').then(m => m.LayoutComponent),
    children: [
      { path: 'dashboard', loadChildren: () => import('./dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'analytics', loadChildren: () => import('./analytics/analytics.routes').then(m => m.ANALYTICS_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'monitoring', loadChildren: () => import('./monitoring/monitoring.routes').then(m => m.MONITORING_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'universities', loadChildren: () => import('./universities/universities.routes').then(m => m.UNIVERSITIES_ROUTES) },
      { path: 'recommendations', loadChildren: () => import('./recommendations/recommendations.routes').then(m => m.RECOMMENDATIONS_ROUTES) },
      { path: 'documents', loadChildren: () => import('./documents/documents.routes').then(m => m.DOCUMENTS_ROUTES) },
      { path: 'imports', loadChildren: () => import('./imports/imports.routes').then(m => m.IMPORTS_ROUTES) },
      { path: 'rules', loadChildren: () => import('./rules/rules.routes').then(m => m.RULES_ROUTES) },
      { path: 'intelligence', loadChildren: () => import('./intelligence/intelligence.routes').then(m => m.INTELLIGENCE_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'knowledge', loadChildren: () => import('./knowledge/knowledge.routes').then(m => m.KNOWLEDGE_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'notifications', loadChildren: () => import('./notifications/notifications.routes').then(m => m.NOTIFICATIONS_ROUTES) },
      { path: 'administration', loadChildren: () => import('./administration/administration.routes').then(m => m.ADMINISTRATION_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'settings', loadChildren: () => import('./settings/settings.routes').then(m => m.SETTINGS_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'reports', loadChildren: () => import('./reports/reports.routes').then(m => m.REPORTS_ROUTES), canActivate: [roleGuard], data: { roles: ['SUPER_ADMIN', 'ADMIN'] } },
      { path: 'profile', loadChildren: () => import('./profile/profile.routes').then(m => m.PROFILE_ROUTES) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
