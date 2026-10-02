import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {Observable} from "rxjs";
import {Task} from "../dtos/task";

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private baseUrl: string = this.globals.backendUri + '/task';

  constructor(private http: HttpClient, private globals: Globals) {
  }

  getAll(): Observable<Task[]> {
    return this.http.get<Task[]>(this.baseUrl);
  }

  getById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.baseUrl}/${id}`);
  }

  create(task: Task): Observable<Task> {
    console.log(task)
    return this.http.post<Task>(this.baseUrl, task);
  }

  update(id: number, task: Task): Observable<Task> {
    console.log(task)
    return this.http.put<Task>(`${this.baseUrl}/${id}`, task);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
