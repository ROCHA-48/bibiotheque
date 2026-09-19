import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ReservationListComponent } from './reservation-list.component';
import { Reservation } from '../_model/reservation';
import { UserAuthService } from '../_service/user-auth.service';

describe('ReservationListComponent', () => {
  let component: ReservationListComponent;
  let fixture: ComponentFixture<ReservationListComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [ReservationListComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    localStorage.clear();
    fixture = TestBed.createComponent(ReservationListComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  const makeReservation = (id: number, status: string, userId = 1): Reservation => ({
    reservationId: id,
    bookId: 10,
    userId,
    status,
    reservationDate: new Date('2026-09-10'),
    expirationDate: new Date('2026-09-20')
  } as any);

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the session name for own reservations when the users list is not loaded', () => {
    // Un adhérent ne charge jamais la liste des utilisateurs : sa propre ligne
    // doit quand même afficher son nom (repli identité de session), pas « Inconnu ».
    const auth = TestBed.inject(UserAuthService);
    auth.setUserId(5);
    auth.setName('Adherent Deux');
    component.users = []; // liste vide, comme pour une session ADHERENT
    expect(component.getUserName(5)).toBe('Adherent Deux');
    expect(component.getUserName(99)).toBe('Inconnu'); // autre utilisateur
    expect(component.getUserName(null)).toBe('Inconnu');
  });

  it('should display the user name from the loaded users list', () => {
    component.users = [{ userId: 5, name: 'Adherent Deux' } as any];
    expect(component.getUserName(5)).toBe('Adherent Deux');
  });

  it('should only allow cancelling EN_ATTENTE and DISPONIBLE reservations', () => {
    expect(component.canCancel('EN_ATTENTE')).toBeTrue();
    expect(component.canCancel('DISPONIBLE')).toBeTrue();
    expect(component.canCancel('ANNULEE')).toBeFalse();
    expect(component.canCancel('EXPIREE')).toBeFalse();
    expect(component.canCancel('HONOREE')).toBeFalse();
  });

  it('should display French status labels', () => {
    expect(component.getStatusLabel('EN_ATTENTE')).toBe('En attente');
    expect(component.getStatusLabel('DISPONIBLE')).toBe('Disponible');
    expect(component.getStatusLabel('ANNULEE')).toBe('Annulée');
    expect(component.getStatusLabel('UNKNOWN')).toBe('UNKNOWN');
  });

  it('should paginate to 5 items per page and expose page count', () => {
    component.reservations = Array.from({ length: 12 }, (_, i) => makeReservation(i + 1, 'EN_ATTENTE'));
    // Le getter calcule totalPages de façon paresseuse : on y accède d'abord
    expect(component.paginatedReservations.length).toBe(5);
    expect(component.totalPages).toBe(3);
    expect(component.pageNumbers).toEqual([1, 2, 3]);
  });

  it('should navigate between pages and ignore out-of-range pages', () => {
    component.reservations = Array.from({ length: 12 }, (_, i) => makeReservation(i + 1, 'EN_ATTENTE'));
    component.paginatedReservations; // déclenche le calcul de totalPages = 3
    component.goToPage(2);
    expect(component.currentPage).toBe(2);
    expect(component.paginatedReservations.length).toBe(5);
    component.goToPage(99); // hors bornes : ignoré
    expect(component.currentPage).toBe(2);
    component.goToPage(0); // hors bornes : ignoré
    expect(component.currentPage).toBe(2);
  });

  it('should reset to page 1 when the status filter changes', () => {
    component.reservations = Array.from({ length: 12 }, (_, i) => makeReservation(i + 1, 'EN_ATTENTE'));
    component.paginatedReservations;
    component.goToPage(2);
    component.onFilterChange('ANNULEE');
    expect(component.selectedStatus).toBe('ANNULEE');
    expect(component.currentPage).toBe(1);
  });

  it('should sort reservations by a field with direction icons', () => {
    component.reservations = [
      makeReservation(1, 'EN_ATTENTE'),
      makeReservation(2, 'ANNULEE')
    ];
    component.toggleSort('reservationId');
    expect(component.getSortIcon('reservationId')).toBe('↑');
    expect(component.sortedReservations[0].reservationId).toBe(1);
    component.toggleSort('reservationId');
    expect(component.getSortIcon('reservationId')).toBe('↓');
    expect(component.sortedReservations[0].reservationId).toBe(2);
    expect(component.getSortIcon('otherField')).toBe('↕');
  });

  it('should PATCH /annuler and notify the parent on success', fakeAsync(() => {
    // La native confirm() bloque le navigateur headless : on la stubbe.
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(component.reservationCancelled, 'emit');
    component.reservations = [makeReservation(5, 'EN_ATTENTE')];
    component.paginatedReservations;
    component.annuler(5);
    const req = httpMock.expectOne('http://localhost:8080/api/reservations/5/annuler');
    expect(req.request.method).toBe('PATCH');
    req.flush({});
    tick();
    expect(component.reservationCancelled.emit).toHaveBeenCalled();
  }));
});
