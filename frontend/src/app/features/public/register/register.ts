import {Component} from '@angular/core';
import {Button} from 'primeng/button';
import {FloatLabel} from 'primeng/floatlabel';
import {ReactiveFormsModule} from '@angular/forms';
import {Toast} from 'primeng/toast';

@Component({
  selector: 'app-register',
  imports: [
    Button,
    FloatLabel,
    ReactiveFormsModule,
    Toast
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {}
