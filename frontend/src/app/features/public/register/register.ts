import {Component, inject, signal} from '@angular/core';
import {Button} from 'primeng/button';
import {FloatLabel} from 'primeng/floatlabel';
import {InputText} from 'primeng/inputtext';
import {Toast} from 'primeng/toast';
import {MessageService} from 'primeng/api';
import {Router, RouterLink} from '@angular/router';
import {AuthenticationControllerService} from '@app/api-services/services/authentication-controller.service';
import {RegisterTenantRequest} from '@app/api-services/models/register-tenant-request';
import {email, form, FormField, FormRoot, required, submit} from '@angular/forms/signals';
import {lastValueFrom} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {Divider} from 'primeng/divider';
import {ValidationError} from '@app/shared/models/validation-error';
import {ErrorResponse} from '@app/shared/models/error-response';

@Component({
  selector: 'app-register',
  imports: [
    Button,
    FloatLabel,
    InputText,
    Toast,
    RouterLink,
    FormRoot,
    FormField,
    Divider,
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  readonly formSubmitted = signal(false)
  readonly validationErrors =  signal<ValidationError[]>([]);
  readonly #messageService = inject(MessageService)
  readonly #router = inject(Router)
  readonly #authService = inject(AuthenticationControllerService)

  readonly #registerRequest = signal<RegisterTenantRequest>({
    adminFullName: '',
    adminPassword: '',
    adminUserName: '',
    adminEmail: '',
    compagnyCode: '',
    compagnyName: '',
    email: '',
  })

  protected hasError(fieldName: string): boolean {
    return this.validationErrors().some((e) => e.field === fieldName);
  }

  protected getErrorMsg(fieldName: string): string {
    return this.validationErrors().find((e) => e.field === fieldName)?.message || '';
  }

  registerForm = form(this.#registerRequest, (schemaPath) => {
    required(schemaPath.adminFullName, {message: 'adminFullName is required'});
    required(schemaPath.adminUserName, {message: 'adminUserName is required'});
    required(schemaPath.adminEmail, {message: 'adminEmail is required'});
    required(schemaPath.adminPassword, {message: 'adminPassword is required'});
    required(schemaPath.compagnyName, {message: 'compagnyName is required'});
    required(schemaPath.compagnyCode, {message: 'compagnyCode is required'});
    required(schemaPath.email, {message: 'email is required'});
    email(schemaPath.email, {message: 'email must be a valid email address'});
  }, {
    submission: {
      action: async () => {
        try {
          await lastValueFrom(this.#authService.register({body: this.#registerRequest()}));
          console.log(this.#registerRequest());
          this.#messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Registration successful'
          });
          await this.#router.navigate(['']);
        } catch (e: unknown) {
          const httpError = e as HttpErrorResponse;
          const error = JSON.parse(httpError.error as string) as ErrorResponse;
          this.validationErrors.set(error.validationErrors || []);
          this.#messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Registration failed. Please check your input.'
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

  onSubmit() {
    this.formSubmitted.set(true);
    submit(this.registerForm);
  }

  navigateToLogin() {
    this.#router.navigate(['anmeldung']);
  }
}
