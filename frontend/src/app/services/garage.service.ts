import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Garage} from '../dtos/tool';
import {Globals} from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class GarageService {

  private baseUrl: string = this.globals.backendUri + '/garages';

  constructor(
    private http: HttpClient,
    private globals: Globals
  ) {
  }

  /** Get all garages of the logged-in user */
  getAll(): Observable<Garage[]> {
    return this.http.get<Garage[]>(this.baseUrl);
  }

  getById(id: number) {
    return this.http.get<Garage>(this.baseUrl + '/' + id);
  }

  update(garageDto: Garage) {
    return this.http.put<Garage>(this.baseUrl + '/edit/' + garageDto.id, garageDto);
  }

  delete(id: number) {
    return this.http.delete(this.baseUrl + '/delete/' + id);
  }

  /** Create a new garage */
  create(garage: Garage): Observable<Garage> {
    return this.http.post<Garage>(`${this.baseUrl}/create`, garage);
  }
}
