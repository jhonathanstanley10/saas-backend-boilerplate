import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="flex min-h-screen flex-col bg-gray-50 font-inter">
      <!-- Header -->
      <header class="sticky top-0 z-10 bg-white shadow-sm">
        <nav
          class="container mx-auto flex items-center justify-between px-6 py-4"
        >
          <div class="flex items-center gap-3">
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
          <div class="space-x-4">
            <a
              routerLink="/auth/login"
              class="rounded-lg px-5 py-2.5 text-sm font-medium text-gray-700 transition hover:bg-gray-100"
              >Sign In</a
            >
            <a
              routerLink="/auth/register"
              class="rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
              >Sign Up</a
            >
          </div>
        </nav>
      </header>

      <!-- Hero Section -->
      <main class="flex flex-1 items-center justify-center py-16 md:py-24">
        <div class="container mx-auto px-6 text-center">
          <h1
            class="mb-6 text-4xl font-extrabold tracking-tight text-gray-900 md:text-5xl lg:text-6xl"
          >
            The <span class="text-indigo-600">Ultimate Solution</span> for Your
            Business
          </h1>
          <p class="mx-auto mb-10 max-w-2xl text-lg text-gray-600 md:text-xl">
            Streamline your workflow, boost productivity, and achieve your
            goals with our intuitive SaaS platform. Get started today!
          </p>
          <div class="flex flex-col justify-center gap-4 sm:flex-row">
            <a
              routerLink="/auth/register"
              class="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-8 py-3.5 text-base font-medium text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
              >Get Started for Free</a
            >
            <a
              routerLink="/auth/login"
              class="inline-flex items-center justify-center rounded-lg border border-gray-300 bg-white px-8 py-3.5 text-base font-medium text-gray-700 shadow-sm transition hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
              >Sign In to Your Account</a
            >
          </div>
        </div>
      </main>

      <!-- Footer -->
      <footer class="bg-white py-6">
        <div class="container mx-auto px-6 text-center text-sm text-gray-500">
          &copy; {{ currentYear }} SaaS App. All rights reserved. Built with
          Angular & Tailwind CSS.
        </div>
      </footer>
    </div>
  `,
})
export class LandingPageComponent {
  currentYear = new Date().getFullYear();
}
