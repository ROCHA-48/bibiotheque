import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { UserAuthService } from './user-auth.service';

describe('UserAuthService', () => {
  let service: UserAuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(UserAuthService);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should store and retrieve the JWT token', () => {
    service.setToken('jwt-abc');
    expect(service.getToken()).toBe('jwt-abc');
  });

  it('should store and retrieve roles', () => {
    const roles = [{ roleId: 4, roleName: 'ADHERENT' }];
    service.setRoles(roles as any);
    expect(service.getRoles()).toEqual(roles as any);
  });

  it('should store and retrieve the userId', () => {
    service.setUserId(42);
    expect(service.getUserId()).toBe(42);
  });

  it('should store and retrieve the user name', () => {
    service.setName('Adherent Deux');
    expect(service.getName()).toBe('Adherent Deux');
    expect(service.getName()).not.toBe('42');
  });

  it('should be logged in only when token AND roles are present', () => {
    service.setToken('jwt-abc');
    expect(service.isLoggedIn()).toBeFalsy(); // token sans rôles
    service.setRoles([{ roleId: 1, roleName: 'Admin' }] as any);
    expect(service.isLoggedIn()).toBeTruthy();
  });

  it('should clear all auth data on logout', () => {
    service.setToken('jwt-abc');
    service.setRoles([{ roleId: 1, roleName: 'Admin' }] as any);
    service.setUserId(1);
    service.clear();
    expect(service.getToken()).toBeNull();
    expect(service.getRoles()).toBeNull();
    expect(service.isLoggedIn()).toBeFalsy();
  });
});
