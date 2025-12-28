import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Observable } from 'rxjs'; // <--- Import obligatoire
import { EventService } from '../event';
import { Event } from '../event.model';

@Component({
  selector: 'app-event-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './event-list.html',
  styleUrls: ['./event-list.css']
})
export class EventListComponent implements OnInit {

  // On utilise un Observable (c'est le flux de données)
  events$: Observable<Event[]> | undefined;

  constructor(private eventService: EventService, private router: Router) {}

  ngOnInit() {
    // On connecte le tuyau direct (plus de subscribe manuel)
    this.events$ = this.eventService.getEvents();
  }

  onReserve(id: number | undefined) {
    // 1. On vérifie si le clic est détecté
    console.log("🟢 Clic sur le bouton Réserver !");
    console.log("👉 ID reçu :", id);

    if (id) {
      // 2. On tente la navigation
      this.router.navigate(['/reservation', id])
        .then(success => {
          if (success) {
            console.log("✅ Navigation réussie !");
          } else {
            console.error("❌ Navigation refusée par Angular (vérifiez les Routes)");
          }
        })
        .catch(err => console.error("💥 Erreur Router :", err));
    } else {
      console.error("⛔ ID manquant ou undefined !");
    }
  }
}
