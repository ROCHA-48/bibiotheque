import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Books } from '../_model/books';
import { BooksService } from '../_service/books.service';
import { ModalService } from '../_service/modal.service';

@Component({
  selector: 'app-books-list',
  templateUrl: './books-list.component.html',
  styleUrls: ['./books-list.component.css']
})
export class BooksListComponent implements OnInit {

  books: Books[];

  constructor(
    private booksService: BooksService,
    private router: Router,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {
    this.getBooks();
  }

  private getBooks() {
    this.booksService.getBooksList().subscribe(data => {
      this.books = data;
    });
  }

  updateBook(bookId: number) {
    this.router.navigate(['update-book', bookId]);
  }

  async deleteBook(bookId: number) {
    const confirmed = await this.modalService.confirm({
      title: 'Supprimer le livre',
      message: 'Êtes-vous sûr de vouloir supprimer ce livre ? Cette action est irréversible.',
      confirmText: 'Supprimer',
      cancelText: 'Annuler',
      type: 'danger'
    });

    if (confirmed) {
      this.booksService.deleteBook(bookId).subscribe(data => {
        this.getBooks();
      });
    }
  }

  bookDetails(bookId: number) {
    // Ouvre le détail du livre dans une modale, sans quitter la liste
    const book = this.books.find(b => b.bookId === bookId);
    if (book) {
      this.modalService.openBookDetailsModal(book);
    }
  }

  openBorrowForBook(bookId: number) {
    this.router.navigate(['borrow-book']);
  }

  openReservationForBook(bookId: number) {
    this.router.navigate(['reservations']);
  }
}
