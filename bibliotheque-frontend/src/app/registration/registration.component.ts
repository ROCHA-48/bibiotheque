import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Users } from '../_model/users';
import { UsersService } from '../_service/users.service';
import { ToastService } from '../_service/toast.service';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit {

  user: Users = new Users();
  selectedRoleName = 'User';

  constructor(private usersService: UsersService,
    private router: Router,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
  }

  saveUser() {
    this.user.role = [{ roleName: this.selectedRoleName }];
    this.usersService.createUser(this.user).subscribe(data => {
      this.toastService.success('Utilisateur créé avec succès !');
      this.goToUsersList();
    },
    error => {
      this.toastService.error('Erreur lors de la création de l\'utilisateur.');
    });
  }

  goToUsersList() {
    this.router.navigate(['/users']);
  }

  onSubmit() {
    console.log(this.user);
    this.saveUser();
  }

}
