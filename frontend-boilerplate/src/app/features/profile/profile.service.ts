import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment.development'; // <-- FIX: Using path alias
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from '../../core/models/user.model'; // <-- FIX: Using path alias
import { AppStateService } from '../../core/services/app-state.service'; // <-- FIX: Using path alias
import { NotificationService } from '../../core/services/notification.service'; // <-- FIX: Using path alias

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  private http = inject(HttpClient);
  private appState = inject(AppStateService);
  private notification = inject(NotificationService);
  private apiUrl = `${environment.apiUrl}/users`;

  updateProfile(payload: {
    firstName: string;
    lastName: string;
  }): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/my-profile`, payload).pipe(
      tap((updatedUser) => {
        // Update the global state
        this.appState.currentUser.set(updatedUser);
        this.notification.showSuccess('Profile updated successfully!');
      })
    );
  }
}

