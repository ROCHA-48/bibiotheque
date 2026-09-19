import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Books } from '../_model/books';
import { BooksService } from '../_service/books.service';
import { ToastService } from '../_service/toast.service';

@Component({
  selector: 'app-create-book',
  templateUrl: './create-book.component.html',
  styleUrls: ['./create-book.component.css']
})
export class CreateBookComponent implements OnInit {

  book: Books = new Books();
  constructor(private booksService: BooksService,
    private router: Router,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
  }

  saveBook() {
    this.booksService.createBook(this.book).subscribe(data => {
      this.toastService.success('Livre créé avec succès !');
      this.goToBooksList();
    },
    error => {
      this.toastService.error('Erreur lors de la création du livre.');
    });
  }

  goToBooksList() {
    this.router.navigate(['/books']);
  }

  onSubmit() {
    console.log(this.book);
    this.saveBook();
  }

}
