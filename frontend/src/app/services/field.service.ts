import {Injectable} from '@angular/core';
import {Globals} from "../global/globals";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {
  FieldCreateDto,
  FieldUpdateDto,
  FieldValidationRequestDto,
  FieldValidationResponseDto,
  SimpleFieldDto
} from "../dtos/field";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class FieldService {
  private readonly fieldBaseUri: string = this.globals.backendUri + '/field';

  constructor(private readonly httpClient: HttpClient, private readonly globals: Globals, private readonly authService: AuthService) {
  }

  getAllFields(): Observable<SimpleFieldDto[]> {
    return this.httpClient.get<SimpleFieldDto[]>(this.fieldBaseUri);
  }

  createField(fieldDto: FieldCreateDto): Observable<FieldCreateDto> {
    const token = this.authService.getToken();
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    return this.httpClient.post<FieldCreateDto>(this.fieldBaseUri, fieldDto, {headers});
  }

  editField(fieldDto: FieldUpdateDto, id: number): Observable<FieldUpdateDto> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.httpClient.put<FieldCreateDto>(this.fieldBaseUri + '/edit/' + id, fieldDto, {headers});
  }

  getFieldById(id: number): Observable<SimpleFieldDto> {
    return this.httpClient.get<SimpleFieldDto>(this.fieldBaseUri + '/' + id);
  }

  deleteField(id: number): Observable<boolean> {
    return this.httpClient.delete<boolean>(this.fieldBaseUri + '/' + id);
  }

  validateField(request: FieldValidationRequestDto): Observable<FieldValidationResponseDto> {
    const token = this.authService.getToken();
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    return this.httpClient.post<FieldValidationResponseDto>(
      this.fieldBaseUri + '/validate',
      request,
      {headers}
    );
  }
}
