import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {providePrimeNG} from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import {provideHttpClient} from '@angular/common/http';
import {ApplicationConfig, provideZoneChangeDetection} from '@angular/core';


export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection(
      { eventCoalescing: true })
    ,
    provideRouter(routes),
    provideHttpClient(),
    providePrimeNG({
      theme: {
        preset: Aura
      },
    }),

  ]
};
