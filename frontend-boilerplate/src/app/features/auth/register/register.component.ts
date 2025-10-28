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
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SpinnerComponent],
  template: `
    <div class="text-center">
      <h2 class="text-3xl font-bold text-gray-800">Create Account</h2>
      <p class="mt-2 text-gray-600">Get started with your new workspace.</p>
    </div>
    <form
      [formGroup]="registerForm"
      (ngSubmit)="register()"
      class="space-y-6"
    >
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label
            for="reg-firstName"
            class="block text-sm font-medium text-gray-700"
            >First Name</label
          >
          <input
            id="reg-firstName"
            type="text"
            formControlName="firstName"
            required
            class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
        <div>
          <label
            for="reg-lastName"
            class="block text-sm font-medium text-gray-700"
            >Last Name</label
          >
          <input
            id="reg-lastName"
            type="text"
            formControlName="lastName"
            required
            class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
      </div>

      <div>
        <label
          for="reg-email"
          class="block text-sm font-medium text-gray-700"
          >Email address</label
        >
        <input
          id="reg-email"
          type="email"
          formControlName="email"
          required
          class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <div>
        <label
          for="reg-password"
          class="block text-sm font-medium text-gray-700"
          >Password</label
        >
        <input
          id="reg-password"
          type="password"
          formControlName="password"
          required
          class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2.5 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <button
        type="submit"
        [disabled]="registerForm.invalid || isLoading()"
        class="flex w-full items-center justify-center rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
      >
        @if (isLoading()) {
          <app-spinner />
        } @else {
          Create Account
        }
      </button>

      <p class="text-center text-sm text-gray-600">
        Already have an account?
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
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private notification = inject(NotificationService);

  isLoading = signal(false);

  registerForm = this.fb.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  register() {
    if (this.registerForm.invalid) return;
    this.isLoading.set(true);

    this.authService
      .register(this.registerForm.value)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: () => {
          this.notification.showSuccess('Registration successful! Welcome.');
          this.router.navigate(['/']);
        },
      });
  }
}