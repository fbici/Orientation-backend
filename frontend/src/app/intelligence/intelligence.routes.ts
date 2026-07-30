import { Routes } from '@angular/router';

export const INTELLIGENCE_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./components/pipeline.component').then(m => m.PipelineComponent) },
  { path: 'smart-query', loadComponent: () => import('./components/smart-query.component').then(m => m.SmartQueryComponent) },
  { path: 'knowledge', loadComponent: () => import('./components/knowledge-browser.component').then(m => m.KnowledgeBrowserComponent) },
];
