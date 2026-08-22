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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BooksRepository booksRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Books livreIndisponible;

    private Users adherent;

    @BeforeEach
    void setUp() {
        livreIndisponible = new Books();
        livreIndisponible.setBookId(107);
        livreIndisponible.setBookName("la visions");
        livreIndisponible.setNoOfCopies(0);

        adherent = new Users();
        adherent.setUserId(102);
        adherent.setName("Rocha");
    }

    private ReservationRequestDto requeteValide() {
        ReservationRequestDto request = new ReservationRequestDto();
        request.setLivreId(107);
        request.setAdherentId(102);
        return request;
    }

    // RG-03 : un adhérent ne peut pas dépasser 3 réservations actives simultanées.
    @Test
    void rg03_refuseLaQuatriemeReservationActive() {
        when(booksRepository.findById(107)).thenReturn(Optional.of(livreIndisponible));
        when(usersRepository.findById(102)).thenReturn(Optional.of(adherent));
        when(reservationRepository.existsByAdherentAndLivreAndStatutIn(
                eq(adherent), eq(livreIndisponible), anyCollection())).thenReturn(false);
        when(reservationRepository.countByAdherentAndStatutIn(eq(adherent), anyCollection())).thenReturn(3L);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> reservationService.creer(requeteValide()));

        assertTrue(exception.getMessage().contains("[RG-03]"),
                "Le message doit nommer la règle RG-03 : " + exception.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // Cas limite du RG-03 : avec seulement 2 réservations actives, la 3e est acceptée.
    @Test
    void rg03_accepteLaTroisiemeReservationActive() {
        when(booksRepository.findById(107)).thenReturn(Optional.of(livreIndisponible));
        when(usersRepository.findById(102)).thenReturn(Optional.of(adherent));
        when(reservationRepository.existsByAdherentAndLivreAndStatutIn(
                eq(adherent), eq(livreIndisponible), anyCollection())).thenReturn(false);
        when(reservationRepository.countByAdherentAndStatutIn(eq(adherent), anyCollection())).thenReturn(2L);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Date avant = new Date();
        ReservationResponseDto resultat = reservationService.creer(requeteValide());

        assertNotNull(resultat);
        assertEquals(StatutReservation.EN_ATTENTE, resultat.getStatut());

        // RG-04 : dateExpiration = dateReservation + 7 jours.
        Calendar calendrier = Calendar.getInstance();
        calendrier.setTime(avant);
        calendrier.add(Calendar.DATE, 7);
        Date attendu = calendrier.getTime();
        long ecart = Math.abs(resultat.getDateExpiration().getTime() - attendu.getTime());
        assertTrue(ecart < 5000, "dateExpiration doit valoir dateReservation + 7 jours");
    }
}
