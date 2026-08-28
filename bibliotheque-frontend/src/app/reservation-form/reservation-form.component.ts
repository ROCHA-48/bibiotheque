import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Books } from '../_model/books';
import { Users } from '../_model/users';
import { Reservation } from '../_model/reservation';
import { ReservationService } from '../_service/reservation.service';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css']
})
export class ReservationFormComponent implements OnInit, OnChanges {

  @Input() books: Books[] = [];
  @Input() users: Users[] = [];
  @Input() reservations: Reservation[] = [];
  @Output() reservationCreated = new EventEmitter<void>();

  reservation: Reservation = new Reservation();
  formError = '';

  constructor(private reservationService: ReservationService) { }

  ngOnInit(): void { }

  ngOnChanges(changes: SimpleChanges): void { }

  isFormValid(): boolean {
    return this.reservation.bookId != null && this.reservation.userId != null;
  }

  onSubmit(): void {
    if (!this.isFormValid()) return;

    this.formError = '';
    this.reservationService.createReservation(this.reservation).subscribe({
      next: (data) => {
        this.reservation = new Reservation();
        this.reservationCreated.emit();
      },
      error: (err) => {
        this.formError = err;
      }
    });
  }

  getActiveCountForUser(userId: number): number {
    if (!userId) return 0;
    const activeStatuses = ['EN_ATTENTE', 'DISPONIBLE'];
    return this.reservations.filter(
      r => r.userId === userId && activeStatuses.includes(r.status)
    ).length;
  }

  isUserAtQuota(userId: number): boolean {
    return this.getActiveCountForUser(userId) >= 3;
  }
}
