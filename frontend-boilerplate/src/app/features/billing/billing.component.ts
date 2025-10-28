import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BillingService } from './billing.service';
import { finalize } from 'rxjs/operators';
import { SpinnerComponent } from '../../shared/components/spinner/spinner.component';
import { ActivatedRoute } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';
import { AppStateService } from '../../core/services/app-state.service';

@Component({
  selector: 'app-billing',
  standalone: true,
  imports: [CommonModule, SpinnerComponent],
  template: `
    <h1 class="mb-6 text-3xl font-bold text-gray-800">Billing</h1>
    <div class="grid max-w-4xl grid-cols-1 gap-6 md:grid-cols-2">
      <div class="rounded-xl bg-white p-8 shadow-lg">
        <h2 class="mb-4 text-2xl font-semibold text-gray-800">
          Subscription Plan
        </h2>

        @if (organization(); as org) {
          @if (org.subscriptionStatus === 'PREMIUM') {
            <p class="mb-6 text-gray-600">
              You are currently on the <strong>Premium Plan</strong>.
            </p>
            <div
              class="rounded-lg border border-green-200 bg-green-50 p-4 text-center text-green-700"
            >
              <span class="font-medium">Your subscription is active.</span>
            </div>
          } @else {
            <p class="mb-6 text-gray-600">
              You are currently on the <strong>Free Plan</strong>. Upgrade to
              unlock all features.
            </p>
            <button
              (click)="goToCheckout()"
              [disabled]="isLoading()"
              class="flex w-full items-center justify-center rounded-lg bg-indigo-600 px-6 py-3 font-semibold text-white shadow-md transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            >
              @if (isLoading() && action() === 'checkout') {
                <app-spinner />
              } @else {
                Upgrade to Premium
              }
            </button>
          }
        }
      </div>

      @if (organization(); as org) {
        @if (org.subscriptionStatus === 'PREMIUM' && org.stripeCustomerId) {
          <div class="rounded-xl bg-white p-8 shadow-lg">
            <h2 class="mb-4 text-2xl font-semibold text-gray-800">
              Manage Subscription
            </h2>
            <p class="mb-6 text-gray-600">
              Manage your billing information, invoices, and subscription plan via
              our secure Stripe portal.
            </p>
            <button
              (click)="goToPortal()"
              [disabled]="isLoading()"
              class="flex w-full items-center justify-center rounded-lg bg-gray-700 px-6 py-3 font-semibold text-white shadow-md transition hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 disabled:opacity-50"
            >
              @if (isLoading() && action() === 'portal') {
                <app-spinner />
              } @else {
                Manage Billing
              }
            </button>
          </div>
        }
      }
    </div>
  `,
})
export class BillingComponent implements OnInit {
  private billingService = inject(BillingService);
  private appState = inject(AppStateService);
  private route = inject(ActivatedRoute);
  private notification = inject(NotificationService);

  isLoading = signal(false);
  action = signal<'checkout' | 'portal' | null>(null);

  // This computed signal is now for reading state, not for UI logic
  organization = this.appState.currentOrganization.asReadonly();

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      if (params['payment_success']) {
        this.notification.showSuccess('Payment successful! Welcome to Premium.');
        // Refresh all app data to get new org status
        this.appState.loadData().subscribe();
      }
    });
  }

  goToCheckout() {
    this.isLoading.set(true);
    this.action.set('checkout');
    this.billingService
      .createCheckoutSession()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe((response) => {
        window.location.href = response.url;
      });
  }

  goToPortal() {
    this.isLoading.set(true);
    this.action.set('portal');
    this.billingService
      .createPortalSession()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe((response) => {
        window.location.href = response.url;
      });
  }
}