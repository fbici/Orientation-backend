import { Routes } from '@angular/router';

export const INTELLIGENCE_ROUTES: Routes = [
  {path:'decisions',loadComponent:()=>import('./decisions.component').then(m=>m.DecisionsComponent)},
  { path: '', loadComponent: () => import('../documents/components/documents.component').then(m => m.DocumentsComponent) },
  { path: 'smart-query', loadComponent: () => import('../knowledge/components/smart-query.component').then(m => m.SmartQueryComponent) },
  { path: 'knowledge', loadComponent: () => import('../knowledge/components/knowledge.component').then(m => m.KnowledgeComponent) },
];
