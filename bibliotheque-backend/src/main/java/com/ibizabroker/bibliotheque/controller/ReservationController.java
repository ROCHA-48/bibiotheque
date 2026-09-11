package com.ibizabroker.bibliotheque.controller;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.Users;
import com.ibizabroker.bibliotheque.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:4200/")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADHERENT', 'BIBLIOTHECAIRE')")
    public List<Users> getUsersForReservations() {
        return usersRepository.findAll();
    }

    @GetMapping("/books")
    @PreAuthorize("hasAnyRole('ADHERENT', 'BIBLIOTHECAIRE')")
    public List<Books> getBooksForReservations() {
        return booksRepository.findAll();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADHERENT', 'BIBLIOTHECAIRE')")
    public ResponseEntity<?> getAllReservations(@RequestParam(required = false) String status) {
        return reservationService.getAllReservations(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADHERENT', 'BIBLIOTHECAIRE')")
    public ResponseEntity<?> getReservation(@PathVariable Integer id) {
        try {
            Reservation reservation = reservationService.getReservation(id);
            return ResponseEntity.ok(reservation);
        } catch (com.ibizabroker.bibliotheque.exceptions.NotFoundException e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(error);
        } catch (SecurityException e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(403).body(error);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADHERENT', 'BIBLIOTHECAIRE')")
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADHERENT', 'BIBLIOTHECAIRE')")
    public ResponseEntity<?> annulerReservation(@PathVariable Integer id) {
        return reservationService.annulerReservation(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTHECAIRE')")
    public ResponseEntity<?> deleteReservation(@PathVariable Integer id) {
        return reservationService.deleteReservation(id);
    }
}
