import { Routes } from '@angular/router';
import { AppComponent } from './app.component';

export const routes: Routes = [
  { path: '', component: AppComponent },
  { path: 'dashboard', component: AppComponent },
  { path: '**', redirectTo: '' }
];
