import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment.development';
import { Observable } from 'rxjs';
import { Organization } from '../models/organization.model';

@Injectable({
  providedIn: 'root',
})
export class OrganizationService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/organizations`;

  /**
   * Fetches the organization for the currently authenticated user.
   * NOTE: Assumes you have created a `GET /api/organizations/my-organization`
   * endpoint on your backend.
   */
  getMyOrganization(): Observable<Organization> {
    return this.http.get<Organization>(`${this.apiUrl}/my-organization`);
  }
}