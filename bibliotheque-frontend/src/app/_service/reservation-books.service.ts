import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Books } from '../_model/books';

@Injectable({
  providedIn: 'root'
})
export class ReservationBooksService {

  private baseURL = "http://localhost:8080/api/reservations/books";

  constructor(private httpClient: HttpClient) { }

  getBooksList(): Observable<Books[]> {
    return this.httpClient.get<Books[]>(this.baseURL).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur est survenue.';
    if (error.error && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'Le serveur est injoignable.';
    }
    return throwError(errorMessage);
  }
}
