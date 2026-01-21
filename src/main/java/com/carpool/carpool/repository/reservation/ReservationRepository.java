package com.carpool.carpool.repository.reservation;

import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long>, JpaSpecificationExecutor<Reservation> {
    Optional<Reservation> findByUserId(Long userId);

    @Query("""
        SELECT r FROM Reservation r
        JOIN r.trip t
        JOIN StateHistory sh ON sh.reservation.id = r.id
        JOIN sh.state st
        WHERE r.user.id = :userId
          AND st.name IN ('ACCEPTED', 'PENDING', 'REJECTED', 'COMPLETED')
          AND t.id = :tripId
    """)
    Optional<Reservation> findReservationByUserAndTrip(
            @Param("userId") Long userId,
            @Param("tripId") Long tripId
    );

    @Query("""
        SELECT r FROM Reservation r
        JOIN r.trip t
        JOIN r.startCity sCity
        JOIN r.destinationCity sDestintaion
        JOIN StateHistory sh ON sh.reservation.id = r.id
        JOIN sh.state st
        WHERE r.user.id = :userId
          AND st.name = 'ACCEPTED'
          AND (
              (:departureTime < sDestintaion.estimatedArrivalDateTime)
              AND (:arrivalTime > sCity.estimatedArrivalDateTime)
          )
    """)
    List<Reservation> findOverlappingAcceptedReservations(
            @Param("userId") Long userId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime
    );

    @Query("""
        SELECT r FROM Reservation r 
        JOIN StateHistory sh ON sh.reservation.id = r.id
        JOIN sh.state s
        WHERE r.trip.id = :tripId 
        AND s.name = :stateName 
        AND sh.finishDateTime IS NULL
    """)
    List<Reservation> findByTripIdAndStateName(@Param("tripId") Long tripId, @Param("stateName") String stateName);

}
