import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';
import { ModalService } from '../_service/modal.service';

@Component({
  selector: 'app-login-modal',
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.css']
})
export class LoginModalComponent implements OnInit, OnDestroy {
  isOpen = false;
  private sub!: Subscription;

  constructor(
    private userService: UsersService,
    private userAuthService: UserAuthService,
    private router: Router,
    private modalService: ModalService
  ) {}

  ngOnInit() {
    this.sub = this.modalService.loginModal$.subscribe((isOpen) => {
      this.isOpen = isOpen;
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  close() {
    this.modalService.closeLoginModal();
  }

  login(loginForm: NgForm) {
    this.userService.login(loginForm.value).subscribe(
      (response: any) => {
        this.userAuthService.setRoles(response.user.role);
        this.userAuthService.setToken(response.jwtToken);
        this.userAuthService.setUserId(response.user.userId);
        this.userAuthService.setName(response.user.name);

        const role = response.user.role[0].roleName;
        this.close();
        if (role === 'Admin') {
          this.router.navigate(['/books']);
        } else {
          this.router.navigate(['/borrow-book']);
        }
      },
      (error) => {
        console.log(error);
      }
    );
  }

  onBackdropClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.close();
    }
  }
}
