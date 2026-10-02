import { Injectable } from '@angular/core';
import {Globals} from "../global/globals";
import { HttpClient } from '@angular/common/http';
import {Observable} from 'rxjs';
import { EditFarmerDto } from "../dtos/farmer/editFarmerDto";
import { ChangePasswordDto } from "../dtos/farmer/changePasswordDto";

@Injectable({
  providedIn: 'root'
})
export class FarmerService {

  private farmersBaseUrl: string = this.globals.backendUri + '/farmers';
  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  updateFarmer(editFarmerDto: EditFarmerDto): Observable<boolean> {
    return this.httpClient.put<boolean>(this.farmersBaseUrl, editFarmerDto);
  }

  deleteFarmer(): Observable<boolean> {
    return this.httpClient.delete<boolean>(this.farmersBaseUrl);
  }

  changePassword(changePasswordDto: ChangePasswordDto): Observable<boolean> {
    return this.httpClient.put<boolean>(this.farmersBaseUrl + '/changePassword', changePasswordDto);
  }

  viewFarmer(): Observable<EditFarmerDto> {
    return this.httpClient.get<EditFarmerDto>(this.farmersBaseUrl);
  }
}
