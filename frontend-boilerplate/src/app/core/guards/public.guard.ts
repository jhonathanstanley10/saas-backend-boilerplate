import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AppStateService } from '../services/app-state.service';

export const publicGuard: CanActivateFn = () => {
  const appState = inject(AppStateService);
  const router = inject(Router);

  if (appState.isAuthenticated()) {
    // If authenticated, redirect TO the app dashboard
    router.navigate(['/app/dashboard']);
    return false; // Prevent access to public page
  }
  // Not authenticated, allow access to public page
  return true;
};
