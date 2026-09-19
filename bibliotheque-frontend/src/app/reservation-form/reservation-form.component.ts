import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Books } from '../_model/books';
import { Users } from '../_model/users';
import { Reservation } from '../_model/reservation';
import { ReservationService } from '../_service/reservation.service';
import { ToastService } from '../_service/toast.service';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css']
})
export class ReservationFormComponent implements OnInit, OnChanges {

  @Input() books: Books[] = [];
  @Input() users: Users[] = [];
  @Input() canChooseAdherent = false;
  @Input() reservations: Reservation[] = [];
  @Output() reservationCreated = new EventEmitter<void>();

  reservation: Reservation = new Reservation();
  formError = '';
  showForm = false;
  bookSearch = '';

  constructor(private reservationService: ReservationService, private toastService: ToastService) { }

  ngOnInit(): void { }

  ngOnChanges(changes: SimpleChanges): void { }

  toggleForm(): void {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.reservation = new Reservation();
    this.bookSearch = '';
    this.formError = '';
  }

  // Résout le nom de livre tapé vers son bookId ; si aucun livre du catalogue ne correspond,
  // le titre libre est conservé dans newBookTitle pour une réservation hors catalogue.
  onBookInput(): void {
    const typed = this.bookSearch.trim();
    const match = this.books.find(
      b => b.bookName.toLowerCase() === typed.toLowerCase()
    );
    if (match) {
      this.reservation.bookId = match.bookId;
      this.reservation.newBookTitle = null;
    } else {
      this.reservation.bookId = null;
      this.reservation.newBookTitle = typed || null;
    }
  }

  isFormValid(): boolean {
    // Soit un livre du catalogue, soit un titre libre saisi
    if (this.reservation.bookId == null && !this.reservation.newBookTitle) return false;
    if (this.canChooseAdherent) {
      return this.reservation.userId != null;
    }
    return true;
  }

  onSubmit(): void {
    if (!this.isFormValid()) return;
    if (!this.canChooseAdherent) {
      // RS-04 : l'identité vient du token, pas du formulaire
      this.reservation.userId = null;
    }

    // Réservation hors catalogue : on ne renvoie pas un bookId invalide
    if (this.reservation.bookId == null) {
      this.reservation.bookId = undefined as any;
    }

    this.formError = '';
    this.reservationService.createReservation(this.reservation).subscribe({
      next: (data) => {
        this.toastService.success('Réservation créée avec succès !');
        this.resetForm();
        this.showForm = false;
        this.reservationCreated.emit();
      },
      error: (err) => {
        this.toastService.error('Erreur lors de la création de la réservation.');
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
