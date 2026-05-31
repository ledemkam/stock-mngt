import {provideRouter, withComponentInputBinding} from '@angular/router';
import { routes } from './app.routes';
import {providePrimeNG} from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import {MessageService} from 'primeng/api';
import {httpInterceptorInterceptor} from '@app/core/interceptors/http-interceptor';


export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([httpInterceptorInterceptor])),
    providePrimeNG({
      theme: {
        preset: Aura
      },
    }),
    MessageService
  ]
};
