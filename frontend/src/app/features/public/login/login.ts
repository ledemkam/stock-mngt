import {Component, inject, signal} from '@angular/core';
import {FloatLabel} from 'primeng/floatlabel';
import {Button} from 'primeng/button';
import {Router} from '@angular/router';
import {AuthenticationControllerService} from '@app/api-services/services/authentication-controller.service';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MessageService} from 'primeng/api';
import {Toast} from 'primeng/toast';

@Component({
  selector: 'app-login',
  imports: [
    FloatLabel,
    Button,
    ReactiveFormsModule,
    Toast
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  readonly submitting = signal(false)
  readonly #messageService = inject(MessageService)
  readonly #router = inject(Router)
  readonly #authService = inject(AuthenticationControllerService)

  form = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4)],
    }),
  })

  submit() {
    this.form.markAllAsTouched()
    console.log(this.form.value)
    if (this.form.invalid) {
      this.#messageService.add({
        severity: 'warn',
        summary: 'Pflichtfelder',
        detail: 'Bitte Username und Passwort eingeben.',
      })
      return
    }

    const body = this.form.getRawValue()
    this.submitting.set(true)
    this.#authService.login({ body }).subscribe({
      next: () => {
        this.submitting.set(false)
        void this.#router.navigate([''])
      },
      error: () => {
        this.submitting.set(false)
        this.#messageService.add({
          severity: 'error',
          summary: 'Fehler',
          detail: 'Keine mögliche anmeldung, bitte wieder sich einloggen',
        })
      },
    })
  }

  navigateToRegister() {
    void this.#router.navigate(['registrieren'])
  }
}
