import { Routes } from '@angular/router';

export const DOCUMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/documents.component').then(m => m.DocumentsComponent)
  }
];
