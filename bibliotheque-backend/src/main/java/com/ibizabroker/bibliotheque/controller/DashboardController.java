package com.ibizabroker.bibliotheque.controller;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.BorrowRepository;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Borrow;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.ReservationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin("http://localhost:4200/")
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Nombre total de livres
        stats.put("totalBooks", booksRepository.count());

        // Nombre total d'utilisateurs
        stats.put("totalUsers", usersRepository.count());

        // Emprunts en cours (non retournés)
        List<?> activeBorrows = borrowRepository.findByReturnDateIsNull();
        stats.put("activeBorrows", activeBorrows.size());

        // Réservations en attente
        List<?> pendingReservations = reservationRepository.findByStatus(ReservationStatus.EN_ATTENTE);
        stats.put("pendingReservations", pendingReservations.size());

        // Réservations disponibles à récupérer
        List<?> availableReservations = reservationRepository.findByStatus(ReservationStatus.DISPONIBLE);
        stats.put("availableReservations", availableReservations.size());

        // Réservations honorées
        List<?> honoredReservations = reservationRepository.findByStatus(ReservationStatus.HONOREE);
        stats.put("honoredReservations", honoredReservations.size());

        // Livres empruntés (total)
        List<?> allBorrows = borrowRepository.findAll();
        stats.put("totalBorrows", allBorrows.size());

        return stats;
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> getRecentActivity() {
        Map<String, Object> activity = new HashMap<>();

        // Derniers emprunts
        List<?> allBorrows = borrowRepository.findAll();
        if (allBorrows.size() > 5) {
            activity.put("recentBorrows", allBorrows.subList(allBorrows.size() - 5, allBorrows.size()));
        } else {
            activity.put("recentBorrows", allBorrows);
        }

        // Dernières réservations
        List<?> allReservations = reservationRepository.findAll();
        if (allReservations.size() > 5) {
            activity.put("recentReservations", allReservations.subList(allReservations.size() - 5, allReservations.size()));
        } else {
            activity.put("recentReservations", allReservations);
        }

        return activity;
    }

    @GetMapping("/chart/borrows-by-month")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> getBorrowsByMonth() {
        Map<String, Object> chartData = new HashMap<>();
        List<Borrow> allBorrows = borrowRepository.findAll();
        
        // Grouper par mois (6 derniers mois)
        Map<String, Integer> monthlyBorrows = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.FRENCH);
        
        // Initialiser les 6 derniers mois
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            Calendar tempCal = Calendar.getInstance();
            tempCal.add(Calendar.MONTH, -i);
            String monthKey = monthFormat.format(tempCal.getTime());
            monthlyBorrows.put(monthKey, 0);
        }
        
        // Compter les emprunts par mois
        for (Borrow borrow : allBorrows) {
            if (borrow.getIssueDate() != null) {
                String monthKey = monthFormat.format(borrow.getIssueDate());
                if (monthlyBorrows.containsKey(monthKey)) {
                    monthlyBorrows.put(monthKey, monthlyBorrows.get(monthKey) + 1);
                }
            }
        }
        
        chartData.put("labels", new ArrayList<>(monthlyBorrows.keySet()));
        chartData.put("data", new ArrayList<>(monthlyBorrows.values()));
        chartData.put("label", "Emprunts");
        
        return chartData;
    }

    @GetMapping("/chart/reservation-status")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> getReservationStatusDistribution() {
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        
        // Couleurs pour chaque statut
        Map<ReservationStatus, String> statusColors = new HashMap<>();
        statusColors.put(ReservationStatus.EN_ATTENTE, "#ffc107"); // Jaune
        statusColors.put(ReservationStatus.DISPONIBLE, "#28a745");  // Vert
        statusColors.put(ReservationStatus.HONOREE, "#6f42c1");   // Violet
        statusColors.put(ReservationStatus.ANNULEE, "#dc3545");   // Rouge
        statusColors.put(ReservationStatus.EXPIREE, "#6c757d");   // Gris
        
        for (ReservationStatus status : ReservationStatus.values()) {
            long count = reservationRepository.findByStatus(status).size();
            if (count > 0) {
                labels.add(status.name().replace("_", " "));
                data.add(count);
                colors.add(statusColors.getOrDefault(status, "#007bff"));
            }
        }
        
        chartData.put("labels", labels);
        chartData.put("data", data);
        chartData.put("colors", colors);
        chartData.put("label", "Réservations");
        
        return chartData;
    }

    @GetMapping("/chart/books-by-genre")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> getBooksByGenre() {
        Map<String, Object> chartData = new HashMap<>();
        List<Books> allBooks = booksRepository.findAll();
        
        // Grouper par genre
        Map<String, Integer> genreCount = new LinkedHashMap<>();
        for (Books book : allBooks) {
            String genre = book.getBookGenre();
            if (genre != null && !genre.isEmpty()) {
                genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
            }
        }
        
        // Trier par nombre décroissant
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(genreCount.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        
        // Palette de couleurs
        String[] palette = {"#4f46e5", "#06b6d4", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#ec4899", "#14b8a6"};
        int colorIndex = 0;
        
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            labels.add(entry.getKey());
            data.add(entry.getValue());
            colors.add(palette[colorIndex % palette.length]);
            colorIndex++;
        }
        
        chartData.put("labels", labels);
        chartData.put("data", data);
        chartData.put("colors", colors);
        chartData.put("label", "Livres par genre");
        
        return chartData;
    }
}
