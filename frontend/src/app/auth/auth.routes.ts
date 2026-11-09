import { Routes } from '@angular/router';
import { AuthShellComponent } from './shared/auth-shell.component';
export const AUTH_ROUTES: Routes = [{path:'',component:AuthShellComponent,children:[
 {path:'login',loadComponent:()=>import('./login/login.component').then(m=>m.LoginComponent)},
 {path:'register',loadComponent:()=>import('./register/register.component').then(m=>m.RegisterComponent)},
 {path:'verify-email',loadComponent:()=>import('./verify/verify.component').then(m=>m.VerifyComponent)},
 {path:'forgot-password',loadComponent:()=>import('./forgot-password/forgot-password.component').then(m=>m.ForgotPasswordComponent)},
 {path:'reset-password',loadComponent:()=>import('./reset-password/reset-password.component').then(m=>m.ResetPasswordComponent)},
 {path:'',redirectTo:'login',pathMatch:'full'}
]}];
