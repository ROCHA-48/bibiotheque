package com.ibizabroker.bibliotheque.service;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.ReservationStatus;
import com.ibizabroker.bibliotheque.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceUnitTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BooksRepository booksRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Users adherentUser;
    private Books unavailableBook;

    @BeforeEach
    void setUp() {
        adherentUser = new Users();
        adherentUser.setUserId(1);
        adherentUser.setUsername("adherent1");
        adherentUser.setName("Alice Dupont");

        unavailableBook = new Books();
        unavailableBook.setBookId(10);
        unavailableBook.setNoOfCopies(0);

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADHERENT"));
        Authentication auth = new UsernamePasswordAuthenticationToken("adherent1", null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldAllowThirdReservationWhenUserHasTwoActiveReservations() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));
        when(booksRepository.findById(10)).thenReturn(Optional.of(unavailableBook));
        when(reservationRepository.findByBookIdAndUserIdAndStatusIn(eq(10), eq(1), anyList()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(2L);

        Reservation newReservation = new Reservation();
        newReservation.setBookId(10);

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void shouldRejectFourthReservationWhenUserHasThreeActiveReservations() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));
        when(booksRepository.findById(10)).thenReturn(Optional.of(unavailableBook));
        when(reservationRepository.findByBookIdAndUserIdAndStatusIn(eq(10), eq(1), anyList()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(3L);

        Reservation newReservation = new Reservation();
        newReservation.setBookId(10);

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
