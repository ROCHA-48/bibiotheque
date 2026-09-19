import { Component, OnInit } from '@angular/core';
import { Books } from '../_model/books';
import { Users } from '../_model/users';
import { Reservation } from '../_model/reservation';
import { ReservationService } from '../_service/reservation.service';
import { ReservationBooksService } from '../_service/reservation-books.service';
import { ReservationUsersService } from '../_service/reservation-users.service';
import { ToastService } from '../_service/toast.service';
import { UsersService } from '../_service/users.service';

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

  canChooseAdherent = false;

  activeFilter = 'TOUS';

  createModalOpen = false;
  modalError = '';

  draftReservation: Reservation = new Reservation();
  bookSearch = '';

  constructor(
    private reservationService: ReservationService,
    private reservationBooksService: ReservationBooksService,
    private reservationUsersService: ReservationUsersService,
    private usersService: UsersService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.canChooseAdherent = this.usersService.roleMatch(['Admin', 'BIBLIOTHECAIRE']);
    this.loadReservations();
    this.loadBooks();
    this.activeFilter = 'TOUS';
    if (this.canChooseAdherent) {
      this.loadUsers();
    }
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

  openCreateModal(): void {
    this.createModalOpen = true;
    this.modalError = '';
    this.resetDraft();
  }

  closeCreateModal(): void {
    this.createModalOpen = false;
    this.resetDraft();
  }

  resetDraft(): void {
    this.draftReservation = new Reservation();
    this.bookSearch = '';
    this.modalError = '';
  }

  onBookInput(): void {
    const typed = this.bookSearch.trim();
    const match = this.books.find(
      (b) => b.bookName.toLowerCase() === typed.toLowerCase()
    );
    if (match) {
      this.draftReservation.bookId = match.bookId;
      this.draftReservation.newBookTitle = null;
    } else {
      this.draftReservation.bookId = null;
      this.draftReservation.newBookTitle = typed || null;
    }
  }

  isDraftValid(): boolean {
    if (this.draftReservation.bookId == null && !this.draftReservation.newBookTitle) return false;
    if (this.canChooseAdherent) {
      return this.draftReservation.userId != null;
    }
    return true;
  }

  getActiveCountForUser(userId: number): number {
    if (!userId) return 0;
    const activeStatuses = ['EN_ATTENTE', 'DISPONIBLE'];
    return this.reservations.filter(
      (r) => r.userId === userId && activeStatuses.includes(r.status)
    ).length;
  }

  isUserAtQuota(userId: number): boolean {
    return this.getActiveCountForUser(userId) >= 3;
  }

  onCreateSubmit(): void {
    if (!this.isDraftValid()) return;
    if (!this.canChooseAdherent) {
      this.draftReservation.userId = null;
    }

    this.modalError = '';
    this.reservationService.createReservation(this.draftReservation).subscribe({
      next: () => {
        this.toastService.success('Réservation créée avec succès !');
        this.loadReservations();
        this.closeCreateModal();
      },
      error: (err) => {
        this.toastService.error('Erreur lors de la création de la réservation.');
        this.modalError = err;
      }
    });
  }

  onReservationCreated(): void {
    this.loadReservations();
  }

  onReservationCancelled(): void {
    this.loadReservations();
  }

  onFilterChange(status: string): void {
    this.activeFilter = status;
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
