import { Routes } from '@angular/router';
export const MONITORING_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./components/monitoring.component').then(m => m.MonitoringComponent) }
];
