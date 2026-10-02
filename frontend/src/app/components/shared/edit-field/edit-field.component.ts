import {
  AfterContentInit,
  Component,
  ContentChild,
  ElementRef,
  EventEmitter,
  forwardRef,
  Input,
  Output
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";

@Component({
  selector: 'app-edit-field',
  imports: [CommonModule, MatTooltip],
  templateUrl: './edit-field.component.html',
  styleUrl: './edit-field.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => EditFieldComponent),
      multi: true
    },
  ]
})
export class EditFieldComponent implements ControlValueAccessor, AfterContentInit {
  @Input() label = '';
  @Input() labelTooltip?: string;
  @Input() required = false;
  @Input() type: 'text' | 'number' | 'date' | 'datetime-local' | 'password' = 'text';
  @Input() placeholder = '';
  @Input() disabled: boolean = false;

  @Output() blurred = new EventEmitter<void>();

  value: any = '';
  hasContent = false;

  @ContentChild('customInput') customInput?: ElementRef;

  private onChange = (value: any) => {
  };
  private onTouched = () => {
  };

  ngAfterContentInit() {
    this.hasContent = !!this.customInput;
    if (this.customInput) {
      const element = this.customInput.nativeElement;
      element.value = this.value;

      element.addEventListener('change', (event: Event) => {
        const target = event.target as HTMLInputElement | HTMLSelectElement;
        this.value = target.value;
        this.onChange(this.value);
      });

      element.addEventListener('blur', () => {
        this.onTouched();
      });
    }
  }

  onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    let newValue: any = target.value;

    if (this.type === 'number' && newValue !== '') {
      newValue = parseFloat(newValue);
    }

    this.value = newValue;
    this.onChange(newValue);
  }

  onBlur(): void {
    this.onTouched();
  }

  writeValue(value: any): void {
    this.value = value || '';
    if (this.customInput) {
      this.customInput.nativeElement.value = this.value;
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean) {
    this.disabled = isDisabled;
    if (this.customInput) {
      this.customInput.nativeElement.disabled = isDisabled;
    }
  }
}
