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
          AND sh.finishDateTime IS NULL
    """)
    Optional<Reservation> findReservationByUserAndTrip(
            @Param("userId") Long userId,
            @Param("tripId") Long tripId
    );

    @Query("""
    SELECT r FROM Reservation r
    JOIN r.trip t
    JOIN StateHistory sh ON sh.reservation = r
    JOIN sh.state st
    WHERE r.user.id = :userId
      AND t.id = :tripId
      AND sh.finishDateTime IS NULL
      AND st.name NOT IN :excludedStates
""")
    Optional<Reservation> findReservationByUserAndTripExcludingStates(
            @Param("userId") Long userId,
            @Param("tripId") Long tripId,
            @Param("excludedStates") List<String> excludedStates
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

    /**
     * Query para obtener las reservas en un estado que se pasa por parametros que finalizar en una parada intermedia
     * cuyo id tambien se pasa por parametros
     * @param tripId viaje al que pertenece la parada intermedia
     * @param tripStopId id de la parada intermedia de la cual se quiere obtener las reservas que finalizar alli
     * @param stateName estado por el que se quiere filtrar
     * @return
     */
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN StateHistory sh ON sh.reservation.id = r.id
        JOIN sh.state s
        WHERE r.trip.id = :tripId
        AND r.destinationCity.id = :tripStopId
        AND s.name = :stateName
        AND sh.finishDateTime IS NULL
    """)
    List<Reservation> findReservationsByTripAndDestinationAndState(
            @Param("tripId") Long tripId,
            @Param("tripStopId") Long tripStopId,
            @Param("stateName") String stateName
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

    /**
     * Obtiene la reserva del usuario que se encuentra actualmente en estado UNPAID.
     *
     * @param userId id del usuario
     * @return reserva con estado UNPAID si existe
     */
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN StateHistory sh ON sh.reservation.id = r.id
        JOIN sh.state s
        WHERE r.user.id = :userId
          AND s.name = 'UNPAID'
          AND sh.finishDateTime IS NULL
    """)
    Optional<Reservation> findUnpaidReservationByUserId(
            @Param("userId") Long userId
    );

    /**
     * Obtiene la reserva del usuario que se encuentra actualmente en estado EXPIRED.
     *
     * @param userId id del usuario
     * @return reserva con estado EXPIRED si existe
     */
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN StateHistory sh ON sh.reservation.id = r.id
        JOIN sh.state s
        WHERE r.user.id = :userId
        AND s.name = 'EXPIRED'
        AND sh.finishDateTime IS NULL
    """)
    Optional<Reservation> findExpiredReservationByUserId(
            @Param("userId") Long userId
    );

    @Query("""
    SELECT COUNT(r) > 0
    FROM Reservation r
    JOIN StateHistory sh ON sh.reservation.id = r.id
    JOIN sh.state s
    WHERE r.trip.id = :tripId
      AND s.name IN ('PENDING', 'ACCEPTED')
      AND sh.finishDateTime IS NULL
""")
    boolean existsActiveReservationsForTrip(@Param("tripId") Long tripId);
        
    int countReservedSeatsByTripId(Long tripId);
    List<Reservation> findByTripIdInAndUserId(List<Long> tripIds, Long userId);

    /**
     * Valida que un usuario pertenezca a un viaje especifico 
     * @param userId
     * @param tripId
     * @return
     */
    boolean existsByUserIdAndTripId(Long userId, Long tripId);

    @Query(value = """
    SELECT COUNT(r.id) > 0
    FROM reservation r
    JOIN trip_stop ts_start ON r.start_city_id = ts_start.id
    JOIN trip_stop ts_end   ON r.destination_city_id = ts_end.id
    JOIN state_history sh   ON r.id = sh.reservation_id
    JOIN state s            ON s.id = sh.state_id
    WHERE r.user_id = :userId
      AND sh.finish_datetime IS NULL
      AND s.name IN ('ACCEPTED', 'IN_PROGRESS')
      AND :newStart < (ts_end.estimated_arrival_date_time   + INTERVAL '30 minutes')
      AND :newEnd   > (ts_start.estimated_arrival_date_time - INTERVAL '30 minutes')
    """, nativeQuery = true)
    boolean hasOverlappingReservation(
        @Param("userId")   Long userId,
        @Param("newStart") LocalDateTime newStart,
        @Param("newEnd")   LocalDateTime newEnd
    );

   @Query(value = """
        SELECT r.*
        FROM reservation r
        JOIN trip_stop ts_start ON r.start_city_id = ts_start.id
        JOIN trip_stop ts_end   ON r.destination_city_id = ts_end.id
        JOIN state_history sh   ON r.id = sh.reservation_id
        JOIN state s ON s.id = sh.state_id
        WHERE r.user_id = :userId
            AND r.id <> :excludeReservationId
            AND sh.finish_datetime IS NULL
            AND s.name = 'PENDING'
            AND :newStart < (ts_end.estimated_arrival_date_time   + INTERVAL '30 minutes')
            AND :newEnd   > (ts_start.estimated_arrival_date_time - INTERVAL '30 minutes')
    """, nativeQuery = true)
    List<Reservation> findOverlappingPendingReservations(
        @Param("userId") Long userId,
        @Param("newStart") LocalDateTime newStart,
        @Param("newEnd") LocalDateTime newEnd,
        @Param("excludeReservationId") Long excludeReservationId);

}
