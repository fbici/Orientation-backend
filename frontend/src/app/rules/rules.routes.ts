import { Routes } from '@angular/router';

export const RULES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/rules.component').then(m => m.RulesComponent)
  }
];
