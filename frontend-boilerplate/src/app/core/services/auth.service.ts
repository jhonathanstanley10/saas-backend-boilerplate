import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment.development';
import type { AuthResponse } from '../models/auth-response.model';
import { Observable, of } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';
import { AppStateService } from './app-state.service'; // Import new state service

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private appState = inject(AppStateService); // Inject state service
  private apiUrl = environment.apiUrl;

  constructor() {
    this.loadUserFromToken();
  }

  private loadUserFromToken() {
    const token = this.getToken();
    if (token) {
      this.appState.loadData().subscribe();
    }
  }

  // --- Auth API Calls ---

  register(payload: any): Observable<any> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/auth/register`, payload)
      .pipe(
        tap((res) => this.setSession(res)),
        switchMap(() => this.appState.loadData()) // Load state after registering
      );
  }

  login(payload: any): Observable<any> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/auth/login`, payload)
      .pipe(
        tap((res) => this.setSession(res)),
        switchMap(() => this.appState.loadData()) // Load state after login
      );
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/forgot-password`, { email });
  }

  resetPassword(payload: any): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/reset-password`, payload);
  }

  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
    this.appState.clearData(); // Clear state
    this.router.navigate(['/auth/login']);
  }

  // --- Token Management ---

  private setSession(authResponse: AuthResponse) {
    localStorage.setItem('authToken', authResponse.token);
    localStorage.setItem('refreshToken', authResponse.refreshToken);
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }
}