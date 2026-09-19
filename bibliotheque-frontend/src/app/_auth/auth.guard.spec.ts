import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router } from '@angular/router';

import { AuthGuard } from './auth.guard';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let router: Router;
  let userAuthService: UserAuthService;
  let usersService: UsersService;

  const route: any = { data: {} };
  const state: any = { url: '/reservations' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule]
    });
    guard = TestBed.inject(AuthGuard);
    router = TestBed.inject(Router);
    userAuthService = TestBed.inject(UserAuthService);
    usersService = TestBed.inject(UsersService);
    localStorage.clear();
    spyOn(router, 'navigate').and.callFake(async () => true);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should redirect to /login when no token is present', () => {
    spyOn(userAuthService, 'getToken').and.returnValue(null as unknown as string);

    const result = guard.canActivate(route, state);

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should redirect to /forbidden when role does not match', () => {
    spyOn(userAuthService, 'getToken').and.returnValue('jwt-abc');
    spyOn(usersService, 'roleMatch').and.returnValue(false);

    const guardedRoute: any = { data: { roles: ['Admin'] } };
    const result = guard.canActivate(guardedRoute, state);

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/forbidden']);
  });

  it('should allow access when the role matches', () => {
    spyOn(userAuthService, 'getToken').and.returnValue('jwt-abc');
    spyOn(usersService, 'roleMatch').and.returnValue(true);

    const guardedRoute: any = { data: { roles: ['ADHERENT'] } };
    const result = guard.canActivate(guardedRoute, state);

    expect(result).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to /login when the route has no role restriction (token check only decides)', () => {
    // Comportement actuel du guard : un token absent renvoie vers /login même sans rôles requis,
    // et un token présent avec route sans `data.roles` retombe dans le navigate('/login') final.
    spyOn(userAuthService, 'getToken').and.returnValue('jwt-abc');
    spyOn(usersService, 'roleMatch').and.callThrough();

    const result = guard.canActivate(route, state);

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
