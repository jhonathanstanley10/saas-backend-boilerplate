import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment.development';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BillingService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/billing`;

  createCheckoutSession(): Observable<{ url: string }> {
    const payload = {
      successUrl: `${window.location.origin}/app/billing?payment_success=true`,
      cancelUrl: `${window.location.origin}/app/billing`,
    };
    return this.http.post<{ url: string }>(
      `${this.apiUrl}/create-checkout-session`,
      payload
    );
  }

  createPortalSession(): Observable<{ url: string }> {
    const payload = {
      returnUrl: `${window.location.origin}/app/billing`
    };
    console.log(`${window.location.origin}/app/billing`)
    return this.http.post<{ url: string }>(
      `${this.apiUrl}/create-portal-session`,
      payload
    );
  }
}
