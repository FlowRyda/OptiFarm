import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ToolCategory} from "../dtos/tool";
import {Globals} from "../global/globals";

@Injectable({
  providedIn: 'root'
})
export class ToolCategoryService {

  private baseUrl: string = this.globals.backendUri + '/tool-category';
  constructor(private http: HttpClient, private globals: Globals) {
  }

  getAll(): Observable<ToolCategory[]> {
    return this.http.get<ToolCategory[]>(this.baseUrl);
  }
}
