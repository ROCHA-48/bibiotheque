package com.ibizabroker.bibliotheque.controller;

import com.ibizabroker.bibliotheque.dto.ReservationRequestDto;
import com.ibizabroker.bibliotheque.dto.ReservationResponseDto;
import com.ibizabroker.bibliotheque.entity.StatutReservation;
import com.ibizabroker.bibliotheque.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@SecurityRequirement(name = "Bearer Authentication")
public class ReservationController {
    // ... le reste du code
   @Autowired
    private ReservationService reservationService;

    @Operation(summary = "Créer une réservation sur un livre indisponible")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réservation créée"),
            @ApiResponse(responseCode = "400", description = "livreId ou adherentId manquant"),
            @ApiResponse(responseCode = "404", description = "Livre ou adhérent inconnu"),
            @ApiResponse(responseCode = "409", description = "Règle de gestion violée (RG-01, RG-02, RG-03)")
    })
    @PostMapping
    public ResponseEntity<ReservationResponseDto> creer(@Valid @RequestBody ReservationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.creer(request));
    }

    @Operation(summary = "Lister les réservations, filtrable par statut et par adhérent")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réservations"),
            @ApiResponse(responseCode = "400", description = "Statut ou paramètre invalide"),
            @ApiResponse(responseCode = "404", description = "Adhérent inconnu")
    })
    @GetMapping
    public List<ReservationResponseDto> lister(
            @RequestParam(required = false) StatutReservation statut,
            @RequestParam(required = false) Integer adherentId) {
        return reservationService.lister(statut, adherentId);
    }

    @Operation(summary = "Lister les réservations expirées (dateExpiration dépassée)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réservations expirées")
    })
    @GetMapping("/expirees")
    public List<ReservationResponseDto> listerExpirees() {
        return reservationService.listerExpirees();
    }

    @Operation(summary = "Consulter une réservation par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation trouvée"),
            @ApiResponse(responseCode = "404", description = "Réservation inconnue")
    })
    @GetMapping("/{id}")
    public ReservationResponseDto consulter(@PathVariable Integer id) {
        return reservationService.consulter(id);
    }

    @Operation(summary = "Annuler une réservation (possible si EN_ATTENTE ou DISPONIBLE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation annulée"),
            @ApiResponse(responseCode = "404", description = "Réservation inconnue"),
            @ApiResponse(responseCode = "409", description = "Règle de gestion violée (RG-05, RG-06)")
    })
    @PatchMapping("/{id}/annuler")
    public ReservationResponseDto annuler(@PathVariable Integer id) {
        return reservationService.annuler(id);
    }

    @Operation(summary = "Supprimer une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Réservation supprimée"),
            @ApiResponse(responseCode = "404", description = "Réservation inconnue")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        reservationService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
