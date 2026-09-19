package com.ibizabroker.bibliotheque.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "Reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer reservationId;

    Integer bookId;
    Integer userId;

    /**
     * Titre saisi pour un livre absent du catalogue : le backend crée alors
     * automatiquement le livre (0 exemplaire) et la réservation passe à
     * DISPONIBLE dès l'acquisition. Non persisté : le livre créé est
     * référencé par bookId.
     */
    @Transient
    String newBookTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    ReservationStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = JsonDataSerializer.class)
    @Column(name = "date_reservation")
    Date reservationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = JsonDataSerializer.class)
    @Column(name = "date_expiration")
    Date expirationDate;
}
