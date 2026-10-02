import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {Observable} from "rxjs";
import {Schedule} from "../dtos/schedule";


@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  private readonly scheduleBaseUri: string = this.globals.backendUri + '/schedules';

  constructor(private http: HttpClient, private globals: Globals) {
  }

  getAll(): Observable<Schedule[]> {
    return this.http.get<Schedule[]>(this.scheduleBaseUri);
  }

  getById(id: number): Observable<Schedule> {
    return this.http.get<Schedule>(`${this.scheduleBaseUri}/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.scheduleBaseUri}/${id}`);
  }

  save (schedule: Schedule): Observable<Schedule> {
    return this.http.post<Schedule>(`${this.scheduleBaseUri}/save`, schedule);
  }
  updateSchedule(id: number, schedule: Schedule): Observable<Schedule> {
    return this.http.put<Schedule>(`${this.scheduleBaseUri}/${id}`, schedule);
  }

  computeSchedule(): Observable<Schedule> {
    // no body needed
    console.log("Requesting new schedule computation");
    return this.http.post<Schedule>(this.scheduleBaseUri, null);
  }

  reschedule(id: number): Observable<Schedule> {
    //no body needed as id is in the path
    console.log(`Requesting recompute of schedule ${id}...`);
    return this.http.post<Schedule>(`${this.scheduleBaseUri}/${id}/compute`, null);
  }
}
