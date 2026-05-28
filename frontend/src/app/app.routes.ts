import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent:() => import('./features/public/home/home')
      .then(component => component.Home)
  },
  {
    path: 'anmeldung',
    loadComponent:() => import('./features/public/login/login')
      .then(component => component.Login)
  },
  {
    path: 'registrieren',
    loadComponent:() => import('./features/public/register/register')
      .then(component => component.Register)
  }
];
