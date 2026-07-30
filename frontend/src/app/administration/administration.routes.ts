import { Routes } from '@angular/router';
export const ADMINISTRATION_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./components/administration.component').then(m => m.AdministrationComponent) },
  { path: 'users', loadComponent: () => import('./components/users.component').then(m => m.UsersComponent) },
  { path: 'roles', loadComponent: () => import('./components/roles.component').then(m => m.RolesComponent) },
  { path: 'organizations', loadComponent: () => import('./components/organizations.component').then(m => m.OrganizationsComponent) },
  { path: 'tenants', loadComponent: () => import('./components/tenants.component').then(m => m.TenantsComponent) },
  { path: 'teams', loadComponent: () => import('./components/teams.component').then(m => m.TeamsComponent) },
  { path: 'departments', loadComponent: () => import('./components/departments.component').then(m => m.DepartmentsComponent) },
];
