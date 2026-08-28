import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';
import { ModalService } from '../_service/modal.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  @Output() collapsedChange = new EventEmitter<boolean>();
  isCollapsed = false;

  constructor(
    private userAuthService: UserAuthService,
    private router: Router,
    public userService: UsersService,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {}

  // Méthode getter pour obtenir le nom de l'utilisateur
  getUserName(): string {
    return this.userAuthService.getName();
  }

  toggleSidebar() {
    this.isCollapsed = !this.isCollapsed;
    this.collapsedChange.emit(this.isCollapsed);
  }

  isLoggedIn() {
    return this.userAuthService.isLoggedIn();
  }

  isAdmin() {
    return this.userService.roleMatch(['Admin']);
  }

  isUser() {
    return this.userService.roleMatch(['User']);
  }

  openLoginModal() {
    this.modalService.openLoginModal();
  }

  openCreateBookModal() {
    this.modalService.openCreateBookModal();
  }

  openRegisterModal() {
    this.modalService.openRegisterModal();
  }

  logout() {
    this.userAuthService.clear();
    this.router.navigate(['/']);
  }

  isActive(route: string): boolean {
    return this.router.url === route;
  }
}
