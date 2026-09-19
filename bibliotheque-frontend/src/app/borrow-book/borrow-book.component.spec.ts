import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { BorrowBookComponent } from './borrow-book.component';
import { UserAuthService } from '../_service/user-auth.service';

describe('BorrowBookComponent', () => {
  let component: BorrowBookComponent;
  let fixture: ComponentFixture<BorrowBookComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [BorrowBookComponent]
    }).compileComponents();

    // Identité de session avant le premier ngOnInit
    const auth = TestBed.inject(UserAuthService);
    spyOn(auth, 'getUserId').and.returnValue(1);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BorrowBookComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    component.ngOnInit();
    httpMock.expectOne('http://localhost:8080/admin/books').flush([]);
    expect(component).toBeTruthy();
  });

  it('should only list books with at least one copy (reservation-created books excluded)', () => {
    component.ngOnInit();
    httpMock.expectOne('http://localhost:8080/admin/books').flush([
      { bookId: 1, bookName: 'Candide', noOfCopies: 3 },
      { bookId: 2, bookName: 'Test Book', noOfCopies: 0 },
      { bookId: 3, bookName: 'L\'Étranger', noOfCopies: 1 }
    ]);

    expect(component.books.length).toBe(3);
    expect(component.availableBooks.length).toBe(2);
    expect(component.availableBooks.map(b => b.bookName)).toEqual(['Candide', 'L\'Étranger']);
  });

  it('should POST the borrow with bookId and the identity from the session, then reload', () => {
    component.ngOnInit();
    httpMock.expectOne('http://localhost:8080/admin/books').flush([]);

    component.borrowBook(4);
    const req = httpMock.expectOne('http://localhost:8080/borrow');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.bookId).toBe(4);
    expect(req.request.body.userId).toBe(1); // identité de session
    req.flush('ok');

    // Le succès déclenche un rechargement de la liste des livres
    httpMock.expectOne('http://localhost:8080/admin/books').flush([
      { bookId: 4, bookName: 'Candide', noOfCopies: 2 }
    ]);
    expect(component.availableBooks.length).toBe(1);
  });
});
