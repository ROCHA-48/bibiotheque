import { Component, OnInit } from '@angular/core';
import { Books } from '../_model/books';
import { Users } from '../_model/users';
import { Reservation } from '../_model/reservation';
import { ReservationService } from '../_service/reservation.service';
import { ReservationBooksService } from '../_service/reservation-books.service';
import { ReservationUsersService } from '../_service/reservation-users.service';

@Component({
  selector: 'app-reservations',
  templateUrl: './reservations.component.html',
  styleUrls: ['./reservations.component.css']
})
export class ReservationsComponent implements OnInit {

  reservations: Reservation[] = [];
  books: Books[] = [];
  users: Users[] = [];

  loading = false;
  errorMessage = '';

  constructor(
    private reservationService: ReservationService,
    private reservationBooksService: ReservationBooksService,
    private reservationUsersService: ReservationUsersService
  ) { }

  ngOnInit(): void {
    this.loadReservations();
    this.loadBooks();
    this.loadUsers();
  }

  loadReservations(): void {
    this.loading = true;
    this.errorMessage = '';
    this.reservationService.getReservationsList().subscribe({
      next: (data) => {
        this.reservations = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err;
        this.loading = false;
      }
    });
  }

  loadBooks(): void {
    this.reservationBooksService.getBooksList().subscribe({
      next: (data) => { this.books = data; },
      error: (err) => { this.errorMessage = 'Impossible de charger les livres.'; }
    });
  }

  loadUsers(): void {
    this.reservationUsersService.getUsersList().subscribe({
      next: (data) => { this.users = data; },
      error: (err) => { this.errorMessage = 'Impossible de charger les adhérents.'; }
    });
  }

  onReservationCreated(): void {
    this.loadReservations();
  }

  onReservationCancelled(): void {
    this.loadReservations();
  }

  onFilterChange(status: string): void {
    this.loading = true;
    this.errorMessage = '';
    if (status === 'TOUS') {
      this.reservationService.getReservationsList().subscribe({
        next: (data) => {
          this.reservations = data;
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = err;
          this.loading = false;
        }
      });
    } else {
      this.reservationService.getReservationsByStatus(status).subscribe({
        next: (data) => {
          this.reservations = data;
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = err;
          this.loading = false;
        }
      });
    }
  }
}
