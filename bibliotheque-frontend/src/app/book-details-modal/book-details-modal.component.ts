import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { Books } from '../_model/books';
import { Borrow } from '../_model/borrow';
import { ModalService } from '../_service/modal.service';
import { BorrowService } from '../_service/borrow.service';
import { UsersService } from '../_service/users.service';
import { ToastService } from '../_service/toast.service';

interface HistoryEntry extends Borrow {
  borrowerName: string;
}

@Component({
  selector: 'app-book-details-modal',
  templateUrl: './book-details-modal.component.html',
  styleUrls: ['./book-details-modal.component.css']
})
export class BookDetailsModalComponent implements OnInit, OnDestroy {
  isOpen = false;
  book: Books = new Books();
  loading = false;
  borrowHistory: HistoryEntry[] = [];

  sortField = 'issueDate';
  sortDirection: 'asc' | 'desc' = 'desc';

  private sub!: Subscription;

  constructor(
    private modalService: ModalService,
    private borrowService: BorrowService,
    private usersService: UsersService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.sub = this.modalService.bookDetailsModal$.subscribe((book) => {
      if (book) {
        this.book = book;
        this.isOpen = true;
        this.borrowHistory = [];
        this.loadBorrowHistory();
      } else {
        this.isOpen = false;
      }
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  private loadBorrowHistory(): void {
    this.loading = true;
    this.borrowService.getBookBorrowHistory(this.book.bookId).subscribe({
      next: (borrows) => {
        const entries: HistoryEntry[] = (borrows || []).map(b => ({ ...b, borrowerName: '…' }));
        this.borrowHistory = entries;
        this.loading = false;

        entries.forEach(entry => {
          this.usersService.getUserById(entry.userId).subscribe(user => {
            entry.borrowerName = user?.name || user?.username || 'Inconnu';
          });
        });
      },
      error: () => {
        this.loading = false;
        this.toastService.error("Impossible de charger l'historique des emprunts.");
      }
    });
  }

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
      if (field.toLowerCase().includes('date')) {
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

  // --- Helpers ---
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

  close(): void {
    this.modalService.closeBookDetailsModal();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.close();
    }
  }
}
