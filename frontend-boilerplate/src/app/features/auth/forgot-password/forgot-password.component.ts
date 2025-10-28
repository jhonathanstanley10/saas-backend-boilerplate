import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { finalize } from 'rxjs/operators';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SpinnerComponent],
  template: `
    <div class="text-center">
      <h2 class="text-3xl font-bold text-gray-800">Reset Password</h2>
      <p class="mt-2 text-gray-600">Enter your email to get a reset link.</p>
    </div>
    <form
      [formGroup]="forgotPasswordForm"
      (ngSubmit)="sendResetLink()"
      class="space-y-6"
    >
      <div>
        <label
          for="forgot-email"
          class="block text-sm font-medium text-gray-700"
          >Email address</label
        >
        <input
          id="forgot-email"
          type="email"
          formControlName="email"
          required
          class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <button
        type="submit"
        [disabled]="forgotPasswordForm.invalid || isLoading()"
        class="flex w-full items-center justify-center rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
      >
        @if (isLoading()) {
          <app-spinner />
        } @else {
          Send Reset Link
        }
      </button>

      <p class="text-center text-sm text-gray-600">
        Remembered your password?
        <a
          routerLink="/auth/login"
          class="cursor-pointer font-medium text-indigo-600 hover:text-indigo-500"
        >
          Sign in
        </a>
      </p>
    </form>
  `,
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private notification = inject(NotificationService);
  isLoading = signal(false);

  forgotPasswordForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  sendResetLink() {
    if (this.forgotPasswordForm.invalid) return;
    this.isLoading.set(true);

    this.authService
      .forgotPassword(this.forgotPasswordForm.value.email!)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe(() => {
        this.notification.showSuccess('Reset link sent! Check your email.');
        this.forgotPasswordForm.reset();
      });
  }
}