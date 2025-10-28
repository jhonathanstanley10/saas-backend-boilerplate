import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { Router } from '@angular/router';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const notification = inject(NotificationService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message = 'An unexpected error occurred. Please try again.'; // Default message

      // --- Custom Error Handling Logic ---
      switch (error.status) {
        case 400: // Bad Request (Often validation)
          if (error.error?.message === 'Validation failed' && error.error?.errors) {
            // Use the first validation error message if available
            message = error.error.errors[0]?.message || 'Validation failed. Please check your input.';
          } else if (error.error?.message) {
             // Handle specific 400s like bad password reset token
             if (error.url?.includes('/reset-password')) {
                message = 'Invalid or expired password reset token.';
             } else {
                message = error.error.message; // Use backend message directly
             }
          } else {
            message = 'Invalid request. Please check your input.';
          }
          break;

        case 401: // Unauthorized
          if (error.url?.includes('/auth/login')) {
            message = 'Invalid email or password.'; // Specific login failure
          } else {
            // General unauthorized (likely expired token)
            message = 'Session expired. Please log in again.';
            authService.logout(); // Log out on token expiry
          }
          break;

        case 403: // Forbidden
          message = 'You do not have permission to perform this action.';
          break;

        case 404: // Not Found
           // Could check req.url to customize further if needed
           message = 'The requested resource was not found.';
           break;

        case 409: // Conflict (e.g., email already exists during registration)
          if (error.url?.includes('/auth/register') && error.error?.message?.toLowerCase().includes('email')) {
            message = 'This email address is already taken.';
          } else if (error.error?.message) {
            message = error.error.message; // Use backend message
          } else {
            message = 'There was a conflict with the data submitted.';
          }
          break;

        case 500: // Internal Server Error
          message = 'An error occurred on the server. Please try again later.';
          // Avoid showing detailed server errors to the user
          break;

        // Add more cases as needed (e.g., 429 Too Many Requests)
      }

      notification.showError(message); // Show the determined message

      return throwError(() => error); // Re-throw the error for component-level handling if needed
    })
  );
};
