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
import org.mockito.ArgumentCaptor;
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

    @Test
    void shouldRejectReservationWhenBookIsAvailable_RG01() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));

        Books availableBook = new Books();
        availableBook.setBookId(11);
        availableBook.setNoOfCopies(2);
        when(booksRepository.findById(11)).thenReturn(Optional.of(availableBook));

        Reservation newReservation = new Reservation();
        newReservation.setBookId(11);

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldIgnoreUserIdFromRequestBody_RS04() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));
        when(booksRepository.findById(10)).thenReturn(Optional.of(unavailableBook));
        when(reservationRepository.findByBookIdAndUserIdAndStatusIn(eq(10), eq(1), anyList()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(0L);

        Reservation newReservation = new Reservation();
        newReservation.setBookId(10);
        newReservation.setUserId(2); // tentative d'usurpation d'identity

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getUserId()); // l'identité vient du token, pas du corps
    }

    @Test
    void shouldAutoCreateUnknownBookWhenReservingByTitle() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));
        when(booksRepository.findByBookNameIgnoreCase("Batouala")).thenReturn(Optional.empty());
        java.util.concurrent.atomic.AtomicReference<Books> savedBookRef = new java.util.concurrent.atomic.AtomicReference<>();
        when(booksRepository.save(any(Books.class))).thenAnswer(inv -> {
            Books b = inv.getArgument(0);
            b.setBookId(99); // simule la génération d'identifiant par la BD
            savedBookRef.set(b);
            return b;
        });
        when(booksRepository.findById(99)).thenAnswer(inv -> Optional.ofNullable(savedBookRef.get()));
        when(reservationRepository.findByBookIdAndUserIdAndStatusIn(any(), eq(1), anyList()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(0L);

        Reservation newReservation = new Reservation();
        newReservation.setNewBookTitle("Batouala");

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ArgumentCaptor<Books> bookCaptor = ArgumentCaptor.forClass(Books.class);
        verify(booksRepository).save(bookCaptor.capture());
        Books createdBook = bookCaptor.getValue();
        assertEquals("Batouala", createdBook.getBookName());
        assertEquals(0, createdBook.getNoOfCopies());

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertNotNull(captor.getValue().getBookId());
    }

    @Test
    void shouldReuseExistingBookMatchingTitleCaseInsensitively() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));
        Books batouala = new Books();
        batouala.setBookId(42);
        batouala.setBookName("Batouala");
        batouala.setNoOfCopies(0);
        when(booksRepository.findByBookNameIgnoreCase("batouala")).thenReturn(Optional.of(batouala));
        when(booksRepository.findById(42)).thenReturn(Optional.of(batouala));
        when(reservationRepository.findByBookIdAndUserIdAndStatusIn(eq(42), eq(1), anyList()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.countByUserIdAndStatusIn(eq(1), anyList())).thenReturn(0L);

        Reservation newReservation = new Reservation();
        newReservation.setNewBookTitle("batouala");

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(booksRepository, never()).save(any(Books.class)); // pas de doublon
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertEquals(42, captor.getValue().getBookId());
    }

    @Test
    void shouldRejectReservationWithoutBookOrTitle() {
        // Ni bookId ni newBookTitle → 400 avant toute recherche utilisateur
        Reservation newReservation = new Reservation();

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldMarkWaitingReservationsAvailableOnAcquisition() {
        Reservation waiting = new Reservation();
        waiting.setReservationId(7);
        waiting.setBookId(10);
        waiting.setUserId(1);
        waiting.setStatus(ReservationStatus.EN_ATTENTE);
        when(booksRepository.findById(10)).thenReturn(Optional.of(unavailableBook));
        // l'exemplaire vient d'être acquis
        unavailableBook.setNoOfCopies(1);
        when(reservationRepository.findByBookIdAndStatus(10, ReservationStatus.EN_ATTENTE))
                .thenReturn(Collections.singletonList(waiting));

        reservationService.notifyBookAcquired(10);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertEquals(ReservationStatus.DISPONIBLE, captor.getValue().getStatus());
    }

    @Test
    void shouldNotNotifyWhenBookStillHasNoCopy() {
        when(booksRepository.findById(10)).thenReturn(Optional.of(unavailableBook)); // 0 exemplaire

        reservationService.notifyBookAcquired(10);

        verify(reservationRepository, never()).findByBookIdAndStatus(any(), any());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // --- RS-05 : un adhérent ne voit que ses propres réservations ---

    @Test
    void shouldReturnOnlyOwnReservationsForAdherent_RS05() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        Reservation own = new Reservation();
        own.setReservationId(1);
        own.setUserId(1);
        when(reservationRepository.findByUserId(1)).thenReturn(Collections.singletonList(own));

        ResponseEntity<?> response = reservationService.getAllReservations(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Reservation> body = (List<Reservation>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(1, body.get(0).getUserId());
        // RS-05 : jamais findAll pour un adhérent
        verify(reservationRepository, never()).findAll();
    }

    @Test
    void shouldFilterOwnReservationsByStatusForAdherent_RS05() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        Reservation waiting = new Reservation();
        waiting.setReservationId(1);
        waiting.setUserId(1);
        waiting.setStatus(ReservationStatus.EN_ATTENTE);
        Reservation cancelled = new Reservation();
        cancelled.setReservationId(2);
        cancelled.setUserId(1);
        cancelled.setStatus(ReservationStatus.ANNULEE);
        when(reservationRepository.findByUserId(1)).thenReturn(new ArrayList<>(Arrays.asList(waiting, cancelled)));

        ResponseEntity<?> response = reservationService.getAllReservations("EN_ATTENTE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Reservation> body = (List<Reservation>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(ReservationStatus.EN_ATTENTE, body.get(0).getStatus());
    }

    // --- RS-03 : annulation par un non-propriétaire ---

    @Test
    void shouldRefuseCancellationByAnotherAdherent_RS03() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        Reservation other = new Reservation();
        other.setReservationId(5);
        other.setUserId(2); // réservation d'un autre adhérent
        other.setStatus(ReservationStatus.EN_ATTENTE);
        when(reservationRepository.findById(5)).thenReturn(Optional.of(other));

        ResponseEntity<?> response = reservationService.annulerReservation(5);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldAllowOwnerToCancelWaitingReservation() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        Reservation own = new Reservation();
        own.setReservationId(5);
        own.setUserId(1);
        own.setStatus(ReservationStatus.EN_ATTENTE);
        when(reservationRepository.findById(5)).thenReturn(Optional.of(own));

        ResponseEntity<?> response = reservationService.annulerReservation(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertEquals(ReservationStatus.ANNULEE, captor.getValue().getStatus());
    }

    @Test
    void shouldRefuseCancellationOfAlreadyCancelledReservation() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        Reservation own = new Reservation();
        own.setReservationId(5);
        own.setUserId(1);
        own.setStatus(ReservationStatus.ANNULEE);
        when(reservationRepository.findById(5)).thenReturn(Optional.of(own));

        ResponseEntity<?> response = reservationService.annulerReservation(5);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // --- Doublon : une réservation active par livre et par adhérent ---

    @Test
    void shouldRejectDuplicateActiveReservationForSameBook() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));
        when(booksRepository.findById(10)).thenReturn(Optional.of(unavailableBook));
        Reservation existing = new Reservation();
        existing.setReservationId(9);
        existing.setBookId(10);
        existing.setUserId(1);
        existing.setStatus(ReservationStatus.EN_ATTENTE);
        when(reservationRepository.findByBookIdAndUserIdAndStatusIn(eq(10), eq(1), anyList()))
                .thenReturn(Collections.singletonList(existing));

        Reservation newReservation = new Reservation();
        newReservation.setBookId(10);

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // --- RG-01 : réservation d'un livre disponible refusée (cas déjà couvert ci-dessus,
    // ce test documente le message métier attendu) ---

    @Test
    void shouldReturnBusinessMessageWhenBookIsAvailable_RG01() {
        when(usersRepository.findByUsername("adherent1")).thenReturn(Optional.of(adherentUser));
        when(usersRepository.findById(1)).thenReturn(Optional.of(adherentUser));
        Books availableBook = new Books();
        availableBook.setBookId(11);
        availableBook.setNoOfCopies(3);
        when(booksRepository.findById(11)).thenReturn(Optional.of(availableBook));

        Reservation newReservation = new Reservation();
        newReservation.setBookId(11);

        ResponseEntity<?> response = reservationService.createReservation(newReservation);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("disponible"));
    }
}
