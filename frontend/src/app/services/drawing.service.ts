import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import * as L from 'leaflet';

@Injectable({
  providedIn: 'root'
})
export class DrawingService {
  private readonly MIN_POINTS = 3;

  // only used for metadata
  private readonly markerCountSubject = new BehaviorSubject<number>(0);
  private readonly isClosedSubject = new BehaviorSubject<boolean>(false);
  private readonly coordinatesSubject = new BehaviorSubject<L.LatLng[]>([]);

  public readonly markerCount$ = this.markerCountSubject.asObservable();
  public readonly isClosed$ = this.isClosedSubject.asObservable();

  constructor() {
  }

  /**
   * Update coordinates (called by map component)
   */
  setCoordinates(coords: L.LatLng[]): void {
    this.coordinatesSubject.next(coords);
    this.markerCountSubject.next(coords.length);
  }

  /**
   * Finalize the boundary (close the polygon)
   */
  finalizeBoundary(): L.LatLng[] | null {
    const coords = this.coordinatesSubject.value;
    if (coords.length < this.MIN_POINTS) {
      return null;
    }

    this.isClosedSubject.next(true);
    return coords;
  }

  /**
   * Clear all markers and reset state
   */
  clearBoundary(): void {
    this.coordinatesSubject.next([]);
    this.isClosedSubject.next(false);
    this.markerCountSubject.next(0);
  }

  /**
   * Get current coordinates as array
   */
  getCurrentCoordinates(): L.LatLng[] {
    return this.coordinatesSubject.value;
  }

}
