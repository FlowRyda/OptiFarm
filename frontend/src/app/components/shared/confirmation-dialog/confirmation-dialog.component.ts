import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgClass, NgIf} from "@angular/common";

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [
    NgClass,
    NgIf
  ],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.scss'
})
export class ConfirmationDialogComponent {
  @Input() title = 'Confirm';
  @Input() message = 'Are you sure?';
  @Input() confirmLabel = 'OK';
  @Input() cancelLabel = 'Cancel';
  @Input() danger = false;
  @Input() width: string = '40rem';

  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  onBackdropClick() {
    this.cancelled.emit();
  }

  onCancelClick(event: MouseEvent) {
    event.stopPropagation();
    this.cancelled.emit();
  }

  onConfirmClick(event: MouseEvent) {
    event.stopPropagation();
    this.confirmed.emit();
  }

  stopPropagation(event: MouseEvent) {
    event.stopPropagation();
  }
}
