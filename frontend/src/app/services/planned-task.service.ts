import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable({ providedIn: 'root' })
export class PlannedTaskService {
  private readonly baseUrl = this.globals.backendUri + '/planned-tasks'; // Dein neuer Endpoint

  constructor(private http: HttpClient, private globals: Globals) {}

  update(id: number, dto: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
