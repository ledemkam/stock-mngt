import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {Router} from '@angular/router';
import {Button} from 'primeng/button';

@Component({
  selector: 'app-home',
  imports: [
    Button
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
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
