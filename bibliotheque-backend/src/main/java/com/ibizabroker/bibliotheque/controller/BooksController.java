package com.ibizabroker.bibliotheque.controller;

import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.exceptions.NotFoundException;
import com.ibizabroker.bibliotheque.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200/")
@RestController
@RequestMapping("/admin")
public class BooksController {

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/books")
    public List<Books> getAllBooks(){
        return booksRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('Admin','BIBLIOTHECAIRE')")
    @GetMapping("/books/{id}")
    public ResponseEntity<Books> getBookById(@PathVariable Integer id) {
        Books book = booksRepository.findById(id).orElseThrow(() -> new NotFoundException("Book with id "+ id +" does not exist."));
        return ResponseEntity.ok(book);
    }

    @PreAuthorize("hasRole('Admin')")
    @PostMapping("/books")
    public Books createBook(@RequestBody Books book) {
        Books saved = booksRepository.save(book);
        // Si le livre arrive avec des exemplaires, les réservataires en attente sont notifiés.
        reservationService.notifyBookAcquired(saved.getBookId());
        return saved;
    }

    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/books/{id}")
    public ResponseEntity<Books> updateBook(@PathVariable Integer id, @RequestBody Books bookDetails) {
        Books book = booksRepository.findById(id).orElseThrow(() -> new NotFoundException("Book with id "+ id +" does not exist."));

        book.setBookName(bookDetails.getBookName());
        book.setBookAuthor(bookDetails.getBookAuthor());
        book.setBookGenre(bookDetails.getBookGenre());
        book.setNoOfCopies(bookDetails.getNoOfCopies());

        Books updatedBook = booksRepository.save(book);
        // Acquisition d'exemplaires : les réservations EN_ATTENTE passent à DISPONIBLE.
        reservationService.notifyBookAcquired(updatedBook.getBookId());
        return ResponseEntity.ok(updatedBook);
    }

    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteBook(@PathVariable Integer id) {
        Books book = booksRepository.findById(id).orElseThrow(() -> new NotFoundException("Book with id "+ id +" does not exist."));

        booksRepository.delete(book);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}
