import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EventService } from '../event';
import { Event } from '../event.model';

@Component({
  selector: 'app-reservation',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reservation.html',
  styles: [`
    .container {
      max-width: 600px;
      margin: 30px auto;
      padding: 20px;
      font-family: sans-serif;
    }
    .card {
      border: 1px solid #ddd;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }
    input {
      padding: 8px;
      width: 60px;
      margin-left: 10px;
    }
    button {
      margin-top: 15px;
      padding: 10px 20px;
      background-color: #007bff;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }
    button:disabled {
      background-color: #ccc;
      cursor: not-allowed;
    }
  `]
})
export class ReservationComponent implements OnInit {
  event: Event | undefined;
  quantity: number = 1;
  totalPrice: number = 0;
  submitting: boolean = false; // ✅ AJOUTER CETTE LIGNE

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private router: Router
  ) {}

  ngOnInit() {
    const id = +this.route.snapshot.params['id'];

    this.eventService.getEventById(id).subscribe({
      next: (data: Event) => {
        this.event = data;
        this.updateTotal();
      },
      error: (err) => {
        console.error('Erreur chargement:', err);
        alert('Impossible de charger l\'événement');
        this.router.navigate(['/']);
      }
    });
  }

  updateTotal() {
    if (!this.event) return;

    this.quantity = Math.max(1, Math.min(4, this.quantity));
    this.totalPrice = this.quantity * this.event.prix;
  }

  onConfirm() {
    if (!this.event || this.submitting) return;

    this.submitting = true;

    const reservation = {
      eventId: this.event.id,
      ticketCount: this.quantity,
      price: this.totalPrice
    };

    this.eventService.bookTicket(reservation).subscribe({
      next: () => {
        alert('✅ Réservation confirmée !');
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error(err);
        alert('❌ Erreur lors de la réservation');
        this.submitting = false;
      }
    });
  }
}
