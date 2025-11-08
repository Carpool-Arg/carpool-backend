package com.carpool.carpool.repository.reservation;

import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long>, JpaSpecificationExecutor<Reservation> {
    Optional<Reservation> findByUserId(Long userId);
}
