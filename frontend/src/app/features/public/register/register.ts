import {Component, inject, signal} from '@angular/core';
import {Button} from 'primeng/button';
import {FloatLabel} from 'primeng/floatlabel';
import {Toast} from 'primeng/toast';
import {MessageService} from 'primeng/api';
import {Router, RouterLink} from '@angular/router';
import {AuthenticationControllerService} from '@app/api-services/services/authentication-controller.service';
import {RegisterTenantRequest} from '@app/api-services/models/register-tenant-request';
import {email, form, FormField, FormRoot, required, submit} from '@angular/forms/signals';
import {lastValueFrom} from 'rxjs';
import {Divider} from 'primeng/divider';

@Component({
  selector: 'app-register',
  imports: [
    Button,
    FloatLabel,
    Toast,
    RouterLink,
    FormRoot,
    FormField,
    Divider
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  readonly submitting = signal(false)
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


  registerForm = form(this.#registerRequest,(schemaPath) =>{
    required(schemaPath.adminFullName,{message: 'adminFullName is required'}),
    required(schemaPath.adminUserName,{message: 'adminUserName is required'}),
    required(schemaPath.adminEmail,{message: 'adminEmail is required'}),
    required(schemaPath.adminPassword,{message: 'adminPassword is required'}),
    required(schemaPath.compagnyName,{message: 'compagnyName is required'}),
    required(schemaPath.compagnyCode,{message: 'compagnyCode is required'}),
    required(schemaPath.email,{message: 'email is required'}),
    email(schemaPath.email,{message: 'email must be a valid email address'})
  })

  protected navigateToLogin() {
    this.#router.navigate(['login']);
  }


  protected async OnRegister(){
    await submit(this.registerForm, async () => {
      // 1. At this point all fields are already marked as touched
      // 2. If form is invalid - this function will NOT be called
      // 3. form().submitting() === true during execution
      const registrationData = this.registerForm().value();
      try {
       await lastValueFrom(this.#authService.register({ body: registrationData }))
        this.#messageService.add({
          severity:'success',
          summary: 'Success',
          detail: 'Registration successful'
        });
        await this.#router.navigate([''])
      } catch (e: unknown) {
        const message = e instanceof Error ? e.message : 'Registration failed. Please check your input.';
        this.#messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: message
        });
      }
    })
  }



  }
