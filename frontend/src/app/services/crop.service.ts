import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from "../global/globals";
import {CropCreateDto, CropDetailDto, CropDto} from "../dtos/crop.s";


@Injectable({
  providedIn: 'root'
})
export class CropService {
  private readonly baseUrl = this.globals.backendUri + '/crops';

  constructor(private readonly http: HttpClient,
              private readonly globals: Globals) {
  }

  createCrop(cropCreateDto: CropCreateDto): Observable<CropCreateDto> {
    return this.http.post<CropCreateDto>(this.baseUrl, cropCreateDto);
  }

  getAll(): Observable<CropDetailDto[]> {
    return this.http.get<CropDetailDto[]>(this.baseUrl);
  }

  delete(id: number) {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  getById(id: number): Observable<CropDetailDto> {
    return this.http.get<CropDetailDto>(`${this.baseUrl}/${id}`);
  }

  getCropsByFieldId(fieldId: number): Observable<CropDto[]> {
    return this.http.get<CropDto[]>(`${this.baseUrl}/field/${fieldId}`);
  }
}
