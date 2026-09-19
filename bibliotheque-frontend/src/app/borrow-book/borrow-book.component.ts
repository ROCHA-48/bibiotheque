import { Component, OnInit } from '@angular/core';
import { Books } from '../_model/books';
import { Borrow } from '../_model/borrow';
import { BooksService } from '../_service/books.service';
import { BorrowService } from '../_service/borrow.service';
import { UserAuthService } from '../_service/user-auth.service';
import { ToastService } from '../_service/toast.service';

@Component({
  selector: 'app-borrow-book',
  templateUrl: './borrow-book.component.html',
  styleUrls: ['./borrow-book.component.css']
})
export class BorrowBookComponent implements OnInit {

  books: Books[];
  /** Livres réellement empruntables : au moins un exemplaire en stock. */
  availableBooks: Books[];

  constructor(
    private booksService: BooksService,
    private userAuthService: UserAuthService,
    private borrowService: BorrowService,
    private toastService: ToastService
  ) { }

  userId = this.userAuthService.getUserId();

  ngOnInit(): void {
    this.getBooks();
  }

  private getBooks() {
    this.booksService.getBooksList().subscribe(data => {
      this.books = data;
      // Ne montrer que les livres qu'on peut réellement emprunter maintenant :
      // les livres créés par une réservation (0 exemplaire) n'apparaissent pas ici.
      this.availableBooks = (data || []).filter(book => book.noOfCopies > 0);
    });
  }

  borrow: Borrow = new Borrow();

  borrowBook(bookId: number) {
    this.borrow.bookId = bookId;
    this.borrow.userId = this.userId;
    console.log(this.borrow);
    this.borrowService.borrowBook(this.borrow).subscribe(data => {
      this.toastService.success('Livre emprunté avec succès !');
      // La copie vient d'être décrémentée : si le stock tombe à 0, le livre quitte la liste.
      this.getBooks();
    },
    error => {
      this.toastService.error('Erreur lors de l\'emprunt du livre.');
    });
  }
}
