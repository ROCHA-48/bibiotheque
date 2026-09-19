import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Books } from '../_model/books';
import { Users } from '../_model/users';
import { BooksService } from '../_service/books.service';
import { UsersService } from '../_service/users.service';
import { BorrowService } from '../_service/borrow.service';
import { Borrow } from '../_model/borrow';

interface HistoryEntry extends Borrow {
  borrowerName: string;
}

@Component({
  selector: 'app-book-details',
  templateUrl: './book-details.component.html',
  styleUrls: ['./book-details.component.css']
})
export class BookDetailsComponent implements OnInit {

  id: number;
  book: Books;
  borrowHistory: HistoryEntry[] = [];
  user: Users;

  // Tri de l'historique
  sortField = 'issueDate';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(private route: ActivatedRoute,
    private router: Router,
    private bookService: BooksService,
    public userService: UsersService,
    private borrowService: BorrowService
  ) { }

  ngOnInit(): void {
    this.id = this.route.snapshot.params['bookId'];
    this.book = new Books();
    this.bookService.getBookById(this.id).subscribe(data => {
      this.book = data;
      this.loadBorrowHistory();
    });
  }

  /** Historique des emprunts de ce livre + noms des emprunteurs. */
  private loadBorrowHistory(): void {
    this.borrowService.getBookBorrowHistory(this.id).subscribe(borrows => {
      const entries: HistoryEntry[] = (borrows || []).map(b => ({ ...b, borrowerName: '…' }));
      this.borrowHistory = entries;

      entries.forEach(entry => {
        this.userService.getUserById(entry.userId).subscribe(user => {
          entry.borrowerName = user?.name || user?.username || 'Inconnu';
        });
      });
    });
  }

  /** Nombre d'emprunts non retournés. */
  get activeBorrowsCount(): number {
    return this.borrowHistory.filter(b => !b.returnDate).length;
  }

  // --- Tri ---
  get sortedHistory(): HistoryEntry[] {
    const data = [...this.borrowHistory];
    const field = this.sortField as keyof HistoryEntry;
    data.sort((a, b) => {
      const valA = a[field] ?? '';
      const valB = b[field] ?? '';
      if (valA instanceof Date || typeof valA === 'string' && field.toLowerCase().includes('date')) {
        return this.compare(new Date(valA as any).getTime(), new Date(valB as any).getTime());
      }
      return this.compare(valA, valB);
    });
    return this.sortDirection === 'asc' ? data : data.reverse();
  }

  private compare(a: any, b: any): number {
    if (a === b) return 0;
    if (a == null) return 1;
    if (b == null) return -1;
    return a > b ? 1 : -1;
  }

  toggleSort(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) return '↕';
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  // --- Helpers d'affichage ---
  isOverdue(entry: Borrow): boolean {
    if (entry.returnDate || !entry.dueDate) return false;
    return new Date(entry.dueDate).getTime() < Date.now();
  }

  formatDate(date: any): string {
    if (!date) return '—';
    const d = new Date(date);
    if (isNaN(d.getTime())) return String(date);
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  openBorrowBook(): void {
    this.router.navigate(['borrow-book']);
  }

  openReservation(): void {
    this.router.navigate(['reservations']);
  }
}
