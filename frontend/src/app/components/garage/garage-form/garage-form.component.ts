import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {FormLayoutConfig} from '../../shared/panel-layout/data';
import {Garage} from '../../../dtos/tool';

@Component({
  selector: 'app-garage-form',
  templateUrl: './garage-form.component.html',
  styleUrl: 'garage-form.component.scss',
  standalone: false
})
export class GarageFormComponent {
  @Input() form!: FormGroup;
  @Input() formConfig!: FormLayoutConfig<Garage>;
  @Input() successMessage = '';
  @Input() errorMessage = '';

  @Output() submitForm = new EventEmitter<void>();
  @Output() addressSelected = new EventEmitter<{
    lat: string;
    lon: string;
  }>();
  @Output() fieldChanged = new EventEmitter<void>();

  onSubmit() {
    this.submitForm.emit();
  }

  onAddressSelectedInternal(addr: any) {
    this.addressSelected.emit(addr);
  }

  onChangeInternal() {
    this.fieldChanged.emit();
  }
}
