package com.carpool.carpool.repository.trip;

import com.carpool.carpool.model.trip.Trip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    @Query("SELECT DISTINCT t FROM Trip t " + 
        "JOIN t.tripStops ts " +
        "JOIN t.vehicle v " +
        "JOIN v.driver d " +
        "JOIN t.stateHistory sh " +
        "WHERE t.currentAvailableSeats > 0 " +
        "AND sh.state.name = 'CREATED' AND sh.finishDateTime IS NULL " +
        "AND t.startTripDateTime >= :now " +
        "AND ts.city.id = :cityId " +
        "AND d.user.id != :userId " +
        "AND ts.stopOrder < (SELECT MAX(tsMax.stopOrder) FROM TripStop tsMax WHERE tsMax.trip.id = t.id) " +
        "AND NOT EXISTS (" + 
        "  SELECT r FROM Reservation r " +
        "  WHERE r.trip.id = t.id " + 
        "  AND r.user.id = :userId " +
        "  AND EXISTS (" +
        "    SELECT sh FROM StateHistory sh " +
        "    JOIN sh.state st " +
        "    WHERE sh.reservation.id = r.id " +
        "    AND sh.finishDateTime IS NULL " + 
        "    AND st.name IN ('ACCEPTED', 'PENDING') " +
        "  )" +
        ") " + 
        "ORDER BY t.startTripDateTime ASC")
    List<Trip> findTripsForInitialFeed(
        @Param("cityId") Long cityId, 
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now);
    
    /*
     * Busca viajes para el caso de la busqueda con filtros aplicados 
     */
    @Query(value = "SELECT t.* FROM trip t " +
        "JOIN vehicles v ON v.id = t.vehicle_id " +
        "JOIN driver d ON d.id = v.driver_id " + 
        "JOIN state_history sh ON sh.trip_id = t.id " + 
        "JOIN state s ON s.id = sh.state_id " +

        "WHERE t.current_available_seats > 0 " +
        "AND s.name = 'CREATED' AND sh.finish_datetime IS NULL " +
        "AND t.start_date_time >= :now " +
        "AND d.user_id != :userId " + 

        "AND NOT EXISTS ( " +
        " SELECT 1 FROM reservation r " +
        " JOIN state_history shR ON shR.reservation_id = r.id " +
        " JOIN state sR ON sR.id = shR.state_id " +
        " WHERE r.trip_id = t.id " + 
        " AND r.user_id = :userId " + 
        " AND sR.name IN ('ACCEPTED', 'PENDING') " + 
        " AND shR.finish_datetime IS NULL " + 
        ") " +
        "AND ((:departureDate)::date IS NULL OR t.start_date_time::date = :departureDate) " +
        "AND EXISTS (SELECT 1 FROM trip_stop ts1, trip_stop ts2 " +
        " WHERE ts1.city_id = :originCityId " + 
        " AND ts2.city_id = :destinationCityId " + 
        " AND ts1.stop_order < ts2.stop_order " +
        " AND t.id = ts1.trip_id " +
        " AND t.id = ts2.trip_id) " +
        "AND (:minPrice IS NULL OR t.published_seat_price >= :minPrice) " + 
        "AND (:maxPrice IS NULL OR t.published_seat_price <= :maxPrice) " + 
        "ORDER BY " +
        "CASE WHEN :orderByRating = TRUE THEN d.rating ELSE NULL END DESC, " + 
        "t.start_date_time ASC",
        nativeQuery = true)
    List<Trip> findFilteredTrips(
        @Param("originCityId") Long originCityId,
        @Param("destinationCityId") Long destinationCityId,
        @Param("departureDate") java.time.LocalDate departureDate, 
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("userId") Long userId,
        @Param("orderByRating") Boolean orderByRating,
        @Param("now") LocalDateTime now);

    @Query(value = """
        SELECT t.* 
        FROM trip t
        JOIN state_history sh ON sh.trip_id = t.id
        JOIN state s ON s.id = sh.state_id
        JOIN vehicles v ON v.id = t.vehicle_id
        JOIN trip_stop ts ON ts.trip_id = t.id AND ts.is_destination = true
        WHERE v.driver_id = :driverId
          AND s.name = 'CREATED'
          AND s.scope = 'TRIP'
          AND sh.start_datetime = (
              SELECT MAX(sh2.start_datetime)
              FROM state_history sh2
              WHERE sh2.trip_id = t.id
          )
    """, nativeQuery = true)
    List<Trip> findTripsByDriverIdWithCurrentStateCreateTrip(@Param("driverId") Long driverId);

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
        JOIN trip_stop ts ON t.id = ts.trip_id
        JOIN state_history sh ON t.id = sh.trip_id
        JOIN state s ON s.id = sh.state_id
        WHERE t.vehicle_id IN (SELECT v.id FROM vehicles v WHERE v.driver_id = :driverId)
            AND ts.is_destination = true
            AND sh.finish_datetime IS NULL
            AND s.name IN ('CREATED', 'CLOSED', 'IN_PROGRESS')
            AND :newStart < (ts.estimated_arrival_date_time + INTERVAL '30 minutes')
            AND :newEnd > (t.start_date_time - INTERVAL '30 minutes')
    """, nativeQuery = true)
    boolean hasOverlappingSchedule(
        @Param("driverId") Long driverId, 
        @Param("newStart") LocalDateTime newStart,
        @Param("newEnd") LocalDateTime newEnd
    );

    /**
     * Verifica si en un instante de tiempo especifico para hacer un viaje, cae
     * dentro de un viaje programado o en curso 
     * @param driverId
     * @param timeToCheck
     * @return
     */
    @Query(value = """
        SELECT COUNT(t.id) > 0 
        FROM trip t
        JOIN trip_stop ts ON t.id = ts.trip_id
        JOIN state_history sh ON t.id = sh.trip_id
        JOIN state s ON s.id = sh.state_id
        WHERE t.vehicle_id IN (SELECT v.id FROM vehicles v WHERE v.driver_id = :driverId)
            AND ts.is_destination = true
            AND sh.finish_datetime IS NULL
            AND s.name IN ('CREATED', 'IN_PROGRESS')
            AND :timeToCheck BETWEEN (t.start_date_time - INTERVAL '30 minutes') 
                                AND ts.estimated_arrival_date_time
    """, nativeQuery = true)
    boolean isTimeSlotOccupied(
        @Param("driverId") Long driverId, 
        @Param("timeToCheck") LocalDateTime timeToCheck
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
    
}