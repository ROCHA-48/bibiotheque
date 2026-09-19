import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { Users } from '../_model/users';
import { UsersService } from '../_service/users.service';
import { ModalService } from '../_service/modal.service';
import { ToastService } from '../_service/toast.service';

@Component({
  selector: 'app-register-modal',
  templateUrl: './register-modal.component.html',
  styleUrls: ['./register-modal.component.css']
})
export class RegisterModalComponent implements OnInit, OnDestroy {
  isOpen = false;
  user: Users = new Users();
  selectedRoleName = 'User';
  private sub!: Subscription;

  constructor(
    private usersService: UsersService,
    private modalService: ModalService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.sub = this.modalService.registerModal$.subscribe((isOpen) => {
      this.isOpen = isOpen;
      if (isOpen) {
        this.user = new Users();
        this.selectedRoleName = 'User';
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
    // Construit le rôle à partir de la sélection (évite user.role undefined)
    this.user.role = [{ roleName: this.selectedRoleName }];
    this.usersService.createUser(this.user).subscribe(
      (data) => {
        this.toastService.success('Utilisateur créé avec succès !');
        this.close();
        window.location.reload();
      },
      (error) => {
        this.toastService.error('Erreur lors de la création de l\'utilisateur.');
      }
    );
  }

  onBackdropClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.close();
    }
  }
}
