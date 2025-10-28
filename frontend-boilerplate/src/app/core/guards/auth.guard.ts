import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AppStateService } from '../services/app-state.service';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs';

export const authGuard: CanActivateFn = () => {
  const appState = inject(AppStateService);
  const authService = inject(AuthService);
  const router = inject(Router);

  if (appState.isAuthenticated()) {
    return true; // Already authenticated, allow access
  }

  // If we have a token but no state, try to load data
  if (authService.getToken()) {
    return appState.loadData().pipe(
      map((data) => {
        if (data) {
          return true; // Loaded data successfully, allow access
        }
        // Failed to load data (e.g., bad token), redirect to landing
        return router.createUrlTree(['/']);
      })
    );
  }

  // No token, redirect to landing page
  return router.createUrlTree(['/']);
};
