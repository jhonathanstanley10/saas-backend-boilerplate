import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (alert(); as alert) {
    <div
      class="fixed top-5 right-5 z-50 rounded-lg px-5 py-3 text-white shadow-lg"
      [ngClass]="{
        'bg-red-600': alert.type === 'error',
        'bg-green-600': alert.type === 'success',
        'bg-blue-600': alert.type === 'info'
      }"
    >
      <span class="font-semibold">{{ alert.message }}</span>
    </div>
    }
  `,
})
export class AlertComponent {
  private notification = inject(NotificationService);
  alert = this.notification.alert.asReadonly();
}