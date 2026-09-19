import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ReturnBookComponent } from './return-book.component';
import { UserAuthService } from '../_service/user-auth.service';

describe('ReturnBookComponent', () => {
  let component: ReturnBookComponent;
  let fixture: ComponentFixture<ReturnBookComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [ReturnBookComponent]
    }).compileComponents();

    const auth = TestBed.inject(UserAuthService);
    spyOn(auth, 'getUserId').and.returnValue(4);
  });

  beforeEach(() => {
    localStorage.clear();
    fixture = TestBed.createComponent(ReturnBookComponent);
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
    httpMock.expectOne('http://localhost:8080/borrow/user/4').flush([]);
    expect(component).toBeTruthy();
  });

  it('should load the borrows of the session user only', () => {
    component.ngOnInit();
    httpMock.expectOne('http://localhost:8080/admin/books').flush([]);
    const req = httpMock.expectOne('http://localhost:8080/borrow/user/4');
    expect(req.request.method).toBe('GET');
    req.flush([
      { borrowId: 1, bookId: 2, userId: 4, returnDate: null }
    ]);
    expect(component.borrow.length).toBe(1);
  });

  it('should PUT the return and reload the list', () => {
    component.ngOnInit();
    httpMock.expectOne('http://localhost:8080/admin/books').flush([]);
    httpMock.expectOne('http://localhost:8080/borrow/user/4').flush([
      { borrowId: 9, bookId: 2, userId: 4, returnDate: null }
    ]);

    component.returnBook(9);
    const req = httpMock.expectOne('http://localhost:8080/borrow');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.borrowId).toBe(9);
    req.flush({});
    // rechargement après retour
    httpMock.expectOne('http://localhost:8080/borrow/user/4').flush([]);
  });
});
