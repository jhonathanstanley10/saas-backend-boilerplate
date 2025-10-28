import { Component, inject, signal, OnInit } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AppStateService } from '../../core/services/app-state.service';
import { ProfileService } from './profile.service';
import { finalize } from 'rxjs/operators';
import { SpinnerComponent } from '../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SpinnerComponent],
  template: `
    <h1 class="mb-6 text-3xl font-bold text-gray-800">My Profile</h1>
    <div class="max-w-lg">
      <form
        [formGroup]="profileForm"
        (ngSubmit)="updateProfile()"
        class="space-y-6 rounded-xl bg-white p-8 shadow-lg"
      >
        <div>
          <label
            for="email"
            class="block text-sm font-medium text-gray-700"
            >Email</label
          >
          <input
            type="email"
            id="email"
            formControlName="email"
            class="mt-1 block w-full cursor-not-allowed rounded-lg border-gray-300 bg-gray-100 px-4 py-2 shadow-sm"
          />
        </div>

        <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
          <div>
            <label
              for="firstName"
              class="block text-sm font-medium text-gray-700"
              >First Name</label
            >
            <input
              type="text"
              id="firstName"
              formControlName="firstName"
              class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label
              for="lastName"
              class="block text-sm font-medium text-gray-700"
              >Last Name</label
            >
            <input
              type="text"
              id="lastName"
              formControlName="lastName"
              class="mt-1 block w-full rounded-lg border-gray-300 px-4 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        <div class="text-right">
          <button
            type="submit"
            [disabled]="profileForm.invalid || profileForm.pristine || isLoading()"
            class="flex w-36 items-center justify-center rounded-lg bg-indigo-600 px-6 py-2.5 font-medium text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            @if (isLoading()) {
            <app-spinner />
            } @else {
            Save Changes
            }
          </button>
        </div>
      </form>
    </div>
  `,
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private appState = inject(AppStateService);
  private profileService = inject(ProfileService);
  isLoading = signal(false);

  profileForm = this.fb.group({
    email: [{ value: '', disabled: true }],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
  });

  ngOnInit() {
    this.profileForm.patchValue(this.appState.currentUser()!);
  }

  updateProfile() {
    if (this.profileForm.invalid || this.profileForm.pristine) return;
    this.isLoading.set(true);

    // FIX: Use .value and create a specific payload object
    // This avoids passing the disabled 'email' field and fixes the type error.
    const { firstName, lastName } = this.profileForm.value;

    // Type guard (form validation should already cover this)
    if (!firstName || !lastName) {
      this.isLoading.set(false);
      return;
    }

    const payload = { firstName, lastName };

    this.profileService
      .updateProfile(payload) // Pass the correctly typed payload
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe(() => {
        this.profileForm.markAsPristine();
      });
  }
}
