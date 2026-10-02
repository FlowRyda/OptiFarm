import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from "../global/globals";
import {Seed} from "../dtos/seed/seed";


@Injectable({
  providedIn: 'root'
})
export class SeedService {
  private baseUrl = this.globals.backendUri + '/seeds';

  constructor(private http: HttpClient, private globals: Globals) {
  }

  createCropType(seed: Seed): Observable<Seed> {
    return this.http.post<Seed>(this.baseUrl + '/create', seed);
  }

  getAll(): Observable<Seed[]> {
    return this.http.get<Seed[]>(this.baseUrl);
  }

  delete(id: number) {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  getById(id: number): Observable<Seed> {
    return this.http.get<Seed>(`${this.baseUrl}/${id}`);
  }

  update(seed: Seed) {
    return this.http.put<Seed>(`${this.baseUrl}/edit/${seed.id}`, seed);
  }


}
