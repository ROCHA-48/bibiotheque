import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';
import { ToastService } from '../_service/toast.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  constructor(private userService: UsersService,
    private userAuthSerivce: UserAuthService,
    private router: Router,
    private toastService: ToastService
  ) { }

  ngOnInit() {
  }

  login(loginForm: NgForm) {
    this.userService.login(loginForm.value).subscribe(
      (response: any)=>{
        this.userAuthSerivce.setRoles(response.user.role);
        this.userAuthSerivce.setToken(response.jwtToken);
        this.userAuthSerivce.setUserId(response.user.userId);
        this.userAuthSerivce.setName(response.user.name);

        this.toastService.success('Connexion réussie ! Bienvenue ' + response.user.name);
        const role = response.user.role[0].roleName;
        if(role === 'Admin') {
          this.router.navigate(['/books']);
        } else if(role === 'BIBLIOTHECAIRE') {
          this.router.navigate(['/reservations']);
        } else {
          this.router.navigate(['/borrow-book']) //update later
        }
      },
      (error)=>{
        this.toastService.error('Identifiants incorrects. Veuillez réessayer.');
      }
    );
  }

}