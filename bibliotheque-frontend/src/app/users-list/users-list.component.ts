import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Users } from '../_model/users';
import { UsersService } from '../_service/users.service';
import { UserAuthService } from '../_service/user-auth.service';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent implements OnInit {

  users: Users[];

  constructor(private usersService: UsersService,
    private userAuthService: UserAuthService,
    private router: Router) { }

  deleteError = '';

  ngOnInit(): void {
    this.getUsers();
    // this.users = [{
    //   "userId": 1,
    //   "name": "tarun",
    //   "username": "tarungowda",
    //   "role": "STUDENT",
    //   "password": "sdklfjlakdsf"
    // }]
  }

  private getUsers() {
    this.usersService.getUsersList().subscribe(data =>{
      this.users = data;
      console.log(this.users);
    });
  }

  getRoleName(user: Users): string {
    return user?.role?.[0]?.roleName ?? 'sans rôle';
  }

  userDetails(userId: number) {
    this.router.navigate(['user-details', userId ]);
  }

  updateUser(userId: number) {
    this.router.navigate(['update-user', userId ]);
  }

  canDelete(user: Users): boolean {
    // On ne supprime ni les autres admins ni son propre compte
    return this.getRoleName(user) !== 'Admin'
      && user.userId !== this.userAuthService.getUserId();
  }

  deleteUser(user: Users): void {
    this.deleteError = '';
    const confirmed = confirm(`Supprimer définitivement l'utilisateur « ${user.name} » ?\nSes réservations seront également supprimées.`);
    if (!confirmed) return;

    this.usersService.deleteUser(user.userId).subscribe({
      next: () => this.getUsers(),
      error: (err) => this.deleteError = err
    });
  }

}
