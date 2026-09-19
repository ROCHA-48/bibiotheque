import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Books } from '../_model/books';

export interface ConfirmModalData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'danger' | 'warning' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ModalService {

  private loginModalSubject = new Subject<boolean>();
  private createBookModalSubject = new Subject<boolean>();
  private registerModalSubject = new Subject<boolean>();
  private confirmModalSubject = new Subject<ConfirmModalData | null>();
  private confirmResultSubject = new Subject<boolean>();
  private bookDetailsModalSubject = new Subject<Books | null>();

  loginModal$ = this.loginModalSubject.asObservable();
  createBookModal$ = this.createBookModalSubject.asObservable();
  registerModal$ = this.registerModalSubject.asObservable();
  confirmModal$ = this.confirmModalSubject.asObservable();
  confirmResult$ = this.confirmResultSubject.asObservable();
  bookDetailsModal$ = this.bookDetailsModalSubject.asObservable();

  openLoginModal() {
    this.loginModalSubject.next(true);
  }

  closeLoginModal() {
    this.loginModalSubject.next(false);
  }

  openCreateBookModal() {
    this.createBookModalSubject.next(true);
  }

  closeCreateBookModal() {
    this.createBookModalSubject.next(false);
  }

  openRegisterModal() {
    this.registerModalSubject.next(true);
  }

  closeRegisterModal() {
    this.registerModalSubject.next(false);
  }

  openBookDetailsModal(book: Books) {
    this.bookDetailsModalSubject.next(book);
  }

  closeBookDetailsModal() {
    this.bookDetailsModalSubject.next(null);
  }

  confirm(data: ConfirmModalData): Promise<boolean> {
    return new Promise((resolve) => {
      this.confirmModalSubject.next(data);
      const sub = this.confirmResult$.subscribe((result) => {
        sub.unsubscribe();
        this.confirmModalSubject.next(null);
        resolve(result);
      });
    });
  }

  confirmAccept() {
    this.confirmResultSubject.next(true);
  }

  confirmCancel() {
    this.confirmResultSubject.next(false);
  }
}
