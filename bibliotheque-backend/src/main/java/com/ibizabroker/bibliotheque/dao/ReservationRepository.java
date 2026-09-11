package com.ibizabroker.bibliotheque.dao;

import com.ibizabroker.bibliotheque.entity.Reservation;
import com.ibizabroker.bibliotheque.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByUserId(Integer userId);
    List<Reservation> findByBookId(Integer bookId);
    List<Reservation> findByBookIdAndUserIdAndStatusIn(Integer bookId, Integer userId, List<ReservationStatus> statuses);
    long countByUserIdAndStatusIn(Integer userId, List<ReservationStatus> statuses);
}
