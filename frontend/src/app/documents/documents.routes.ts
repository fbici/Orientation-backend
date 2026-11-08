import { Routes } from '@angular/router';
export const DOCUMENTS_ROUTES: Routes = [
  { path: ':id/review', loadComponent: () => import('./components/document-review.component').then(m => m.DocumentReviewComponent) },
  { path: '', loadComponent: () => import('./components/documents.component').then(m => m.DocumentsComponent) }
];
