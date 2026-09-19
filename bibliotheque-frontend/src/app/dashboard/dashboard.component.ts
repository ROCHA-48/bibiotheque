import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserAuthService } from '../_service/user-auth.service';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stats: any = {};
  recentActivity: any = {};
  loading = true;
  error = false;

  // Chart data
  borrowsByMonthChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  reservationStatusChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  booksByGenreChartData: ChartData<'pie'> = { labels: [], datasets: [] };

  // Chart options
  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, position: 'top' },
      title: { display: false }
    },
    scales: {
      y: { beginAtZero: true, ticks: { stepSize: 1 } }
    }
  };

  doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, position: 'right' }
    }
  };

  pieChartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, position: 'right' }
    }
  };

  constructor(
    private http: HttpClient,
    private userAuthService: UserAuthService
  ) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadRecentActivity();
    this.loadChartData();
  }

  loadStats(): void {
    this.http.get('http://localhost:8080/admin/dashboard/stats')
      .subscribe({
        next: (data: any) => {
          this.stats = data;
        },
        error: (err) => {
          console.error('Erreur lors du chargement des statistiques:', err);
          this.error = true;
        }
      });
  }

  loadRecentActivity(): void {
    this.http.get('http://localhost:8080/admin/dashboard/recent-activity')
      .subscribe({
        next: (data: any) => {
          this.recentActivity = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Erreur lors du chargement de l\'activité:', err);
          this.loading = false;
        }
      });
  }

  loadChartData(): void {
    // Borrows by month
    this.http.get('http://localhost:8080/admin/dashboard/chart/borrows-by-month')
      .subscribe({
        next: (data: any) => {
          this.borrowsByMonthChartData = {
            labels: data.labels,
            datasets: [{
              data: data.data,
              label: data.label,
              backgroundColor: 'rgba(79, 70, 229, 0.7)',
              borderColor: 'rgba(79, 70, 229, 1)',
              borderWidth: 1
            }]
          };
        },
        error: (err) => console.error('Erreur chart emprunts:', err)
      });

    // Reservation status distribution
    this.http.get('http://localhost:8080/admin/dashboard/chart/reservation-status')
      .subscribe({
        next: (data: any) => {
          this.reservationStatusChartData = {
            labels: data.labels,
            datasets: [{
              data: data.data,
              backgroundColor: data.colors,
              borderWidth: 1
            }]
          };
        },
        error: (err) => console.error('Erreur chart réservations:', err)
      });

    // Books by genre
    this.http.get('http://localhost:8080/admin/dashboard/chart/books-by-genre')
      .subscribe({
        next: (data: any) => {
          this.booksByGenreChartData = {
            labels: data.labels,
            datasets: [{
              data: data.data,
              backgroundColor: data.colors,
              borderWidth: 1
            }]
          };
        },
        error: (err) => console.error('Erreur chart genres:', err)
      });
  }

  isLoggedIn(): boolean {
    return !!this.userAuthService.isLoggedIn();
  }

  isAdmin(): boolean {
    const roles = this.userAuthService.getRoles();
    return roles && roles.some((role: any) => role.roleName === 'Admin' || role === 'Admin');
  }
}
