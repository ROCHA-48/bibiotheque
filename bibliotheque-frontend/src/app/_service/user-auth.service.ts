import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserAuthService {

  constructor() { }

  public setRoles(roles: []) {
    localStorage.setItem('roles', JSON.stringify(roles));
  }

  public getRoles(): [] {
    return JSON.parse(localStorage.getItem('roles')!);
  }

  public setToken(jwtToken: string) {
    localStorage.setItem('jwtToken', jwtToken);
  }

  public getToken(): string {
    return localStorage.getItem('jwtToken')!;
  }

  public setUserId(userId: number) {
    localStorage.setItem('userId', JSON.stringify(userId));
  }

  public getUserId() {
    return JSON.parse(localStorage.getItem('userId')!);
  }

  public setName(name: string) {
    localStorage.setItem('name', JSON.stringify(name));
  }

  public getName(): string | null {
    try {
      const value = JSON.parse(localStorage.getItem('name')!);
      return typeof value === 'string' ? value : null;
    } catch {
      return null;
    }
  }

  public clear() {
    localStorage.clear();
  }

  public isLoggedIn() {
    return this.getRoles() && this.getToken();
  }

}