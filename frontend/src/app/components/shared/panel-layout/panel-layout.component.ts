import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActionButton, BackAction} from './data';

@Component({
  selector: 'app-panel-layout',
  imports: [CommonModule],
  templateUrl: './panel-layout.component.html',
  styleUrl: './panel-layout.component.scss',
})
export class PanelLayoutComponent {
  @Input() title: string = '';
  @Input() subtitle: string = '';

  @Input() actionButtons: ActionButton[] = [];

  @Input() backAction?: BackAction;
}
