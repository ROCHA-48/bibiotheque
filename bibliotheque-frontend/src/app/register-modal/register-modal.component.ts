import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { Users } from '../_model/users';
import { UsersService } from '../_service/users.service';
import { ModalService } from '../_service/modal.service';

@Component({
  selector: 'app-register-modal',
  templateUrl: './register-modal.component.html',
  styleUrls: ['./register-modal.component.css']
})
export class RegisterModalComponent implements OnInit, OnDestroy {
  isOpen = false;
  user: Users = new Users();
  private sub!: Subscription;

  constructor(
    private usersService: UsersService,
    private modalService: ModalService
  ) {}

  ngOnInit() {
    this.sub = this.modalService.registerModal$.subscribe((isOpen) => {
      this.isOpen = isOpen;
      if (isOpen) {
        this.user = new Users();
      }
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  close() {
    this.modalService.closeRegisterModal();
  }

  onSubmit() {
    this.usersService.createUser(this.user).subscribe(
      (data) => {
        console.log(data);
        this.close();
        window.location.reload();
      },
      (error) => console.log(error)
    );
  }

  onBackdropClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.close();
    }
  }
}
