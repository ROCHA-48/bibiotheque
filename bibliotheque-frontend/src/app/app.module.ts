import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http'
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BooksListComponent } from './books-list/books-list.component';
import { CreateBookComponent } from './create-book/create-book.component';
import { FormsModule } from '@angular/forms';
import { UpdateBookComponent } from './update-book/update-book.component';
import { BookDetailsComponent } from './book-details/book-details.component';
import { BookDetailsModalComponent } from './book-details-modal/book-details-modal.component';
import { RegistrationComponent } from './registration/registration.component';
import { UsersListComponent } from './users-list/users-list.component';
import { UserDetailsComponent } from './user-details/user-details.component';
import { UpdateUserComponent } from './update-user/update-user.component';
import { LoginComponent } from './login/login.component';
import { LogoutComponent } from './logout/logout.component';
import { HeaderComponent } from './header/header.component';
import { HomeComponent } from './home/home.component';
import { BooksService } from './_service/books.service';
import { UsersService } from './_service/users.service';
import { RouterModule } from '@angular/router';
import { AuthGuard } from './_auth/auth.guard';
import { AuthInterceptor } from './_auth/auth.interceptor';
import { ForbiddenComponent } from './forbidden/forbidden.component';
import { BorrowBookComponent } from './borrow-book/borrow-book.component';
import { ReturnBookComponent } from './return-book/return-book.component';
import { ReservationsComponent } from './reservations/reservations.component';
import { ReservationListComponent } from './reservation-list/reservation-list.component';
import { ReservationFormComponent } from './reservation-form/reservation-form.component';
import { ReservationService } from './_service/reservation.service';

// New components
import { SidebarComponent } from './sidebar/sidebar.component';
import { ModalComponent } from './modal/modal.component';
import { ConfirmModalComponent } from './confirm-modal/confirm-modal.component';
import { LoginModalComponent } from './login-modal/login-modal.component';
import { CreateBookModalComponent } from './create-book-modal/create-book-modal.component';
import { RegisterModalComponent } from './register-modal/register-modal.component';
import { ToastComponent } from './toast/toast.component';
import { ToastService } from './_service/toast.service';
import { ModalService } from './_service/modal.service';
import { DashboardComponent } from './dashboard/dashboard.component';
import { NgChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    AppComponent,
    BooksListComponent,
    CreateBookComponent,
    UpdateBookComponent,
    BookDetailsComponent,
    BookDetailsModalComponent,
    RegistrationComponent,
    UsersListComponent,
    UserDetailsComponent,
    UpdateUserComponent,
    LoginComponent,
    LogoutComponent,
    HeaderComponent,
    HomeComponent,
    ForbiddenComponent,
    BorrowBookComponent,
    ReturnBookComponent,
    ReservationsComponent,
    ReservationListComponent,
    ReservationFormComponent,
    // New components
    SidebarComponent,
    ModalComponent,
    ConfirmModalComponent,
    LoginModalComponent,
    CreateBookModalComponent,
    RegisterModalComponent,
    ToastComponent,
    DashboardComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    RouterModule,
    NgChartsModule
  ],
  providers: [
    AuthGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    UsersService,
    BooksService,
    ReservationService,
    ModalService,
    ToastService
   ],
  bootstrap: [AppComponent]
})
export class AppModule { }
