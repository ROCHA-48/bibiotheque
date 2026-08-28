import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';
import { ModalService } from '../_service/modal.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  currentPage = '';

  constructor(
    private userAuthService: UserAuthService,
    private router: Router,
    public userService: UsersService,
    private modalService: ModalService
  ) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentPage = event.urlAfterRedirects || event.url;
    });
  }

  ngOnInit(): void {
    this.currentPage = this.router.url;
  }

  getPageTitle(): string {
    const titles: { [key: string]: string } = {
      '/': 'Accueil',
      '/books': '📚 Liste des livres',
      '/create-book': '➕ Ajouter un livre',
      '/users': '👥 Utilisateurs',
      '/register-user': '👤 Ajouter un utilisateur',
      '/borrow-book': '📖 Emprunter',
      '/return-book': '📤 Retourner',
      '/reservations': '📅 Réservations',
      '/login': '🔐 Connexion',
      '/forbidden': '🚫 Accès interdit'
    };
    
    if (this.currentPage.startsWith('/update-book')) return '✏️ Modifier le livre';
    if (this.currentPage.startsWith('/book-details')) return '📖 Détails du livre';
    if (this.currentPage.startsWith('/user-details')) return '👤 Détails de l\'utilisateur';
    if (this.currentPage.startsWith('/update-user')) return '✏️ Modifier l\'utilisateur';
    
    return titles[this.currentPage] || 'Bibliothèque';
  }

  isLoggedIn() {
    return this.userAuthService.isLoggedIn();
  }

  public logout() {
    this.userAuthService.clear();
    this.router.navigate(['/']);
  }
}
