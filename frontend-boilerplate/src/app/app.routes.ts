import { Routes } from '@angular/router';
import { AppLayoutComponent } from './layout/app-layout/app-layout.component';
import { AuthLayoutComponent } from './layout/auth-layout/auth-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { publicGuard } from './core/guards/public.guard';

export const routes: Routes = [
  // --- NEW: Landing Page Route ---
  {
    path: '', // Root path
    loadComponent: () =>
      import('./features/landing/landing.component').then(
        (m) => m.LandingPageComponent
      ),
    canActivate: [publicGuard], // If logged in, redirect away from landing
  },

  // --- Authenticated App Routes (Path changed slightly) ---
  {
    path: 'app', // Base path for the authenticated app
    component: AppLayoutComponent,
    canActivate: [authGuard], // Protect this whole section
    children: [
      {
        path: '', // Redirect '/app' to '/app/dashboard'
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/profile.component').then(
            (m) => m.ProfileComponent
          ),
      },
      {
        path: 'billing',
        loadComponent: () =>
          import('./features/billing/billing.component').then(
            (m) => m.BillingComponent
          ),
      },
    ],
  },

  // --- Auth Routes (Login, Register, etc. - No change) ---
  {
    path: 'auth',
    component: AuthLayoutComponent,
    canActivate: [publicGuard], // If logged in, redirect away from auth pages
    children: [
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full',
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login.component').then(
            (m) => m.LoginComponent
          ),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/register/register.component').then(
            (m) => m.RegisterComponent
          ),
      },
      {
        path: 'forgot-password',
        loadComponent: () =>
          import('./features/auth/forgot-password/forgot-password.component').then(
            (m) => m.ForgotPasswordComponent
          ),
      },
      {
        path: 'reset-password',
        loadComponent: () =>
          import('./features/auth/reset-password/reset-password.component').then(
            (m) => m.ResetPasswordComponent
          ),
      },
    ],
  },

  // --- Fallback Route ---
  {
    path: '**',
    redirectTo: '', // Redirect unknown paths to the landing page
  },
];
