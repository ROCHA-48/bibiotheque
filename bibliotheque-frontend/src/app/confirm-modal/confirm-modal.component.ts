import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ModalService, ConfirmModalData } from '../_service/modal.service';

@Component({
  selector: 'app-confirm-modal',
  templateUrl: './confirm-modal.component.html',
  styleUrls: ['./confirm-modal.component.css']
})
export class ConfirmModalComponent implements OnInit, OnDestroy {
  isOpen = false;
  data: ConfirmModalData | null = null;
  private sub!: Subscription;

  constructor(private modalService: ModalService) {}

  ngOnInit() {
    this.sub = this.modalService.confirmModal$.subscribe((data) => {
      this.data = data;
      this.isOpen = data !== null;
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  onConfirm() {
    this.modalService.confirmAccept();
  }

  onCancel() {
    this.modalService.confirmCancel();
  }

  onBackdropClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.onCancel();
    }
  }

  getIcon(): string {
    switch (this.data?.type) {
      case 'danger': return 'bi-exclamation-triangle';
      case 'warning': return 'bi-exclamation-circle';
      default: return 'bi-info-circle';
    }
  }
}
