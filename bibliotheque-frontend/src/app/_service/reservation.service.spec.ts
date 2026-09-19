import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ReservationService } from './reservation.service';
import { Reservation } from '../_model/reservation';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;

  const API = 'http://localhost:8080/api/reservations';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ReservationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should GET the reservations list on /api/reservations', () => {
    const mock: Reservation[] = [{ reservationId: 1 } as any];
    service.getReservationsList().subscribe(data => {
      expect(data).toEqual(mock);
    });
    const req = httpMock.expectOne(`${API}`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('should GET reservations filtered by status with a query param', () => {
    service.getReservationsByStatus('EN_ATTENTE').subscribe();
    const req = httpMock.expectOne(`${API}?status=EN_ATTENTE`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should POST a new reservation on /api/reservations', () => {
    const payload = { bookId: 4 } as any;
    service.createReservation(payload).subscribe();
    const req = httpMock.expectOne(`${API}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({});
  });

  it('should PATCH /{id}/annuler to cancel a reservation', () => {
    service.annulerReservation(7).subscribe();
    const req = httpMock.expectOne(`${API}/7/annuler`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should surface the backend business message when present', () => {
    let error: string = '';
    service.getReservationsList().subscribe({
      next: () => fail('should have failed'),
      error: (e) => error = e
    });
    const req = httpMock.expectOne(`${API}`);
    req.flush({ message: 'Quota de réservations atteint.' }, { status: 409, statusText: 'Conflict' });
    expect(error).toBe('Quota de réservations atteint.');
  });

  it('should surface a specific message when the backend is unreachable', () => {
    let error: string = '';
    service.getReservationsList().subscribe({
      next: () => fail('should have failed'),
      error: (e) => error = e
    });
    const req = httpMock.expectOne(`${API}`);
    req.flush(null, { status: 0, statusText: 'Network Error' });
    expect(error).toContain('serveur est injoignable');
  });
});
