import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { finalize } from 'rxjs/operators';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SpinnerComponent],
  template: `
    <div class="text-center">
      <h2 class="text-3xl font-bold text-gray-800">Welcome Back!</h2>
      <p class="mt-2 text-gray-600">Sign in to continue to your workspace.</p>
    </div>
    <form
      [formGroup]="loginForm"
      (ngSubmit)="login()"
      class="space-y-6"
    >
      <div>
        <label
          for="login-email"
          class="block text-sm font-medium text-gray-700"
          >Email address</label
        >
        <input
          id="login-email"
          type="email"
          formControlName="email"
          required
          class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <div>
        <div class="flex items-center justify-between">
          <label
            for="login-password"
            class="block text-sm font-medium text-gray-700"
            >Password</label
          >
          <a
            routerLink="/auth/forgot-password"
            class="cursor-pointer text-sm font-medium text-indigo-600 hover:text-indigo-500"
          >
            Forgot password?
          </a>
        </div>
        <input
          id="login-password"
          type="password"
          formControlName="password"
          required
          class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <button
        type="submit"
        [disabled]="loginForm.invalid || isLoading()"
        class="flex w-full items-center justify-center rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
      >
        @if (isLoading()) {
          <app-spinner />
        } @else {
          Sign In
        }
      </button>

      <p class="text-center text-sm text-gray-600">
        Don't have an account?
        <a
          routerLink="/auth/register"
          class="cursor-pointer font-medium text-indigo-600 hover:text-indigo-500"
        >
          Sign up
        </a>
      </p>
    </form>
  `,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private notification = inject(NotificationService);

  isLoading = signal(false);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  login() {
    if (this.loginForm.invalid) return;
    this.isLoading.set(true);

    this.authService
      .login(this.loginForm.value)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: (err) => {
          // Error is already handled by the interceptor, but you could add specific logic here
        },
      });
  }
}