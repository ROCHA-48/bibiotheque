package com.ibizabroker.bibliotheque.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.StatutReservation;
import lombok.Data;

import java.util.Date;

@Data
public class ReservationResponseDto {

    private Integer id;

    private Integer livreId;

    private String titreLivre;

    private Integer adherentId;

    private String nomAdherent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date dateReservation;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date dateExpiration;

    private StatutReservation statut;

    public static ReservationResponseDto fromEntity(Reservation reservation) {
        ReservationResponseDto dto = new ReservationResponseDto();
        dto.setId(reservation.getReservationId());
        dto.setLivreId(reservation.getLivre().getBookId());
        dto.setTitreLivre(reservation.getLivre().getBookName());
        dto.setAdherentId(reservation.getAdherent().getUserId());
        dto.setNomAdherent(reservation.getAdherent().getName());
        dto.setDateReservation(reservation.getDateReservation());
        dto.setDateExpiration(reservation.getDateExpiration());
        dto.setStatut(reservation.getStatut());
        return dto;
    }
}
