package com.carpool.carpool.repository.trip;

import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t " +
           "JOIN FETCH t.vehicle v " +
           "JOIN FETCH v.driver d " +
           "JOIN FETCH d.user u " +
           "WHERE t.id = :id")
    Optional<Trip> findTripWithAllDetails(@Param("id") Long id);


    /**
     * Query para determinar si el vehiculo que se pasa por parametros tiene un viaje pendiente
     */
    @Query("""
    SELECT COUNT(sh) > 0
    FROM StateHistory sh
    WHERE sh.state.scope = 'TRIP'
      AND sh.state.name = 'CREATED'
      AND sh.finishDateTime IS NULL
      AND sh.trip.vehicle.id = :vehicleId
    """)
    boolean vehicleHasPendingTrip(@Param("vehicleId") Long vehicleId);

    boolean existsByVehicleDriverIdAndStartTripDateTime(Long driverId, LocalDateTime startTripDateTime);


    /**
     * Query para determinar si el vehiculo que se pasa por parametros tiene un viaje en curso
     */
    @Query("""
    SELECT COUNT(sh) > 0
    FROM StateHistory sh
    WHERE sh.state.scope = 'TRIP'
      AND sh.state.name = 'IN_PROGRESS'
      AND sh.finishDateTime IS NULL
      AND sh.trip.vehicle.id = :vehicleId
    """)
    boolean vehicleHasInProgressTrip(@Param("vehicleId") Long vehicleId);


    /*
     * Busca viajes para el caso del feed inicial (sin filtros aplicados)
     * Solo trae viajes con asientos disponibles y que pasen por la localidad del usuario
     */
    @Query(
        value = "SELECT DISTINCT t FROM Trip t " + 
            "JOIN t.tripStops ts ON ts.deletedAt IS NULL " +
            "JOIN t.vehicle v " +
            "JOIN v.driver d " + 
            "JOIN t.stateHistory sh " +
            "WHERE t.currentAvailableSeats > 0 " +
            "AND sh.state.name = 'CREATED' AND sh.finishDateTime IS NULL " + 
            "AND t.startTripDateTime >= :now " +
            "AND ts.city.id = :cityId " +
            "AND d.user.id != :userId " +
            "AND ts.stopOrder < (SELECT MAX(tsMax.stopOrder) FROM TripStop tsMax WHERE tsMax.trip.id = t.id AND tsMax.deletedAt IS NULL) " +
            "AND NOT EXISTS (" + 
            "  SELECT r FROM Reservation r " +
            "  JOIN StateHistory shR ON shR.reservation.id = r.id " + 
            "  WHERE r.trip.id = t.id " + 
            "  AND r.user.id = :userId " +
            "  AND shR.finishDateTime IS NULL " +
            "  AND shR.state.name != 'CANCELLED' " + 
            ")",
        countQuery = "SELECT COUNT(DISTINCT t.id) FROM Trip t " + 
            "JOIN t.tripStops ts ON ts.deletedAt IS NULL " +
            "JOIN t.vehicle v " +
            "JOIN v.driver d " + 
            "JOIN t.stateHistory sh " +
            "WHERE t.currentAvailableSeats > 0 " +
            "AND sh.state.name = 'CREATED' AND sh.finishDateTime IS NULL " + 
            "AND t.startTripDateTime >= :now " +
            "AND ts.city.id = :cityId " +
            "AND d.user.id != :userId " +
            "AND ts.stopOrder < (SELECT MAX(tsMax.stopOrder) FROM TripStop tsMax WHERE tsMax.trip.id = t.id AND tsMax.deletedAt IS NULL) " +
            "AND NOT EXISTS (" + 
            "  SELECT r FROM Reservation r " +
            "  JOIN StateHistory shR ON shR.reservation.id = r.id " + 
            "  WHERE r.trip.id = t.id " + 
            "  AND r.user.id = :userId " +
            "  AND shR.finishDateTime IS NULL " +
            "  AND shR.state.name != 'CANCELLED' " + 
            ")"
    )
    Page<Trip> findTripsForInitialFeed(
        @Param("cityId") Long cityId,
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );
    
    /*
     * Busca viajes para el caso de la busqueda con filtros aplicados 
     */
   @Query(value = "SELECT t.* FROM trip t " +
        "JOIN vehicles v ON v.id = t.vehicle_id " +
        "JOIN driver d ON d.id = v.driver_id " + 
        "JOIN state_history sh ON sh.trip_id = t.id " +
        "JOIN state s ON s.id = sh.state_id " +
        "WHERE t.current_available_seats > 0 " +
        "AND s.name = 'CREATED' AND s.scope = 'TRIP' AND sh.finish_datetime IS NULL " + 
        "AND t.start_date_time >= :now " +
        "AND d.user_id != :userId " +
        "AND NOT EXISTS ( " +
        " SELECT 1 FROM reservation r " +
        " JOIN state_history shR ON shR.reservation_id = r.id " +
        " JOIN state sR ON sR.id = shR.state_id " +
        " WHERE r.trip_id = t.id " + 
        " AND r.user_id = :userId " + 
        " AND shR.finish_datetime IS NULL " +
        " AND sR.name != 'CANCELLED' " + 
        ") " +
        "AND ((:departureDate)::date IS NULL OR t.start_date_time::date = :departureDate) " +
        "AND EXISTS (SELECT 1 FROM trip_stop ts1, trip_stop ts2 " +
        " WHERE ts1.city_id = :originCityId " + 
        " AND ts2.city_id = :destinationCityId " + 
        " AND ts1.stop_order < ts2.stop_order " +
        " AND t.id = ts1.trip_id " +
        " AND t.id = ts2.trip_id " +
        " AND ts1.deleted_at IS NULL " +
        " AND ts2.deleted_at IS NULL) " +
        "AND (:minPrice IS NULL OR t.published_seat_price >= :minPrice) " + 
        "AND (:maxPrice IS NULL OR t.published_seat_price <= :maxPrice) " +
        "ORDER BY " +
        "CASE WHEN :orderByRating = TRUE THEN d.rating ELSE NULL END DESC, " + 
        "t.start_date_time ASC",
        countQuery = "SELECT COUNT(DISTINCT t.id) FROM trip t " +
        "JOIN vehicles v ON v.id = t.vehicle_id " +
        "JOIN driver d ON d.id = v.driver_id " + 
        "JOIN state_history sh ON sh.trip_id = t.id " +
        "JOIN state s ON s.id = sh.state_id " +
        "WHERE t.current_available_seats > 0 " +
        "AND s.name = 'CREATED' AND s.scope = 'TRIP' AND sh.finish_datetime IS NULL " + 
        "AND t.start_date_time >= :now " +
        "AND d.user_id != :userId " +
        "AND NOT EXISTS ( " +
        " SELECT 1 FROM reservation r " +
        " JOIN state_history shR ON shR.reservation_id = r.id " +
        " JOIN state sR ON sR.id = shR.state_id " +
        " WHERE r.trip_id = t.id " + 
        " AND r.user_id = :userId " + 
        " AND shR.finish_datetime IS NULL " +
        " AND sR.name != 'CANCELLED' " + 
        ") " +
        "AND ((:departureDate)::date IS NULL OR t.start_date_time::date = :departureDate) " +
        "AND EXISTS (SELECT 1 FROM trip_stop ts1, trip_stop ts2 " +
        " WHERE ts1.city_id = :originCityId " + 
        " AND ts2.city_id = :destinationCityId " + 
        " AND ts1.stop_order < ts2.stop_order " +
        " AND t.id = ts1.trip_id " +
        " AND t.id = ts2.trip_id " +
        " AND ts1.deleted_at IS NULL " +
        " AND ts2.deleted_at IS NULL) " +
        "AND (:minPrice IS NULL OR t.published_seat_price >= :minPrice) " + 
        "AND (:maxPrice IS NULL OR t.published_seat_price <= :maxPrice)",
        nativeQuery = true)
    Page<Trip> findFilteredTrips(
        @Param("originCityId") Long originCityId,
        @Param("destinationCityId") Long destinationCityId,
        @Param("departureDate") java.time.LocalDate departureDate, 
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("userId") Long userId,
        @Param("orderByRating") Boolean orderByRating,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );  

    @Query(value = """
        SELECT DISTINCT ON (t.id) t.*
        FROM trip t
        JOIN state_history sh ON sh.trip_id = t.id
        JOIN state s ON s.id = sh.state_id
        JOIN vehicles v ON v.id = t.vehicle_id
        JOIN trip_stop ts ON ts.trip_id = t.id AND ts.is_destination = true AND ts.deleted_at IS NULL
        LEFT JOIN reservation r ON r.trip_id = t.id
        LEFT JOIN state_history sh_r 
            ON sh_r.reservation_id = r.id 
        AND sh_r.finish_datetime IS NULL
        LEFT JOIN state s_r 
            ON s_r.id = sh_r.state_id
        AND s_r.scope = 'RESERVATION'
        AND s_r.name = 'ACCEPTED'
        WHERE v.driver_id = :driverId
        AND s.name IN (:tripState)
        AND s.scope = 'TRIP'
        AND sh.start_datetime = (
            SELECT MAX(sh2.start_datetime)
            FROM state_history sh2
            WHERE sh2.trip_id = t.id
        )
        ORDER BY 
            t.id,
            CASE WHEN s.name = 'FINISHED' THEN 1 ELSE 0 END,
            t.start_date_time
        """,
        countQuery = """
        SELECT COUNT(DISTINCT t.id)
        FROM trip t
        JOIN state_history sh ON sh.trip_id = t.id
        JOIN state s ON s.id = sh.state_id
        JOIN vehicles v ON v.id = t.vehicle_id
        JOIN trip_stop ts ON ts.trip_id = t.id AND ts.is_destination = true AND ts.deleted_at IS NULL
        LEFT JOIN reservation r ON r.trip_id = t.id
        LEFT JOIN state_history sh_r 
            ON sh_r.reservation_id = r.id 
        AND sh_r.finish_datetime IS NULL
        LEFT JOIN state s_r 
            ON s_r.id = sh_r.state_id
        AND s_r.scope = 'RESERVATION'
        AND s_r.name = 'ACCEPTED'
        WHERE v.driver_id = :driverId
        AND s.name IN (:tripState)
        AND s.scope = 'TRIP'
        AND sh.start_datetime = (
            SELECT MAX(sh2.start_datetime)
            FROM state_history sh2
            WHERE sh2.trip_id = t.id
        )
        """,
        nativeQuery = true)
    Page<Trip> findTripsByDriverIdWithCurrentStateTrip(
            @Param("driverId") Long driverId,
            @Param("tripState") List<String> tripState,
            Pageable pageable  
    );

    @Query("""
            Select t from Trip t
            Join t.stateHistory sh 
            Join sh.state s
            Where s.name = 'CREATED'
                and s.scope = 'TRIP'
                and sh.finishDateTime is NULL
                and t.startTripDateTime <= :limitTime
            """)
    List<Trip> findTripToClose (@Param("limitTime") LocalDateTime limitTime);



    /**
     * Verifica si el rango de tiempo para un nuevo viaje se solapa con uno existente, creado o en curso.
     * Es decir que se encuentra dentro del rango de un viaje, e inclusive 30 minutos antes del inicio del mismo
     * @param driverId
     * @param newStart
     * @param newEnd
     * @return
     */
    @Query(value = """
        SELECT COUNT(t.id) > 0 
        FROM trip t
        JOIN trip_stop ts ON t.id = ts.trip_id AND ts.deleted_at IS NULL
        JOIN state_history sh ON t.id = sh.trip_id
        JOIN state s ON s.id = sh.state_id
        WHERE t.vehicle_id IN (SELECT v.id FROM vehicles v WHERE v.driver_id = :driverId)
            AND ts.is_destination = true
            AND sh.finish_datetime IS NULL
            AND s.name IN ('CREATED', 'CLOSED', 'IN_PROGRESS')
            AND :newStart < (ts.estimated_arrival_date_time + INTERVAL '30 minutes')
            AND :newEnd > (t.start_date_time - INTERVAL '30 minutes')
            AND (:idTrip IS NULL OR t.id <> :idTrip) 

            OR EXISTS (
                SELECT 1
                FROM reservation r
                JOIN trip_stop ts_start ON r.start_city_id = ts_start.id
                JOIN trip_stop ts_end ON r.destination_city_id = ts_end.id
                JOIN state_history sh_r ON r.id = sh_r.reservation_id
                JOIN state s_r ON s_r.id = sh_r.state_id
                WHERE r.user_id = :userId
                    AND sh_r.finish_datetime IS NULL
                    AND s_r.name IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS')

                    -- Validación de solapamiento con margen de 30 minutos
                    AND :newStart < (ts_end.estimated_arrival_date_time + INTERVAL '30 minutes')
                    AND :newEnd > (ts_start.estimated_arrival_date_time - INTERVAL '30 minutes')
            )
    """, nativeQuery = true)
    boolean hasOverlappingSchedule(
        @Param("userId") Long userId,
        @Param("driverId") Long driverId,
        @Param("newStart") LocalDateTime newStart,
        @Param("newEnd") LocalDateTime newEnd,
        @Param("idTrip") Long idTrip
    );

    /**
     * Verifica que el chofer tenga un viaje en progreso
     * @param driverId Id del chofer
     * @return true si tien un viaje en progreso
     */
    @Query(value = """
            SELECT COUNT (t.id) > 0
            FROM trip t 
            JOIN state_history sh ON t.id = sh.trip_id
            JOIN state s ON s.id = sh.state_id
            WHERE t.vehicle_id IN (SELECT v.id FROM vehicles v WHERE v.driver_id = :driverId)
                AND sh.finish_datetime IS NULL 
                AND s.name = 'IN_PROGRESS'
    """, nativeQuery = true)
    boolean hasTripInProgress(@Param("driverId") Long driverId);

    /**
     * Consulta que devuelve el viaje en progreso de un chofer, si es que lo tiene
     * @param driverId
     * @return
     */
    @Query(value = """
        SELECT t.* 
        FROM trip t
        JOIN state_history sh ON sh.trip_id = t.id
        JOIN state s ON s.id = sh.state_id
        JOIN vehicles v ON v.id = t.vehicle_id
        JOIN trip_stop ts ON ts.trip_id = t.id AND ts.deleted_at IS NULL
        WHERE v.driver_id = :driverId
          AND s.name = 'IN_PROGRESS'
          AND s.scope = 'TRIP'
          AND sh.start_datetime = (
              SELECT MAX(sh2.start_datetime)
              FROM state_history sh2
              WHERE sh2.trip_id = t.id
          )
        LIMIT 1
    """, nativeQuery = true)
    Optional<Trip> findCurrentTripByDriver(@Param("driverId") Long driverId);
    
	@Query(value = """
			    SELECT DISTINCT t
			    FROM Trip t
			    JOIN Reservation r ON r.trip = t
			    JOIN StateHistory sh ON sh.trip = t
			    JOIN State s ON s = sh.state
			    WHERE r.user.id = :userId
			      AND sh.finishDateTime IS NULL
			      AND s.name IN :states
			""", countQuery = """
			    SELECT COUNT(DISTINCT t.id)
			    FROM Trip t
			    JOIN Reservation r ON r.trip = t
			    JOIN StateHistory sh ON sh.trip = t
			    JOIN State s ON s = sh.state
			    WHERE r.user.id = :userId
			      AND sh.finishDateTime IS NULL
			      AND s.name IN :states
			""")
	Page<Trip> findTripsByUserAndCurrentStates(@Param("userId") Long userId, @Param("states") List<String> states,
			Pageable pageable);

    /**
     * Consulta que obtiene una lista con lso usuarios que participaron de un viaje en espcifico y tienen 
     * sus reservas en un estado actual que coincide con alguno de la lista que se pasa por parametros
     * @param tripId
     * @param states
     * @return
     */
    @Query("""
    SELECT DISTINCT r.user
    FROM Reservation r
    JOIN StateHistory sh ON sh.reservation = r
    JOIN State s ON s = sh.state
    WHERE r.trip.id = :tripId
      AND sh.finishDateTime IS NULL
      AND s.name IN :states
    """)
    List<User> findUsersByTripIdAndReservationStates(
            @Param("tripId") Long tripId,
            @Param("states") List<String> states
    );
    
    @Query(value = """
        SELECT COUNT(t.id) > 0
        FROM trip t
        JOIN trip_stop ts_start ON t.id = ts_start.trip_id
                            AND ts_start.deleted_at IS NULL
                            AND ts_start.is_start = true
        JOIN trip_stop ts_end   ON t.id = ts_end.trip_id
                            AND ts_end.deleted_at IS NULL
                            AND ts_end.is_destination = true
        JOIN vehicles v          ON t.vehicle_id = v.id
        JOIN state_history sh   ON t.id = sh.trip_id
        JOIN state s            ON s.id = sh.state_id
        WHERE v.driver_id = :driverId
        AND sh.finish_datetime IS NULL
        AND s.name IN ('CREATED', 'CLOSED', 'IN_PROGRESS')
        AND :newStart < (ts_end.estimated_arrival_date_time   + INTERVAL '30 minutes')
        AND :newEnd   > (ts_start.estimated_arrival_date_time - INTERVAL '30 minutes')
    """, nativeQuery = true)
    boolean hasOverlappingTripAsDriver(
        @Param("driverId")   Long driverId,
        @Param("newStart") LocalDateTime newStart,
        @Param("newEnd")   LocalDateTime newEnd
    );

}