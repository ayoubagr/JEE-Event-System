import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event } from './event.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  // Mettez ici l'URL exacte de votre backend
  private apiUrlevent = 'http://localhost:8081/events';
  private apiUrlreservation = 'http://localhost:8082/reservations';

  constructor(private http: HttpClient) {}

  getEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrlevent);
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrlevent}/${id}`);
  }

  bookTicket(reservationData: any): Observable<any> {
    // Adapter l'URL selon votre Controller Java (ex: /reservations ou /tickets)
    return this.http.post(`${this.apiUrlreservation}`, reservationData);
  }
}
