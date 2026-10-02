import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Tool} from "../dtos/tool";
import {Globals} from "../global/globals";
import {CreateQrCodeDto} from "../dtos/createQrCodeDto";
import {Router} from "@angular/router";
import {CoordinateDto} from "../dtos/locator/coordinateDto";

@Injectable({
  providedIn: 'root'
})
export class ToolService {

  private baseUrl: string = this.globals.backendUri + '/tool';

  constructor(private http: HttpClient, private globals: Globals, private router: Router) {
  }

  /** Get all tools */
  getAll(): Observable<Tool[]> {
    return this.http.get<Tool[]>(this.baseUrl);
  }

  /** Get a single tool by id */
  getById(id: number): Observable<Tool> {
    return this.http.get<Tool>(`${this.baseUrl}/${id}`);
  }

  /** Create a new tool */
  create(tool: Tool): Observable<Tool> {
    return this.http.post<Tool>(this.baseUrl, tool);
  }

  /** Update an existing tool */
  update(id: number, tool: Tool): Observable<Tool> {
    return this.http.put<Tool>(`${this.baseUrl}/${id}`, tool);
  }

  /** Delete a tool by id */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /** Create a new tool */
  generateQrCode(id: number): Observable<Blob> {
    const tree = this.router.createUrlTree(['equipment', "id", 'set-position', 'token']);
    const url = `${window.location.origin}/#${this.router.serializeUrl(tree)}`;
    const createQrCode: CreateQrCodeDto = {
      id: id,
      url: url
    }

    return this.http.post(`${this.baseUrl}/qrcode`, createQrCode, {
      responseType: 'blob'
    });
  }

  setPosition(id: number, token: string, coords: CoordinateDto) {
    return this.http.post(`${this.baseUrl}/position/` + id + '/' + token, coords);
  }
}
