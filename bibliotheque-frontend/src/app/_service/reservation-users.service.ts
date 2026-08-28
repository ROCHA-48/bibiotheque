import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Users } from '../_model/users';

@Injectable({
  providedIn: 'root'
})
export class ReservationUsersService {

  private baseURL = "http://localhost:8080/api/reservations/users";

  constructor(private httpClient: HttpClient) { }

  getUsersList(): Observable<Users[]> {
    return this.httpClient.get<Users[]>(this.baseURL).pipe(
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
