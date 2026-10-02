import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from "../global/globals";
import {CropType} from "../dtos/crop-type/crop-type";

@Injectable({
  providedIn: 'root'
})
export class CroptypeService {
  private baseUrl = this.globals.backendUri + '/croptype';

  constructor(private http: HttpClient, private globals: Globals) {
  }

  createCropType(cropType: CropType): Observable<CropType> {
    return this.http.post<CropType>(this.baseUrl + '/create', cropType);
  }

  getAll(): Observable<CropType[]> {
    return this.http.get<CropType[]>(this.baseUrl);
  }

  delete(id: number) {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  getById(id: number): Observable<CropType> {
    return this.http.get<CropType>(`${this.baseUrl}/${id}`);
  }

  update(cropType: CropType) {
    return this.http.put<CropType>(`${this.baseUrl}/edit/${cropType.id}`, cropType);
  }


}
