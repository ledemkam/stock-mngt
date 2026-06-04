import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {Button} from 'primeng/button';
import {FormField, FormRoot} from '@angular/forms/signals';

@Component({
  selector: 'app-home',
  imports: [
    Button,
    FormRoot,
    FormField,
    RouterLink
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  readonly  #router = inject(Router)


  navigateToLogin(){
    this.#router.navigate(['anmeldung']);
  }

  navigateToRegister(){
    this.#router.navigate(['registrieren']);
  }
}
