import { Routes } from '@angular/router';

export const UNIVERSITIES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./components/universities.component').then(m => m.UniversitiesComponent) },
  { path: ':id', loadComponent: () => import('./components/university-detail.component').then(m => m.UniversityDetailComponent) },
];
