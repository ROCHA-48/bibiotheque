import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Reservation } from '../_model/reservation';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  private baseURL = "http://localhost:8080/api/reservations";

  constructor(private httpClient: HttpClient) { }

  getReservationsList(): Observable<Reservation[]> {
    return this.httpClient.get<Reservation[]>(`${this.baseURL}`).pipe(
      catchError(this.handleError)
    );
  }

  getReservationsByStatus(status: string): Observable<Reservation[]> {
    return this.httpClient.get<Reservation[]>(`${this.baseURL}?status=${status}`).pipe(
      catchError(this.handleError)
    );
  }

  createReservation(reservation: Reservation): Observable<Object> {
    return this.httpClient.post(`${this.baseURL}`, reservation).pipe(
      catchError(this.handleError)
    );
  }

  annulerReservation(reservationId: number): Observable<Object> {
    return this.httpClient.patch(`${this.baseURL}/${reservationId}/annuler`, {}).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
    if (error.error && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'Le serveur est injoignable. Vérifiez que le backend est démarré.';
    } else if (error.status >= 500) {
      errorMessage = 'Erreur interne du serveur. Veuillez réessayer plus tard.';
    }
    return throwError(errorMessage);
  }
}
