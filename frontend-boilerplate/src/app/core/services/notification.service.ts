import { Injectable, signal } from '@angular/core';

export type AlertType = 'success' | 'error' | 'info';
export interface Alert {
  type: AlertType;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  alert = signal<Alert | null>(null);
  private timer: any;

  showAlert(message: string, type: AlertType = 'info', duration = 3000) {
    if (this.timer) {
      clearTimeout(this.timer);
    }
    this.alert.set({ message, type });
    this.timer = setTimeout(() => this.alert.set(null), duration);
  }

  showSuccess(message: string, duration = 3000) {
    this.showAlert(message, 'success', duration);
  }

  showError(message: string, duration = 3000) {
    this.showAlert(message, 'error', duration);
  }
}