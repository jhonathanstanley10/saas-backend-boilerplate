import { Component, inject, signal, OnInit } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { finalize } from 'rxjs/operators';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SpinnerComponent],
  template: `
    <div class="text-center">
      <h2 class="text-3xl font-bold text-gray-800">Set New Password</h2>
      <p class="mt-2 text-gray-600">Please enter your new password.</p>
    </div>
    <form
      [formGroup]="resetPasswordForm"
      (ngSubmit)="resetPassword()"
      class="space-y-6"
    >
      <div>
        <label
          for="reset-password"
          class="block text-sm font-medium text-gray-700"
          >New Password</label
        >
        <input
          id="reset-password"
          type="password"
          formControlName="newPassword"
          required
          class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <button
        type="submit"
        [disabled]="resetPasswordForm.invalid || isLoading() || !token"
        class="flex w-full items-center justify-center rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
      >
        @if (isLoading()) {
          <app-spinner />
        } @else {
          Reset Password
        }
      </button>

      @if (!token) {
        <p class="text-center text-sm text-red-600">
          Invalid or missing reset token.
        </p>
      }
    </form>
  `,
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private notification = inject(NotificationService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isLoading = signal(false);
  token: string | null = null;

  resetPasswordForm = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
  });

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  resetPassword() {
    if (this.resetPasswordForm.invalid || !this.token) return;
    this.isLoading.set(true);

    const payload = {
      token: this.token,
      newPassword: this.resetPasswordForm.value.newPassword,
    };

    this.authService
      .resetPassword(payload)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe(() => {
        this.notification.showSuccess('Password reset! Please log in.');
        this.router.navigate(['/auth/login']);
      });
  }
}