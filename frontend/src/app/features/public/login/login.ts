import {Component, inject, signal} from '@angular/core';
import {FloatLabel} from 'primeng/floatlabel';
import {Button} from 'primeng/button';
import {Router, RouterLink} from '@angular/router';
import {AuthenticationControllerService} from '@app/api-services/services/authentication-controller.service';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MessageService} from 'primeng/api';
import {Toast} from 'primeng/toast';
import {TokenService} from '@app/core/token/token-service';
import {LoginRequest} from '@app/api-services/models/login-request';
import {form, FormField, FormRoot, required} from '@angular/forms/signals';
import {lastValueFrom} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {ErrorResponse} from '@app/shared/models/error-response';
import {ValidationError} from '@app/shared/models/validation-error';

@Component({
  selector: 'app-login',
  imports: [
    FloatLabel,
    Button,
    ReactiveFormsModule,
    Toast,
    FormRoot,
    FormField,
    RouterLink
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  readonly formSubmitted = signal(false)
  readonly validationErrors =  signal<ValidationError[]>([]);
  readonly #loginRequest = signal<LoginRequest>({
    username: '',
    password: ''
  })

  readonly #messageService = inject(MessageService)
  readonly #router = inject(Router)
  readonly #authService = inject(AuthenticationControllerService)
  readonly  #tokenService = inject(TokenService);

  protected hasError(fieldName: string): boolean {
    return this.validationErrors().some((e) => e.field === fieldName);
  }

  protected getErrorMsg(fieldName: string): string {
    return this.validationErrors().find((e) => e.field === fieldName)?.message || '';
  }

  loginForm = form(this.#loginRequest,(schemaPah) => {
     required(schemaPah.username, {message: 'Username is required'});
     required(schemaPah.password, {message: 'Password is required'});

  }, {
    submission: {
      action: async () => {
        try {
          const loginResponse = await lastValueFrom(this.#authService.login({body: this.#loginRequest()}));
          this.#tokenService.saveToken(loginResponse);
          this.#messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Login successful'
          });
          void this.navigateUser();
        } catch (e: unknown) {
          const httpError = e as HttpErrorResponse;
          const error = JSON.parse(httpError.error as string) as ErrorResponse;
          this.validationErrors.set(error.validationErrors || []);
          this.#messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Login failed. Please check your input.'
          });
        }
      },
      onInvalid: () => {
        this.#messageService.add({
          severity: 'warn',
          summary: 'Validation',
          detail: 'Please fill in all required fields.'
        });
      }
    }
  })

  navigateToRegister() {
    void this.#router.navigate(['registrieren'])
  }

  private async navigateUser(){
    if (this.#tokenService.isPlatformAdmin){
      await this.#router.navigate(['administration']);
    }else if (this.#tokenService.isCompagnyAdmin ||
              this.#tokenService.isAdministrator ||
              this.#tokenService.isSalesOperator ||
              this.#tokenService.isUser){
      await this.#router.navigate(['app']);
    }
  }


}
