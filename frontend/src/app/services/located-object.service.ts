import { Injectable } from '@angular/core';
import {Globals} from "../global/globals";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { LocatedObjectDto } from "../dtos/locator/locatedObjectDto";

@Injectable({
  providedIn: 'root'
})
export class LocatedObjectService {
  private readonly fieldBaseUri: string = this.globals.backendUri + '/locator';

  constructor(private readonly httpClient: HttpClient, private readonly globals: Globals) {
  }

  getAllLocatedObjects(): Observable<LocatedObjectDto[]> {
    return this.httpClient.get<LocatedObjectDto[]>(this.fieldBaseUri + '/all');
  }
}
