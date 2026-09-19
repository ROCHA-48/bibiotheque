import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { Toast, ToastService } from '../_service/toast.service';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.css']
})
export class ToastComponent implements OnInit, OnDestroy {
  toasts: Toast[] = [];
  private sub!: Subscription;
  /** Timers d'auto-suppression, annulables à la destruction du composant. */
  private timers: Map<number, any> = new Map();

  constructor(private toastService: ToastService) {}

  ngOnInit() {
    this.sub = this.toastService.toast$.subscribe((toast) => {
      this.toasts.push(toast);
      const timer = setTimeout(() => {
        this.timers.delete(toast.id);
        this.remove(toast.id);
      }, toast.duration);
      this.timers.set(toast.id, timer);
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
    // Évite les fuites de timers : les tests Karma peuvent sinon rester bloqués ouverts.
    this.timers.forEach(timer => clearTimeout(timer));
    this.timers.clear();
  }

  remove(id: number) {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  getIcon(type: string): string {
    switch (type) {
      case 'success': return '✓';
      case 'error': return '✕';
      case 'warning': return '⚠';
      case 'info': return 'ℹ';
      default: return '✓';
    }
  }
}
