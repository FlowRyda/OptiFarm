import {Injectable} from '@angular/core';
import {Globals} from "../global/globals";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Seed} from "../components/list-dashboards/seed-list/seed";
import {Equipment} from "../components/list-dashboards/equipment-list/equipment";
import {Garage} from "../components/list-dashboards/garage-list/garage";
import {Crop} from "../components/list-dashboards/crop-list/crop";
import {ToolCategory} from "../components/list-dashboards/tool-category-list/toolCategory";
import {CropType} from "../components/list-dashboards/crop-type-list/cropType";
import {Page} from "../dtos/page";
import {ActiveField} from "../components/list-dashboards/active-field-list/activeField";

@Injectable({
  providedIn: 'root'
})
export class DashboardListService {

  private farmersBaseUrl: string = this.globals.backendUri + '/dashboard';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  private buildParams(
    page: number,
    size: number,
    sort?: any,
    direction?: string
  ): HttpParams {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (sort && direction) {
      params = params.set('sort', `${sort},${direction}`);
    }

    return params;
  }

  private getPage<T>(
    path: string,
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<T>> {
    const params = this.buildParams(page, size, sort, direction);
    return this.httpClient.get<Page<T>>(this.farmersBaseUrl + path, {params});
  }


  getAllGarages(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<Garage>> {
    return this.getPage<Garage>('/garages', page, size, sort, direction);
  }

  getAllFields(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<ActiveField>> {
    return this.getPage<ActiveField>('/fields', page, size, sort, direction);
  }

  getAllCrops(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<Crop>> {
    return this.getPage<Crop>('/crops', page, size, sort, direction);
  }

  getAllToolCategories(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<ToolCategory>> {
    return this.getPage<ToolCategory>('/toolCategoriesOfOwner', page, size, sort, direction);
  }

  getAllCropTypes(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<CropType>> {
    return this.getPage<CropType>('/cropTypesOfOwner', page, size, sort, direction);
  }

  getAllActiveFields(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<ActiveField>> {
    return this.getPage<ActiveField>('/activeFields', page, size, sort, direction);
  }

  getAllSeeds(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<Seed>> {
    return this.getPage<Seed>('/seeds', page, size, sort, direction);
  }

  getAllEquipment(
    page: number = 0,
    size: number = 10,
    sort?: any,
    direction?: string
  ): Observable<Page<Equipment>> {
    return this.getPage<Equipment>('/equipment', page, size, sort, direction);
  }

}
