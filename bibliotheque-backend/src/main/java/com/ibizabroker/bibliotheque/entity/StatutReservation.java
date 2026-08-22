package com.ibizabroker.bibliotheque.entity;

public enum StatutReservation {

    EN_ATTENTE,
    DISPONIBLE,
    ANNULEE,
    EXPIREE,
    HONOREE;

    public boolean isActive() {
        return this == EN_ATTENTE || this == DISPONIBLE;
    }
}
