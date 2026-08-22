package com.ibizabroker.bibliotheque.service;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.dto.ReservationRequestDto;
import com.ibizabroker.bibliotheque.dto.ReservationResponseDto;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.StatutReservation;
import com.ibizabroker.bibliotheque.entity.Users;
import com.ibizabroker.bibliotheque.exceptions.ConflictException;
import com.ibizabroker.bibliotheque.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ReservationService {

    private static final int DUREE_RESERVATION_JOURS = 7;
    private static final int MAX_RESERVATIONS_ACTIVES = 3;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private UsersRepository usersRepository;

    public ReservationResponseDto creer(ReservationRequestDto request) {
        Books livre = booksRepository.findById(request.getLivreId())
                .orElseThrow(() -> new NotFoundException("Livre introuvable : id " + request.getLivreId()));
        Users adherent = usersRepository.findById(request.getAdherentId())
                .orElseThrow(() -> new NotFoundException("Adhérent introuvable : id " + request.getAdherentId()));

        // RG-01 : on ne peut réserver qu'un livre indisponible.
        if (livre.getNoOfCopies() >= 1) {
            throw new ConflictException(
                    "[RG-01] Le livre \"" + livre.getBookName() + "\" est disponible : il doit être emprunté directement, pas réservé.");
        }

        // RG-02 : une seule réservation active par adhérent et par livre.
        List<StatutReservation> statutsActifs = Arrays.asList(StatutReservation.EN_ATTENTE, StatutReservation.DISPONIBLE);
        if (reservationRepository.existsByAdherentAndLivreAndStatutIn(adherent, livre, statutsActifs)) {
            throw new ConflictException(
                    "[RG-02] L'adhérent " + adherent.getName() + " a déjà une réservation active sur le livre \"" + livre.getBookName() + "\".");
        }

        // RG-03 : au maximum 3 réservations actives simultanées par adhérent.
        long reservationsActives = reservationRepository.countByAdherentAndStatutIn(adherent, statutsActifs);
        if (reservationsActives >= MAX_RESERVATIONS_ACTIVES) {
            throw new ConflictException(
                    "[RG-03] L'adhérent " + adherent.getName() + " a déjà " + reservationsActives
                            + " réservations actives : la limite de " + MAX_RESERVATIONS_ACTIVES + " est atteinte.");
        }

        Reservation reservation = new Reservation();
        reservation.setLivre(livre);
        reservation.setAdherent(adherent);
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        // RG-04 : dateExpiration = dateReservation + 7 jours, calculée côté serveur.
        Date dateReservation = new Date();
        Calendar calendrier = Calendar.getInstance();
        calendrier.setTime(dateReservation);
        calendrier.add(Calendar.DATE, DUREE_RESERVATION_JOURS);
        reservation.setDateReservation(dateReservation);
        reservation.setDateExpiration(calendrier.getTime());

        return ReservationResponseDto.fromEntity(reservationRepository.save(reservation));
    }

    public List<ReservationResponseDto> lister(StatutReservation statut, Integer adherentId) {
        if (adherentId != null && !usersRepository.existsById(adherentId)) {
            throw new NotFoundException("Adhérent introuvable : id " + adherentId);
        }

        if (statut != null && adherentId != null) {
            return toDtoList(reservationRepository.findByAdherentUserIdAndStatut(adherentId, statut));
        }
        if (statut != null) {
            return toDtoList(reservationRepository.findByStatut(statut));
        }
        if (adherentId != null) {
            return toDtoList(reservationRepository.findByAdherentUserId(adherentId));
        }
        return toDtoList(reservationRepository.findAll());
    }

    public ReservationResponseDto consulter(Integer id) {
        return ReservationResponseDto.fromEntity(getReservation(id));
    }

    // RG-05 / RG-06 : annulation possible uniquement si EN_ATTENTE ou DISPONIBLE,
    // une réservation ANNULEE, EXPIREE ou HONOREE ne peut plus changer d'état.
    public ReservationResponseDto annuler(Integer id) {
        Reservation reservation = getReservation(id);
        if (!reservation.getStatut().isActive()) {
            throw new ConflictException(
                    "[RG-05] Seules les réservations EN_ATTENTE ou DISPONIBLE peuvent être annulées [RG-06] : cette réservation est " + reservation.getStatut() + ".");
        }
        reservation.setStatut(StatutReservation.ANNULEE);
        return ReservationResponseDto.fromEntity(reservationRepository.save(reservation));
    }

    public void supprimer(Integer id) {
        Reservation reservation = getReservation(id);
        reservationRepository.delete(reservation);
    }

    // Fait passer en EXPIREE les réservations actives dont la dateExpiration est dépassée.
    // RG-06 : les statuts terminaux (ANNULEE, EXPIREE, HONOREE) ne changent plus.
    @Scheduled(cron = "0 0 * * * *")
    public int expirerReservationsPerimees() {
        List<Reservation> perimees = reservationRepository.findByStatutInAndDateExpirationBefore(
                Arrays.asList(StatutReservation.EN_ATTENTE, StatutReservation.DISPONIBLE), new Date());
        for (Reservation reservation : perimees) {
            reservation.setStatut(StatutReservation.EXPIREE);
        }
        if (!perimees.isEmpty()) {
            reservationRepository.saveAll(perimees);
        }
        return perimees.size();
    }

    public List<ReservationResponseDto> listerExpirees() {
        expirerReservationsPerimees();
        return toDtoList(reservationRepository.findByStatut(StatutReservation.EXPIREE));
    }

    private Reservation getReservation(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation introuvable : id " + id));
    }

    private List<ReservationResponseDto> toDtoList(List<Reservation> reservations) {
        List<ReservationResponseDto> dtos = new ArrayList<>(reservations.size());
        for (Reservation reservation : reservations) {
            dtos.add(ReservationResponseDto.fromEntity(reservation));
        }
        return dtos;
    }
}
