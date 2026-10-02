import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {Vehicle} from '../dtos/vehicle';

@Injectable({
  providedIn: 'root'
})
export class VehicleService {

  private baseUrl: string = this.globals.backendUri + '/vehicle';

  constructor(
    private http: HttpClient,
    private globals: Globals
  ) {
  }

  /** Get all vehicles */
  getAll(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(this.baseUrl);
  }

  /** Get a single vehicle by id */
  getById(id: number): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.baseUrl}/${id}`);
  }

  /** Create a new vehicle */
  create(vehicle: Vehicle): Observable<Vehicle> {
    return this.http.post<Vehicle>(this.baseUrl, vehicle);
  }

  /** Update an existing vehicle */
  update(id: number, vehicle: Vehicle): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.baseUrl}/${id}`, vehicle);
  }

  /** Delete a vehicle by id */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
