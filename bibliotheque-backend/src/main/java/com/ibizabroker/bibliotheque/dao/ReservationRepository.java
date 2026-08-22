package com.ibizabroker.bibliotheque.dao;

import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.StatutReservation;
import com.ibizabroker.bibliotheque.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByStatut(StatutReservation statut);

    List<Reservation> findByAdherentUserId(Integer adherentId);

    List<Reservation> findByAdherentUserIdAndStatut(Integer adherentId, StatutReservation statut);

    boolean existsByAdherentAndLivreAndStatutIn(Users adherent, Books livre, Collection<StatutReservation> statuts);

    long countByAdherentAndStatutIn(Users adherent, Collection<StatutReservation> statuts);

    List<Reservation> findByStatutInAndDateExpirationBefore(Collection<StatutReservation> statuts, Date date);
}
