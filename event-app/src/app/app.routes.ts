import { Routes } from '@angular/router';
import { EventListComponent } from './event-list/event-list';
// 👇 CORRECTION : On importe 'ReservationComponent'
import { ReservationComponent } from './reservation/reservation';

export const routes: Routes = [
  { path: '', component: EventListComponent },
  // 👇 CORRECTION : On utilise 'ReservationComponent'
  { path: 'reservation/:id', component: ReservationComponent }
];
