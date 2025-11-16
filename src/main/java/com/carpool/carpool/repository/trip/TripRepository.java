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

    boolean existsByVehicleIdAndStartTripDateTimeAfter(Long vehicleId, LocalDateTime starTime);

    boolean existsByVehicleDriverIdAndStartTripDateTime(Long driverId, LocalDateTime startTripDateTime);

    /*
     * Busca viajes para el caso del feed inicial (sin filtros aplicados)
     * Solo trae viajes con asientos disponibles y que pasen por la localidad del usuario
     */
    @Query("SELECT DISTINCT t FROM Trip t " + 
        "JOIN t.tripStops ts " +
        "JOIN t.vehicle v " +
        "JOIN v.driver d " +
        
        "WHERE t.currentAvailableSeats > 0 " +
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
    List<Trip> findTripsForInitialFeed(@Param("cityId") Long cityId, @Param("userId") Long userId);
    
    /*
     * Busca viajes para el caso de la busqueda con filtros aplicados 
     */
    @Query(value = "SELECT t.* FROM trip t " +
        "JOIN vehicles v ON v.id = t.vehicle_id " +
        "JOIN driver d ON d.id = v.driver_id " + 
        
        "WHERE t.current_available_seats > 0 " +
        "AND d.user_id != :userId " + 

        
        "AND NOT EXISTS ( " +
        " SELECT 1 FROM reservation r " +
        " JOIN state_history sh ON sh.reservation_id = r.id " +
        " JOIN state s ON s.id = sh.state_id " +
        " WHERE r.trip_id = t.id " + 
        " AND r.user_id = :userId " + 
        " AND s.name IN ('ACCEPTED', 'PENDING') " + 
        " AND sh.finish_datetime IS NULL " + 
        ") " +
        
        // Filtro de fecha 
        "AND ((:departureDate)::date IS NULL OR t.start_date_time::date = :departureDate) " +
        // Filtro de ruta obligatorio
        "AND EXISTS (SELECT 1 FROM trip_stop ts1, trip_stop ts2 " +
        " WHERE ts1.city_id = :originCityId " + 
        " AND ts2.city_id = :destinationCityId " + 
        " AND ts1.stop_order < ts2.stop_order " +
        " AND t.id = ts1.trip_id " +
        " AND t.id = ts2.trip_id) " +
        // Filtros opcionales de precio 
        "AND (:minPrice IS NULL OR t.seat_price >= :minPrice) " + 
        "AND (:maxPrice IS NULL OR t.seat_price <= :maxPrice) " + 


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
        @Param("orderByRating") Boolean orderByRating 
    );

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
}