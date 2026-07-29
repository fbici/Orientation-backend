import { Routes } from '@angular/router';

export const IMPORTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/imports.component').then(m => m.ImportsComponent)
  }
];
