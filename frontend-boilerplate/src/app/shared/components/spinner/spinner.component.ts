import { Component } from '@angular/core';

@Component({
  selector: 'app-spinner',
  standalone: true,
  template: `
    <div
      class="h-6 w-6 animate-spin rounded-full border-4 border-solid border-indigo-600 border-t-transparent"
    ></div>
  `,
})
export class SpinnerComponent {}