import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div
      class="flex min-h-screen items-center justify-center bg-gray-100 p-4 font-inter"
    >
      <div
        class="w-full max-w-md space-y-8 rounded-2xl bg-white p-8 shadow-xl"
      >
        <div class="flex flex-col items-center">
          <span
            class="mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-indigo-600 text-white"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="32"
              height="32"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
              <rect width="20" height="14" x="2" y="6" rx="2" />
            </svg>
          </span>
        </div>
        
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
})
export class AuthLayoutComponent {}