import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {MapComponent} from "../map/map.component";

@Component({
  selector: 'app-map-panel-layout',
  templateUrl: './map-panel-layout.component.html',
  standalone: false,
  styleUrl: './map-panel-layout.component.scss'
})
export class MapPanelLayoutComponent {
  @Input() enableUserLocation = false;

  @Output() markerDragged = new EventEmitter<{ lat: number; lon: number }>();
  @Output() mapClick = new EventEmitter<{ lat: number; lon: number }>();
  @Output() mapReady = new EventEmitter<MapComponent>();

  @ViewChild(MapComponent)
  mapComponent!: MapComponent;


  ngAfterViewInit(): void {
    if (this.mapComponent) {
      this.mapReady.emit(this.mapComponent);
    }
  }
}
