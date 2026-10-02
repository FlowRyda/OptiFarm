import { Injectable } from '@angular/core';
import {Globals} from "../global/globals";
import { HttpClient } from '@angular/common/http';
import {Observable} from 'rxjs';
import {CreateFarmerDto} from "../dtos/farmer/createFarmerDto";

@Injectable({
  providedIn: 'root'
})
export class SignupService {

  private signupBaseUrl: string = this.globals.backendUri + '/signup';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  createUser(user: CreateFarmerDto): Observable<boolean> {
    return this.httpClient.post<boolean>(this.signupBaseUrl, user);
  }
}
