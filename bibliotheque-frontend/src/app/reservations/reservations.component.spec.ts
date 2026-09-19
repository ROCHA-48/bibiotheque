import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ReservationsComponent } from './reservations.component';
import { ReservationListComponent } from '../reservation-list/reservation-list.component';
import { UserAuthService } from '../_service/user-auth.service';
import { Reservation } from '../_model/reservation';

describe('ReservationsComponent', () => {
  let component: ReservationsComponent;
  let fixture: ComponentFixture<ReservationsComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [ReservationsComponent, ReservationListComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    localStorage.clear();
    fixture = TestBed.createComponent(ReservationsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  const makeReservation = (id: number, status: string, userId: number): Reservation => ({
    reservationId: id,
    bookId: 10,
    userId,
    status
  } as any);

  it('should create', () => {
    component.ngOnInit();
    flushInitialCalls();
    expect(component).toBeTruthy();
  });

  it('should count active reservations per user and detect the quota of 3 (RG-03 mirror)', () => {
    component.reservations = [
      makeReservation(1, 'EN_ATTENTE', 5),
      makeReservation(2, 'DISPONIBLE', 5),
      makeReservation(3, 'ANNULEE', 5) // inactive
    ];
    expect(component.getActiveCountForUser(5)).toBe(2);
    expect(component.isUserAtQuota(5)).toBeFalse();

    component.reservations.push(makeReservation(4, 'EN_ATTENTE', 5));
    expect(component.getActiveCountForUser(5)).toBe(3);
    expect(component.isUserAtQuota(5)).toBeTrue();
  });

  it('should reset userId to null for non-admins before sending (RS-04, identity from token)', () => {
    const auth = TestBed.inject(UserAuthService);
    auth.setToken('jwt');
    auth.setRoles([{ roleId: 4, roleName: 'ADHERENT' }] as any);
    auth.setUserId(4);
    component.ngOnInit();
    flushInitialCalls(); // réservations + livres
    // RS-04 côté client : le champ adhérent n'est pas proposé aux non-admins
    expect(component.canChooseAdherent).toBeFalse();

    component.draftReservation = { bookId: 10, userId: 2 } as any; // usurpation tentée
    component.onCreateSubmit();

    const req = httpMock.expectOne('http://localhost:8080/api/reservations');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.userId).toBeNull(); // identité imposée par le backend
    req.flush({});
    // Le succès recharge la liste des réservations
    httpMock.expectOne('http://localhost:8080/api/reservations').flush([]);
  });

  it('should keep the selected adherent for admins/bibliothécaires', () => {
    const auth = TestBed.inject(UserAuthService);
    auth.setToken('jwt');
    auth.setRoles([{ roleId: 1, roleName: 'Admin' }] as any);
    auth.setUserId(1);
    component.ngOnInit();
    flushInitialCalls(); // réservations + livres + utilisateurs

    component.draftReservation = { bookId: 10, userId: 3 } as any;
    component.onCreateSubmit();

    const req = httpMock.expectOne('http://localhost:8080/api/reservations');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.userId).toBe(3);
    req.flush({});
    // Le succès recharge la liste des réservations
    httpMock.expectOne('http://localhost:8080/api/reservations').flush([]);
  });

  /** Consomme les appels HTTP déclenchés par ngOnInit selon le rôle. */
  function flushInitialCalls(): void {
    const resReq = httpMock.expectOne('http://localhost:8080/api/reservations');
    resReq.flush([]);
    const booksReq = httpMock.expectOne('http://localhost:8080/api/reservations/books');
    booksReq.flush([]);
    if (component.canChooseAdherent) {
      const usersReq = httpMock.expectOne('http://localhost:8080/api/reservations/users');
      usersReq.flush([]);
    }
  }
});