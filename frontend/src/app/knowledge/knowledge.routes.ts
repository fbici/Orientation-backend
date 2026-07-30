import { Routes } from '@angular/router';

export const KNOWLEDGE_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./components/knowledge.component').then(m => m.KnowledgeComponent) },
  { path: 'smart-query', loadComponent: () => import('./components/smart-query.component').then(m => m.SmartQueryComponent) },
];
