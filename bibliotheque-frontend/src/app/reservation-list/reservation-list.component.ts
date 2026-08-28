import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Books } from '../_model/books';
import { Users } from '../_model/users';
import { Reservation } from '../_model/reservation';
import { ReservationService } from '../_service/reservation.service';

@Component({
  selector: 'app-reservation-list',
  templateUrl: './reservation-list.component.html',
  styleUrls: ['./reservation-list.component.css']
})
export class ReservationListComponent implements OnInit {

  @Input() reservations: Reservation[] = [];
  @Input() books: Books[] = [];
  @Input() users: Users[] = [];
  @Input() loading = false;
  @Input() errorMessage = '';

  @Output() reservationCancelled = new EventEmitter<void>();
  @Output() filterChange = new EventEmitter<string>();

  selectedStatus = 'TOUS';
  cancelError = '';

  // Pagination
  currentPage = 1;
  itemsPerPage = 5;
  totalPages = 1;

  // Tri
  sortField = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(private reservationService: ReservationService) { }

  ngOnInit(): void { }

  // --- Données triées ---
  get sortedReservations(): Reservation[] {
    let data = [...this.reservations];
    if (this.sortField) {
      const field = this.sortField as keyof Reservation;
      data.sort((a, b) => {
        const valA = a[field] ?? '';
        const valB = b[field] ?? '';
        const cmp = String(valA).localeCompare(String(valB), 'fr-FR');
        return this.sortDirection === 'asc' ? cmp : -cmp;
      });
    }
    return data;
  }

  // --- Données paginées ---
  get paginatedReservations(): Reservation[] {
    const sorted = this.sortedReservations;
    this.totalPages = Math.max(1, Math.ceil(sorted.length / this.itemsPerPage));
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
    const start = (this.currentPage - 1) * this.itemsPerPage;
    return sorted.slice(start, start + this.itemsPerPage);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  // --- Tri ---
  toggleSort(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.currentPage = 1;
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) return '↕';
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  // --- Filtre ---
  onFilterChange(status: string): void {
    this.selectedStatus = status;
    this.currentPage = 1;
    this.filterChange.emit(status);
  }

  // --- Helpers ---
  getBookName(bookId: number): string {
    const book = this.books.find(b => b.bookId === bookId);
    return book ? book.bookName : 'Inconnu';
  }

  getUserName(userId: number): string {
    const user = this.users.find(u => u.userId === userId);
    return user ? user.name : 'Inconnu';
  }

  canCancel(status: string): boolean {
    return status === 'EN_ATTENTE' || status === 'DISPONIBLE';
  }

  annuler(reservationId: number): void {
    this.cancelError = '';
    const confirmed = confirm('Êtes-vous sûr de vouloir annuler cette réservation ?');
    if (!confirmed) return;

    this.reservationService.annulerReservation(reservationId).subscribe({
      next: (data) => {
        this.reservationCancelled.emit();
      },
      error: (err) => {
        this.cancelError = err;
      }
    });
  }

  formatDate(date: Date): string {
    if (!date) return '-';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR');
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'EN_ATTENTE': 'En attente',
      'DISPONIBLE': 'Disponible',
      'ANNULEE': 'Annulée',
      'EXPIREE': 'Expirée',
      'HONOREE': 'Honorée'
    };
    return labels[status] || status;
  }

  getBadgeColor(status: string): string {
    const colors: { [key: string]: string } = {
      'EN_ATTENTE': '#f39c12',
      'DISPONIBLE': '#27ae60',
      'ANNULEE': '#95a5a6',
      'EXPIREE': '#e74c3c',
      'HONOREE': '#3498db'
    };
    return colors[status] || '#999';
  }

  getStatusBadgeClass(status: string): string {
    const classes: { [key: string]: string } = {
      'EN_ATTENTE': 'bg-warning text-dark',
      'DISPONIBLE': 'bg-success',
      'ANNULEE': 'bg-secondary',
      'EXPIREE': 'bg-danger',
      'HONOREE': 'bg-info text-dark'
    };
    return classes[status] || 'bg-secondary';
  }
}
