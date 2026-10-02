import {Injectable} from '@angular/core';
import {Globals} from "../global/globals";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {AuthService} from "./auth.service";
import {
  ActiveFieldCreateDto,
  ActiveFieldDetailDto,
  ActiveFieldUpdateDto,
  SimpleActiveFieldDto
} from "../dtos/activefield";
import {CropCreateDto} from "../dtos/crop.s";

@Injectable({
  providedIn: 'root'
})
export class ActiveFieldService {
  private readonly fieldBaseUri: string = this.globals.backendUri + '/activeField';

  constructor(private readonly httpClient: HttpClient, private readonly globals: Globals, private readonly authService: AuthService) {
  }

  getAllActiveFields(): Observable<SimpleActiveFieldDto[]> {
    return this.httpClient.get<SimpleActiveFieldDto[]>(this.fieldBaseUri);
  }

  getAllActiveFieldsWithoutEndDate(): Observable<SimpleActiveFieldDto[]> {
    return this.httpClient.get<SimpleActiveFieldDto[]>(this.fieldBaseUri + '/current');
  }

  createActiveField(activeFieldDto: ActiveFieldCreateDto): Observable<ActiveFieldCreateDto> {
    const token = this.authService.getToken();
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    return this.httpClient.post<ActiveFieldCreateDto>(this.fieldBaseUri, activeFieldDto, {headers});
  }

  editActiveField(activeFieldDto: ActiveFieldUpdateDto, id: number): Observable<ActiveFieldUpdateDto> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.httpClient.put<ActiveFieldUpdateDto>(this.fieldBaseUri + '/edit/' + id, activeFieldDto, {headers});
  }

  getActiveFieldById(id: number): Observable<ActiveFieldDetailDto> {
    return this.httpClient.get<ActiveFieldDetailDto>(this.fieldBaseUri + '/' + id);
  }

  getActiveFieldByFieldId(id: number): Observable<ActiveFieldDetailDto> {
    return this.httpClient.get<ActiveFieldDetailDto>(this.fieldBaseUri + '/by-field/' + id);
  }


  deleteField(id: number): Observable<boolean> {
    return this.httpClient.delete<boolean>(this.fieldBaseUri + '/' + id);
  }

  harvestField(cropCreateDto: CropCreateDto, id: number): Observable<CropCreateDto> {
    return this.httpClient.post<CropCreateDto>(this.fieldBaseUri + '/harvest/' + id, cropCreateDto);
  }
}



