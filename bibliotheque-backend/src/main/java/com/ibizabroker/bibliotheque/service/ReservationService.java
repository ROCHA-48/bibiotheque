package com.ibizabroker.bibliotheque.service;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.ReservationStatus;
import com.ibizabroker.bibliotheque.entity.Users;
import com.ibizabroker.bibliotheque.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private UsersRepository usersRepository;

    public Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        String username = auth.getName();
        Optional<Users> user = usersRepository.findByUsername(username);
        return user.map(Users::getUserId).orElse(null);
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isOwner(Integer reservationId) {
        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);
        return reservation.isPresent() && currentUserId.equals(reservation.get().getUserId());
    }

    public ResponseEntity<?> getAllReservations(String status) {
        try {
            List<Reservation> reservations;
            Integer currentUserId = getCurrentUserId();

            if (hasRole("BIBLIOTHECAIRE")) {
                if (status != null && !status.isEmpty()) {
                    ReservationStatus reservationStatus = ReservationStatus.valueOf(status);
                    reservations = reservationRepository.findByStatus(reservationStatus);
                } else {
                    reservations = reservationRepository.findAll();
                }
            } else {
                reservations = reservationRepository.findByUserId(currentUserId);
                if (status != null && !status.isEmpty()) {
                    ReservationStatus reservationStatus = ReservationStatus.valueOf(status);
                    reservations.removeIf(r -> r.getStatus() != reservationStatus);
                }
            }

            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Statut invalide : " + status);
            return ResponseEntity.badRequest().body(error);
        }
    }

    public Reservation getReservation(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La réservation demandée n'existe pas."));

        if (hasRole("ADHERENT") && !hasRole("BIBLIOTHECAIRE") && !isOwner(id)) {
            throw new SecurityException("Accès interdit : cette réservation ne vous appartient pas.");
        }

        return reservation;
    }

    public ResponseEntity<?> createReservation(Reservation reservation) {
        if (reservation.getBookId() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Le champ livre est obligatoire.");
            return ResponseEntity.badRequest().body(error);
        }

        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Utilisateur non authentifié.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if (hasRole("ADHERENT") && !hasRole("BIBLIOTHECAIRE")) {
            reservation.setUserId(currentUserId);
        } else if (reservation.getUserId() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Le champ adhérent est obligatoire.");
            return ResponseEntity.badRequest().body(error);
        }

        Books book = booksRepository.findById(reservation.getBookId()).orElse(null);
        if (book == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Le livre demandé n'existe pas.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Users user = usersRepository.findById(reservation.getUserId()).orElse(null);
        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "L'adhérent demandé n'existe pas.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (book.getNoOfCopies() > 0) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Ce livre est disponible, pas besoin de réserver. Vous pouvez l'emprunter directement.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        List<ReservationStatus> activeStatuses = Arrays.asList(ReservationStatus.EN_ATTENTE, ReservationStatus.DISPONIBLE);
        List<Reservation> existingReservations = reservationRepository.findByBookIdAndUserIdAndStatusIn(
                reservation.getBookId(), reservation.getUserId(), activeStatuses);
        if (!existingReservations.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Vous avez déjà une réservation active pour ce livre.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        long activeCount = reservationRepository.countByUserIdAndStatusIn(reservation.getUserId(), activeStatuses);
        if (activeCount >= 3) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Vous avez atteint la quota de 3 réservations actives. Veuillez en annuler une avant d'en créer une nouvelle.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        reservation.setReservationDate(new Date());

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 7);
        reservation.setExpirationDate(c.getTime());

        Reservation saved = reservationRepository.save(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public ResponseEntity<?> annulerReservation(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElse(null);
        if (reservation == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "La réservation demandée n'existe pas.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (hasRole("ADHERENT") && !hasRole("BIBLIOTHECAIRE")) {
            Integer currentUserId = getCurrentUserId();
            Map<String, String> error = new HashMap<>();
            if (currentUserId == null || !currentUserId.equals(reservation.getUserId())) {
                error.put("message", "Accès interdit : cette réservation ne vous appartient pas.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }

        if (reservation.getStatus() != ReservationStatus.EN_ATTENTE && reservation.getStatus() != ReservationStatus.DISPONIBLE) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Impossible d'annuler une réservation avec le statut : " + reservation.getStatus());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        reservation.setStatus(ReservationStatus.ANNULEE);
        Reservation updated = reservationRepository.save(reservation);
        return ResponseEntity.ok(updated);
    }

    public ResponseEntity<?> deleteReservation(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElse(null);
        if (reservation == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "La réservation demandée n'existe pas.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        reservationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
