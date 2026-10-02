import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from "../global/globals";
import {FieldDataDto} from "../dtos/field-data/fieldDataDto";
import {ForecastDto} from "../dtos/forecast/forecastDto";
import {FieldDataDetailDto} from "../dtos/field-data/fieldDataDetailDto";


@Injectable({
    providedIn: 'root'
})
export class FieldDataService {
    private readonly baseUrl = this.globals.backendUri + '/fielddata';

    constructor(private readonly http: HttpClient,
                private readonly globals: Globals) {
    }

    getById(id: number): Observable<FieldDataDto> {
        return this.http.get<FieldDataDto>(`${this.baseUrl}/${id}`);
    }

    getFieldForecastById(id: number): Observable<ForecastDto> {
        return this.http.get<ForecastDto>(`${this.baseUrl}/${id}/forecast`);
    }

    getFieldDataByUserId(id: number): Observable<FieldDataDetailDto[]> {
        return this.http.get<FieldDataDetailDto[]>(`${this.baseUrl}/user/${id}`);
    }
}
