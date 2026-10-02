import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-info-field',
  templateUrl: './info-field.component.html',
  styleUrl: './info-field.component.scss'
})
export class InfoFieldComponent {
  @Input() label!: string;
  @Input() value!: string | number | null;
}
