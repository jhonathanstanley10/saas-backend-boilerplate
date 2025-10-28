import { Component, inject, signal } from '@angular/core';
import { RouterModule, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
// FIX: Use absolute paths from the project root (default baseUrl)
import { AuthService } from '../../core/services/auth.service';
import { AppStateService } from '../../core/services/app-state.service';

@Component({
  selector: 'app-app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink, RouterLinkActive],
  template: `
    <div class="flex h-screen bg-gray-100 font-inter">
      <!-- Overlay for mobile menu -->
      @if (isMobileMenuOpen()) {
      <div
        (click)="toggleMobileMenu()"
        class="fixed inset-0 z-20 bg-black/50 md:hidden"
      ></div>
      }

      <!-- Sidebar -->
      <nav
        class="fixed inset-y-0 left-0 z-30 flex w-64 flex-col bg-white shadow-xl transition-transform duration-300 -translate-x-full 
               md:relative md:translate-x-0 md:flex-shrink-0"
        [class.translate-x-0]="isMobileMenuOpen()"
      >
        <!-- Logo/Brand -->
        <div class="flex items-center gap-3 p-5">
          <span
            class="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-600 text-white"
          >
            <!-- Briefcase Icon -->
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="24"
              height="24"
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
          <span class="text-xl font-bold text-gray-800">SaaS App</span>
        </div>

        <!-- Navigation Links -->
        <div class="flex-1 space-y-2 px-4 py-2">
          <a
            routerLink="/"
            routerLinkActive="bg-indigo-100 text-indigo-700"
            [routerLinkActiveOptions]="{ exact: true }"
            (click)="isMobileMenuOpen.set(false)"
            class="group flex cursor-pointer items-center gap-3 rounded-lg px-4 py-3 text-gray-600 transition-all hover:bg-indigo-50 hover:text-indigo-600"
          >
            <!-- Layout Icon -->
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <rect width="18" height="18" x="3" y="3" rx="2" />
              <path d="M3 9h18" />
              <path d="M9 21V9" />
            </svg>
            <span class="font-medium">Home</span>
          </a>

          <a
            routerLink="/app/profile"
            routerLinkActive="bg-indigo-100 text-indigo-700"
            (click)="isMobileMenuOpen.set(false)"
            class="group flex cursor-pointer items-center gap-3 rounded-lg px-4 py-3 text-gray-600 transition-all hover:bg-indigo-50 hover:text-indigo-600"
          >
            <!-- User Icon -->
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            <span class="font-medium">Profile</span>
          </a>

          <a
            routerLink="/app/billing"
            routerLinkActive="bg-indigo-100 text-indigo-700"
            (click)="isMobileMenuOpen.set(false)"
            class="group flex cursor-pointer items-center gap-3 rounded-lg px-4 py-3 text-gray-600 transition-all hover:bg-indigo-50 hover:text-indigo-600"
          >
            <!-- Credit Card Icon -->
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <rect width="20" height="14" x="2" y="5" rx="2" />
              <path d="M2 10h20" />
            </svg>
            <span class="font-medium">Billing</span>
            @if(organization(); as org) {
            <span
              class="ml-auto rounded-full px-2.5 py-0.5 text-xs font-semibold"
              [ngClass]="{
                'bg-green-100 text-green-800':
                  org.subscriptionStatus === 'PREMIUM',
                'bg-gray-100 text-gray-800': org.subscriptionStatus === 'FREE'
              }"
            >
              {{ org.subscriptionStatus }}
            </span>
            }
          </a>
        </div>

        <!-- User Profile / Logout -->
        <div class="border-t border-gray-200 p-4">
          @if(user(); as user) {
          <div class="mb-3">
            <span class="block text-sm font-semibold text-gray-800"
              >{{ user.firstName }} {{ user.lastName }}</span
            >
            <span class="block truncate text-xs text-gray-500">{{
              user.email
            }}</span>
          </div>
          }
          <button
            (click)="logout()"
            class="group flex w-full items-center gap-3 rounded-lg px-4 py-3 text-gray-600 transition-all hover:bg-red-50 hover:text-red-600"
          >
            <!-- Log Out Icon -->
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" x2="9" y1="12" y2="12" />
            </svg>
            <span class="font-medium">Logout</span>
          </button>
        </div>
      </nav>

      <!-- Main Content Area -->
      <div class="flex flex-1 flex-col">
        <!-- Mobile Header -->
        <header
          class="flex items-center justify-between bg-white p-4 shadow-md md:hidden"
        >
          <!-- Logo -->
          <div class="flex items-center gap-2">
            <span
              class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-600 text-white"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
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
            <span class="text-lg font-bold text-gray-800">SaaS App</span>
          </div>
          <!-- Hamburger Button -->
          <button
            (click)="toggleMobileMenu()"
            class="rounded-lg p-2 text-gray-600 hover:bg-gray-100 hover:text-gray-900"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="3" x2="21" y1="6" y2="6" />
              <line x1="3" x2="21" y1="12" y2="12" />
              <line x1="3" x2="21" y1="18" y2="18" />
            </svg>
          </button>
        </header>

        <!-- Main Content -->
        <main class="flex-1 overflow-y-auto p-4 md:p-8">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
})
export class AppLayoutComponent {
  private authService = inject(AuthService);
  private appState = inject(AppStateService);
  user = this.appState.currentUser.asReadonly();
  organization = this.appState.currentOrganization.asReadonly();

  isMobileMenuOpen = signal(false);

  toggleMobileMenu() {
    this.isMobileMenuOpen.update((v) => !v);
  }

  logout() {
    this.isMobileMenuOpen.set(false);
    this.authService.logout();
  }
}
