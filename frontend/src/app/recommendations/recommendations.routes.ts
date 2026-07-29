import { Routes } from '@angular/router';

export const RECOMMENDATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/recommendations.component').then(m => m.RecommendationsComponent)
  }
];
