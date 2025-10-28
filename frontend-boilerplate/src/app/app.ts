import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AlertComponent } from './shared/components/alert/alert.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule, AlertComponent],
  template: `
    <router-outlet></router-outlet>
    <app-alert></app-alert>
  `,
})
export class AppComponent {}