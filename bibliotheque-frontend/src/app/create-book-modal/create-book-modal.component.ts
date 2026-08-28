import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { Books } from '../_model/books';
import { BooksService } from '../_service/books.service';
import { ModalService } from '../_service/modal.service';

@Component({
  selector: 'app-create-book-modal',
  templateUrl: './create-book-modal.component.html',
  styleUrls: ['./create-book-modal.component.css']
})
export class CreateBookModalComponent implements OnInit, OnDestroy {
  isOpen = false;
  book: Books = new Books();
  private sub!: Subscription;

  constructor(
    private booksService: BooksService,
    private modalService: ModalService
  ) {}

  ngOnInit() {
    this.sub = this.modalService.createBookModal$.subscribe((isOpen) => {
      this.isOpen = isOpen;
      if (isOpen) {
        this.book = new Books();
      }
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  close() {
    this.modalService.closeCreateBookModal();
  }

  onSubmit() {
    this.booksService.createBook(this.book).subscribe(
      (data) => {
        console.log(data);
        this.close();
        window.location.reload();
      },
      (error) => console.log(error)
    );
  }

  onBackdropClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.close();
    }
  }
}
