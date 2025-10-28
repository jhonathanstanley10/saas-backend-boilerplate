import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment.development';
import { User } from '../models/user.model';
import { Organization } from '../models/organization.model';
import { OrganizationService } from './organization.service';
import { forkJoin, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AppStateService {
  private http = inject(HttpClient);
  private organizationService = inject(OrganizationService);
  private apiUrl = environment.apiUrl;

  // --- State Signals ---
  currentUser = signal<User | null>(null);
  currentOrganization = signal<Organization | null>(null);

  // --- Computed Signals ---
  isAuthenticated = computed(() => !!this.currentUser());
  isPremium = computed(() => this.currentOrganization()?.subscriptionStatus === 'PREMIUM');

  /**
   * Loads all essential user and organization data in parallel.
   * This is called by AuthService after login.
   */
  loadData() {
    return forkJoin({
      user: this.http.get<User>(`${this.apiUrl}/users/my-profile`),
      organization: this.organizationService.getMyOrganization(),
    }).pipe(
      tap(({ user, organization }) => {
        this.currentUser.set(user);
        this.currentOrganization.set(organization);
      }),
      catchError((err) => {
        console.error('Failed to load user data', err);
        this.clearData(); // Clear state on failure
        return of(null);
      })
    );
  }

  /**
   * Clears all state. Called by AuthService on logout.
   */
  clearData() {
    this.currentUser.set(null);
    this.currentOrganization.set(null);
  }
}