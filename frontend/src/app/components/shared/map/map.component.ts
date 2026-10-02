import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, Output, SimpleChanges} from '@angular/core';
import * as L from 'leaflet';
import {SimpleFieldDto} from "../../../dtos/field";
import {FieldService} from "../../../services/field.service";
import {Subject, takeUntil} from "rxjs";
import {DrawingService} from "../../../services/drawing.service";
import {isMarkerDto, isPolygonDto, LocatedObjectDto} from "../../../dtos/locator/locatedObjectDto";
import {MarkerDto} from 'src/app/dtos/locator/markerDto';
import {PolygonDto} from "../../../dtos/locator/polygonDto";
import {MapColors} from "../../../constants/map-colors";

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss',
  standalone: false
})
export class MapComponent implements AfterViewInit, OnDestroy {
  @Input() fields: SimpleFieldDto[] = [];
  @Input() initialZoom = 13;
  @Input() center: [number, number] = [51.505, -0.09];
  @Input() enableUserLocation = true;
  @Input() drawingEnabled = false;
  @Input() loadFields = true;
  @Input() excludeFieldId?: number;

  @Output() fieldClicked = new EventEmitter<number>();
  @Output() mapClick = new EventEmitter<{ lat: number; lon: number }>();
  @Output() markerDragged = new EventEmitter<{ lat: number; lon: number }>();
  @Output() mapReady = new EventEmitter<MapComponent>();
  @Output() polygonCreated = new EventEmitter<L.LatLng[]>();
  @Output() boundaryCleared = new EventEmitter<void>();

  private map?: L.Map;
  private markers: L.Marker[] = [];
  private isPolygonClosed = false;
  private lines: L.Polyline[] = [];
  private clickHandler?: (e: L.LeafletMouseEvent) => void;
  private fieldLayerMap = new Map<number, L.Polygon>();
  private previewPolygon?: L.Polygon;
  private destroy$ = new Subject<void>();
  private featuresLayer = L.layerGroup();
  private emptyFieldIcons: L.Marker[] = [];
  private isExclusionMode = false;

  constructor(
    private readonly fieldService: FieldService,
    private readonly drawingService: DrawingService
  ) {
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
      this.subscribeToDrawingState();
      this.mapReady.emit(this);

      if (this.drawingEnabled) {
        this.toggleDrawingMode(true);
      }
    }, 0);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    this.emptyFieldIcons.forEach(icon => this.map?.removeLayer(icon));
    this.emptyFieldIcons = [];

    if (this.map) {
      this.map.off();
      this.map.remove();
      this.map = undefined;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.map) return;

    if (changes['drawingEnabled']) {
      this.toggleDrawingMode(changes['drawingEnabled'].currentValue);
    }
  }

  private initMap(): void {
    const satelliteLayer = L.tileLayer(
      'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
      {attribution: 'Tiles &copy; Esri &mdash; Source: Esri, Maxar, Earthstar Geographics, and the GIS User Community'}
    );

    const streetsLayer = L.tileLayer(
      'https://tiles.stadiamaps.com/tiles/alidade_smooth/{z}/{x}/{y}{r}.png',
      {attribution: '&copy; <a href="https://www.stadiamaps.com/" target="_blank">Stadia Maps</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'}
    );

    if (this.map === undefined) {
      this.map = L.map('map', {
        center: this.center,
        zoom: this.initialZoom,
        zoomControl: false,
        layers: [streetsLayer, satelliteLayer]
      });
    }

    const baseMaps = {
      "Streets": streetsLayer,
      "Satellite": satelliteLayer
    };

    L.control.layers(baseMaps, {}, {position: 'topleft'}).addTo(this.map);
    L.control.zoom({position: 'bottomleft'}).addTo(this.map);

    this.featuresLayer.addTo(this.map);

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      if (!this.drawingEnabled) {
        this.mapClick.emit({lat: e.latlng.lat, lon: e.latlng.lng});
      }
    });
  }

  private subscribeToDrawingState(): void {
    this.drawingService.isClosed$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isClosed => {
        this.isPolygonClosed = isClosed;
        if (isClosed) {
          this.onPolygonFinalized();
        }
        this.updatePolygon();
      });
  }


  private toggleDrawingMode(enabled: boolean): void {
    if (!this.map) return;

    if (enabled) {
      this.enableClickToMark();
    } else {
      this.disableClickToMark();
    }
  }

  private enableClickToMark(): void {
    if (!this.map) return;

    const container = this.map.getContainer();
    container.classList.add('drawing-mode');

    this.clickHandler = (e: L.LeafletMouseEvent) => {
      const originalEvent = (e as any).originalEvent;
      if (originalEvent && originalEvent.defaultPrevented) {
        return;
      }
      this.addMarkerAtPosition(e.latlng);
    };

    this.map.on('click', this.clickHandler);
  }

  private disableClickToMark(): void {
    if (!this.map) return;

    const container = this.map.getContainer();
    container.classList.remove('drawing-mode');

    this.map.off('mousedown mousemove mouseup');

    if (this.clickHandler) {
      this.map.off('click', this.clickHandler);
      this.clickHandler = undefined;
    }
  }

  public initializeWithCoordinates(coords: L.LatLng[]): void {
    this.clearBoundary();

    coords.forEach((latlng, index) => {
      const marker = this.createLeafletMarker(latlng, index);
      this.markers.push(marker);
    });

    this.updatePolygon();
    this.syncToService();
    this.drawingService.finalizeBoundary();
  }

  clearBoundary(): void {
    this.markers.forEach(marker => this.map?.removeLayer(marker));
    this.markers = [];

    this.lines.forEach(line => this.map?.removeLayer(line));
    this.lines = [];

    if (this.previewPolygon) {
      this.map?.removeLayer(this.previewPolygon);
      this.previewPolygon = undefined;
    }

    this.isPolygonClosed = false;
    this.drawingService.clearBoundary();
    this.boundaryCleared.emit();
  }

  public setExclusionMode(enabled: boolean): void {
    this.isExclusionMode = enabled;
    this.updateMarkerIcons();
    this.updatePolygon();
  }


  private addMarkerAtPosition(latlng: L.LatLng, insertIndex?: number): void {
    const marker = this.createLeafletMarker(latlng, this.markers.length);

    if (insertIndex !== undefined && insertIndex < this.markers.length) {
      this.markers.splice(insertIndex, 0, marker);
    } else {
      this.markers.push(marker);
    }

    this.updateMarkerIcons();
    this.updatePolygon();
    this.syncToService();
  }

  private removeMarkerAtIndex(index: number): void {
    if (index >= 0 && index < this.markers.length) {
      const marker = this.markers[index];
      this.map?.removeLayer(marker);
      this.markers.splice(index, 1);

      this.updateMarkerIcons();
      this.updatePolygon();
      this.syncToService();
    }
  }

  private createLeafletMarker(latlng: L.LatLng, index: number): L.Marker {
    const marker = L.marker(latlng, {
      icon: this.createMarkerIcon(index, index === 0, this.isExclusionMode),
      draggable: true
    }).addTo(this.map!);

    marker.on('drag', () => this.updatePolygon());
    marker.on('dragend', () => this.syncToService());

    marker.on('contextmenu', (e: L.LeafletMouseEvent) => {
      L.DomEvent.stopPropagation(e);
      const markerIndex = this.markers.indexOf(marker);
      if (markerIndex !== -1) {
        this.removeMarkerAtIndex(markerIndex);
      }
    });

    return marker;
  }

  private createMarkerIcon(index: number, isFirst: boolean = false, isExclusion: boolean = false): L.DivIcon {
    const size = 28;
    const badgeSize = 16;

    const color = isExclusion
      ? (isFirst ? MapColors.EXCLUSION_PRIMARY : MapColors.EXCLUSION_SECONDARY)
      : (isFirst ? MapColors.OUTER_BOUNDARY_FIRST_MARKER : MapColors.OUTER_BOUNDARY_MARKER);

    const badgeStyle = `border: 2px solid ${color}; width: ${badgeSize}px; height: ${badgeSize}px; color: ${color}`;

    return L.divIcon({
      className: isExclusion ? 'field-marker exclusion-marker' : 'field-marker',
      html: `
        <div class="marker-wrapper" style="width: ${size}px; height: ${size}px;">
          <svg width="${size}" height="${size}" viewBox="0 0 28 28">
            <circle cx="14" cy="14" r="12" fill="${color}" stroke="white" stroke-width="2.5"/>
            <circle cx="14" cy="14" r="5" fill="white"/>
          </svg>
          <div class="marker-badge" style="${badgeStyle}">
            ${isExclusion && isFirst ? '✕' : index + 1}
          </div>
        </div>
      `,
      iconSize: [size + 8, size + 8],
      iconAnchor: [(size + 8) / 2, (size + 8) / 2]
    });
  }

  private updateMarkerIcons(): void {
    this.markers.forEach((marker, index) => {
      marker.setIcon(this.createMarkerIcon(index, index === 0, this.isExclusionMode));
    });
  }


  private updatePolygon(): void {
    if (!this.map) return;

    this.lines.forEach(line => this.map!.removeLayer(line));
    this.lines = [];

    const latlngs = this.markers.map(m => m.getLatLng());

    if (latlngs.length >= 2) {
      for (let i = 0; i < latlngs.length - 1; i++) {
        const line = this.createClickableLine([latlngs[i], latlngs[i + 1]], i);
        this.lines.push(line);
      }

      if (latlngs.length >= 3) {
        const closingLine = this.createClickableLine(
          [latlngs[latlngs.length - 1], latlngs[0]],
          latlngs.length - 1
        );
        this.lines.push(closingLine);
      }
    }

    this.updatePreviewPolygon(latlngs);
  }

  private updatePreviewPolygon(latlngs: L.LatLng[]): void {
    if (!this.map) return;

    if (this.previewPolygon) {
      this.map.removeLayer(this.previewPolygon);
      this.previewPolygon = undefined;
    }

    if (this.drawingEnabled && latlngs.length >= 3 && !this.isPolygonClosed) {
      const polygonColor = this.isExclusionMode ? MapColors.EXCLUSION_PRIMARY : MapColors.OUTER_BOUNDARY_MARKER;

      this.previewPolygon = L.polygon(latlngs, {
        color: polygonColor,
        weight: 2,
        dashArray: '5, 10',
        fillColor: polygonColor,
        fillOpacity: 0.1,
        interactive: false
      }).addTo(this.map);
    }
  }

  private createClickableLine(latlngs: L.LatLng[], segmentIndex: number): L.Polyline {
    const baseColor = this.isExclusionMode ? MapColors.EXCLUSION_PRIMARY : MapColors.OUTER_BOUNDARY_MARKER;
    const hoverColor = this.isExclusionMode ? MapColors.EXCLUSION_HOVER : MapColors.OUTER_BOUNDARY_FIRST_MARKER;

    const line = L.polyline(latlngs, {
      color: baseColor,
      weight: 6,
      opacity: 0.6,
      className: 'clickable-line'
    }).addTo(this.map!);

    line.on('mouseover', () => {
      line.setStyle({color: hoverColor, weight: 8, opacity: 0.8});
      this.map!.getContainer().style.cursor = 'pointer';
    });

    line.on('mouseout', () => {
      line.setStyle({color: baseColor, weight: 6, opacity: 0.6});
      this.map!.getContainer().style.cursor = 'crosshair';
    });

    line.on('click', (e: L.LeafletMouseEvent) => {
      L.DomEvent.stopPropagation(e);
      this.addMarkerAtPosition(e.latlng, segmentIndex + 1);
    });

    return line;
  }

  private syncToService(): void {
    const coords = this.markers.map(m => m.getLatLng());
    this.drawingService.setCoordinates(coords);
  }

  private onPolygonFinalized(): void {
    if (this.map) {
      this.map.getContainer().style.cursor = '';
    }
    const coordinates = this.markers.map(m => m.getLatLng());
    this.polygonCreated.emit(coordinates);
  }

  public drawLocatedObjects(objects: LocatedObjectDto[], focusFieldId?: number): void {
    this.featuresLayer.clearLayers();
    this.fieldLayerMap.clear();

    this.emptyFieldIcons.forEach(icon => this.map?.removeLayer(icon));
    this.emptyFieldIcons = [];

    const bounds = L.latLngBounds([]);
    const isInteractive = !this.drawingEnabled;

    objects.forEach((obj) => {
      if (isMarkerDto(obj)) {
        if (focusFieldId !== undefined) return;

        const marker = this.createMarkerFromDto(obj, isInteractive);
        if (marker) {
          marker.addTo(this.featuresLayer);
          bounds.extend(marker.getLatLng());
        }
      } else if (isPolygonDto(obj)) {
        const isFocused = focusFieldId !== undefined && obj.fieldId === focusFieldId;
        const polygon = this.createPolygonFromDto(obj, isInteractive, isFocused);

        if (polygon) {
          polygon.addTo(this.featuresLayer);

          if (!obj.currentSeed) {
            const iconMarker = this.createEmptyFieldIcon(obj.center);
            iconMarker.addTo(this.map!);
            this.emptyFieldIcons.push(iconMarker);
          }

          if (obj.fieldId) {
            this.fieldLayerMap.set(obj.fieldId, polygon);
          }

          this.extendBoundsFromLatLngs(bounds, polygon.getLatLngs());
        }
      }
    });

    if (focusFieldId !== undefined) {
      this.zoomToField(focusFieldId);
    } else if (bounds.isValid()) {
      this.map?.fitBounds(bounds, {padding: [20, 20]});
    }
  }

  private createMarkerFromDto(markerDto: MarkerDto, interactive: boolean = true): L.Marker | null {
    if (!markerDto.points || markerDto.points.length !== 1) {
      return null;
    }

    const {lat, lon} = markerDto.points[0];

    const icon = markerDto.iconUrl
      ? L.icon({
        iconUrl: markerDto.iconUrl,
        iconSize: [markerDto.iconWidth, markerDto.iconHeight],
        iconAnchor: [markerDto.iconWidth / 2, markerDto.iconHeight],
      })
      : undefined;

    let markerToolTip = markerDto.title;
    if (markerDto.fuelLevel < 25 && markerDto.fuelLevel >= 0) {
      markerToolTip += ' needs fuel!';
    }

    const detailUrl = (markerDto.garage ? 'garage/details/' : 'equipment/detail/') + markerDto.markerId;

    const marker = L.marker([lat, lon], {
      ...(icon ? {icon} : {}),
      interactive: interactive,
    })
      .bindTooltip(markerToolTip, {
        permanent: false,
        direction: 'bottom'
      })
      .bindPopup(`
      <div class="field-popup">
        <div class="field-info">
      <span class="field-name">${markerDto.title || 'No Name'}</span>
    </div>
        <a href="#/${detailUrl}" class="field-details-link">
      View Details →
    </a>
      </div>
    `, {
        offset: L.point(0, -25)
      });
    if (markerDto.isUsable === false) {
      marker.setOpacity(0.4);
    }

    if (interactive) {
      marker.on('click', () => {
        this.map?.setView([lat, lon], 18);
      });
    }

    return marker;
  }

  private createPolygonFromDto(
    polyDto: PolygonDto,
    interactive: boolean = true,
    isFocused: boolean = false
  ): L.Polygon | null {
    if (!polyDto.points || polyDto.points.length < 3) {
      return null;
    }

    const latLngs: L.LatLngExpression[] = polyDto.points.map(p => [p.lat, p.lon]);

    let polygonCoords: L.LatLngExpression[] | L.LatLngExpression[][] = latLngs;
    if (polyDto.exclusions && polyDto.exclusions.length > 0) {
      const holes = polyDto.exclusions.map(exclusion =>
        exclusion.map(p => [p.lat, p.lon] as L.LatLngExpression)
      );
      polygonCoords = [latLngs, ...holes];
    }

    let toolTipText = polyDto.title + ' currently not in use.';
    if (polyDto.currentSeed) {
      toolTipText = polyDto.currentSeed + ' growth state at approx ' + Math.round(((polyDto.fillOpacity - 0.2) / 0.6) * 100) + '%';
    }

    const color = isFocused ? MapColors.FIELD_FOCUSED : polyDto.color;
    const weight = isFocused ? 5 : polyDto.weight;

    const polygon = L.polygon(polygonCoords, {
      color,
      weight,
      fillColor: polyDto.fillColor,
      fillOpacity: polyDto.fillOpacity,
      interactive: interactive,
    }).bindTooltip(toolTipText, {permanent: false, direction: 'top'})
      .bindPopup(`
        <div class="field-popup">
          <div class="field-info">
            <span class="field-name">${polyDto.title || 'No Name'}</span>
            <span class="crop-name">${polyDto.currentSeed || 'No crop planted'}</span>
          </div>
          <a href="#/field/details/${polyDto.fieldId}" class="field-details-link">
            View Details →
          </a>
        </div>
      `);

    if (interactive) {
      polygon.on('click', () => {
        this.map?.fitBounds(polygon.getBounds());
      });
    }

    return polygon;
  }

  private createEmptyFieldIcon(center: [number, number]): L.Marker {
    const latLng = L.latLng(center[0], center[1]);

    const icon = L.divIcon({
      className: 'empty-field-icon',
      html: `
        <div style="
          background: white;
          border: 2px solid #28a745;
          border-radius: 50%;
          width: 32px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          box-shadow: 0 2px 4px rgba(0,0,0,0.2);
        ">
          🌱
        </div>
      `,
      iconSize: [32, 32],
      iconAnchor: [16, 16]
    });

    const marker = L.marker(latLng, {
      icon,
      interactive: false,
      zIndexOffset: 1000
    });

    marker.bindTooltip('Ready to plant', {
      permanent: false,
      direction: 'top',
      offset: [0, -16]
    });

    return marker;
  }

  private extendBoundsFromLatLngs(
    bounds: L.LatLngBounds,
    latLngs: L.LatLng[] | L.LatLng[][] | L.LatLng[][][]
  ): void {
    const anyLatLngs = latLngs as any;
    if (Array.isArray(anyLatLngs)) {
      anyLatLngs.forEach((item: any) => {
        if (Array.isArray(item)) {
          this.extendBoundsFromLatLngs(bounds, item);
        } else {
          bounds.extend(item as L.LatLng);
        }
      });
    }
  }


  drawLockedPolygon(
    coords: L.LatLng[],
    color: string,
    label: string,
    clickable: boolean = false,
    popupConfig?: {
      title: string;
      actions: Array<{ label: string; callback: () => void; style?: 'primary' | 'danger' }>;
    },
    holes?: L.LatLng[][],
    bringToFront: boolean = false
  ): L.Polygon {
    const polygonCoords: L.LatLngExpression[] | L.LatLngExpression[][] = holes && holes.length > 0
      ? [coords, ...holes]
      : coords;

    const polygon = L.polygon(polygonCoords, {
      color: color,
      weight: 3,
      fillColor: color,
      fillOpacity: 0.2,
      interactive: clickable
    }).addTo(this.map!);

    if (bringToFront) {
      polygon.bringToFront();
    }

    polygon.bindTooltip(label, {
      permanent: false,
      direction: 'center',
      className: 'locked-polygon-label'
    });

    if (clickable && popupConfig) {
      const popupContent = document.createElement('div');
      popupContent.className = 'locked-polygon-popup-content';
      popupContent.style.padding = '8px';
      popupContent.style.minWidth = '150px';

      const title = document.createElement('div');
      title.textContent = popupConfig.title;
      title.style.fontWeight = 'bold';
      title.style.marginBottom = '8px';
      popupContent.appendChild(title);

      const actionsContainer = document.createElement('div');
      actionsContainer.style.display = 'flex';
      actionsContainer.style.flexDirection = 'row';
      actionsContainer.style.gap = '6px';

      popupConfig.actions.forEach(action => {
        const button = document.createElement('button');
        button.textContent = action.label;
        button.className = 'btn btn-sm';

        if (action.style === 'danger') {
          button.classList.add('btn-danger');
        } else if (action.style === 'primary') {
          button.classList.add('btn-primary');
        } else {
          button.classList.add('btn-outline-secondary');
        }

        button.onclick = () => {
          action.callback();
          polygon.closePopup();
        };
        actionsContainer.appendChild(button);
      });

      popupContent.appendChild(actionsContainer);

      polygon.bindPopup(popupContent, {
        closeButton: true,
        maxWidth: 200,
        className: 'locked-polygon-popup'
      });
    }

    return polygon;
  }

  removeLockedPolygon(polygon: L.Polygon): void {
    if (this.map && polygon) {
      this.map.removeLayer(polygon);
    }
  }


  public drawOverlapPolygon(coordinates: L.LatLng[]): L.Polygon | null {
    if (!this.map || coordinates.length < 3) {
      return null;
    }

    const mapContainer = this.map.getContainer();
    let svgDefs = mapContainer.querySelector('svg defs');

    if (!svgDefs) {
      const svg = mapContainer.querySelector('svg.leaflet-zoom-animated');
      if (svg) {
        const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
        defs.innerHTML = `
          <pattern id="warning-stripes" patternUnits="userSpaceOnUse" width="10" height="10" patternTransform="rotate(45)">
            <rect width="5" height="10" fill="#ff0000" opacity="0.8"/>
            <rect x="5" width="5" height="10" fill="#ff3333" opacity="0.8"/>
          </pattern>
        `;
        svg.insertBefore(defs, svg.firstChild);
      }
    }

    const overlapPolygon = L.polygon(coordinates, {
      color: MapColors.OVERLAP_ERROR,
      weight: 5,
      fillColor: 'url(#warning-stripes)',
      fillOpacity: 1,
      dashArray: '8, 4',
      interactive: false,
      className: 'overlap-polygon-warning'
    }).addTo(this.map);

    setTimeout(() => {
      const pathElement = overlapPolygon.getElement();
      if (pathElement) {
        pathElement.setAttribute('fill', 'url(#warning-stripes)');
        pathElement.setAttribute('fill-opacity', '0.8');
      }
    }, 100);

    overlapPolygon.bindTooltip('⚠️ OVERLAP DETECTED ⚠️', {
      permanent: true,
      direction: 'center',
      className: 'overlap-tooltip-warning'
    });

    const bounds = overlapPolygon.getBounds();
    this.map.fitBounds(bounds, {
      padding: [50, 50],
      maxZoom: 18
    });

    return overlapPolygon;
  }

  public removeOverlapPolygon(polygon: L.Polygon): void {
    if (this.map && polygon) {
      this.map.removeLayer(polygon);
    }
  }


  public zoomToField(fieldId: number): void {
    const polygon = this.fieldLayerMap.get(fieldId);
    if (polygon && this.map) {
      this.map.fitBounds(polygon.getBounds(), {
        padding: [50, 50],
        maxZoom: 25
      });
    }
  }

  public zoomToCoordinates(coords: L.LatLng[], padding: number = 50): void {
    if (!this.map || !coords || coords.length === 0) return;

    const bounds = L.latLngBounds(coords);
    this.map.fitBounds(bounds, {padding: [padding, padding]});
  }

  flyToLocation(lat: number, lng: number, zoomLevel: number = 18): void {
    if (this.map) {
      this.map.flyTo([lat, lng], zoomLevel);
    }
  }

  zoomToLocation(lat: number, lng: number, zoomLevel: number = 18): void {
    if (this.map) {
      this.map.setView([lat, lng], zoomLevel);
    }
  }

  centerAt(lat: number, lon: number): void {
    this.map?.setView([lat, lon], 14);
  }

  setZoom(zoomLevel: number): void {
    if (this.map) {
      this.map.setZoom(zoomLevel);
    }
  }

  getCurrentPosition(): Promise<{ lat: number; lon: number }> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position: GeolocationPosition) => {
          resolve({
            lat: position.coords.latitude,
            lon: position.coords.longitude
          });
        },
        (error: GeolocationPositionError) => {
          switch (error.code) {
            case error.PERMISSION_DENIED:
              reject(new Error('User denied the request for geolocation'));
              break;
            case error.POSITION_UNAVAILABLE:
              reject(new Error('Location information is unavailable'));
              break;
            case error.TIMEOUT:
              reject(new Error('Request timed out'));
              break;
            default:
              reject(new Error('Unknown geolocation error'));
          }
        },
        {
          enableHighAccuracy: true,
          timeout: 30000,
          maximumAge: 0
        }
      );
    });
  }

  async showUserLocation(): Promise<void> {
    try {
      const position = await this.getCurrentPosition();
      this.centerAt(position.lat, position.lon);
    } catch (error) {
      console.error("Error while requesting the location:", error);
    }
  }

  addMarker(lat: number, lng: number, label: string, draggable: boolean = true, iconUrl?: string): void {
    if (!this.map) return;

    const icon = iconUrl
      ? L.icon({
        iconUrl,
        iconSize: [25, 25],
        iconAnchor: [12, 25],
      })
      : new L.Icon.Default();

    const marker = L.marker([lat, lng], {
      draggable,
      icon,
    }).addTo(this.map);

    this.markers.push(marker);

    if (label) {
      marker.bindPopup(label).openPopup();
    }

    this.map.setView([lat, lng], 18);

    marker.on('dragend', (e: L.LeafletEvent) => {
      const m = e.target as L.Marker;
      const pos = m.getLatLng();
      this.markerDragged.emit({lat: pos.lat, lon: pos.lng});
    });
  }

  clearMarker(): void {
    if (!this.map) return;

    this.markers.forEach(m => this.map?.removeLayer(m));
    this.markers = [];
  }
}
