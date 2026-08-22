package com.ibizabroker.bibliotheque.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReservationRequestDto {

    @NotNull(message = "Le champ livreId est obligatoire")
    private Integer livreId;

    @NotNull(message = "Le champ adherentId est obligatoire")
    private Integer adherentId;
}
