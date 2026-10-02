import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from "../global/globals";
import {FarmDataDto} from "../dtos/farm-data/farmDataDto";
import {ForecastDto} from "../dtos/forecast/forecastDto";

@Injectable({
  providedIn: 'root'
})
export class FarmDataService {
  private readonly baseUrl = this.globals.backendUri + '/farmdata';

  constructor(private readonly http: HttpClient,
              private readonly globals: Globals) {
  }

  getById(id: number): Observable<FarmDataDto> {
    return this.http.get<FarmDataDto>(`${this.baseUrl}/${id}`);
  }

  getFarmForecastByUserId(id: number): Observable<ForecastDto> {
    return this.http.get<ForecastDto>(`${this.baseUrl}/${id}/forecast`);
  }
}
