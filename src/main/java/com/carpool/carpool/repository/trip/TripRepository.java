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
    @Query("SELECT t FROM Trip t " +
        "JOIN t.tripStops ts " +
        "JOIN t.vehicle v " +          
        "JOIN v.driver d " +
        "WHERE t.availableSeat > 0 " +
        "AND ts.city.id = :cityId " +
        "AND d.user.id != :userId " +
        "AND ts.stopOrder < (SELECT MAX(tsMax.stopOrder) FROM TripStop tsMax WHERE tsMax.trip.id = t.id) " +
        "ORDER BY t.startTripDateTime  ASC")
    List<Trip> findTripsForInitialFeed(@Param("cityId") Long cityId, @Param("userId") Long userId);
    
    /*
    * Busca viajes para el caso de la busqueda con filtros aplicados 
    */
    @Query(value = "SELECT t.* FROM trip t " +
        "JOIN vehicles v ON v.id = t.vehicle_id " +
        "JOIN driver d ON d.id = v.driver_id " + 
        
        "WHERE t.available_seat > 0 " +

        "AND d.user_id != :userId " + 
        
        // Filtro de fecha 
        "AND ((:departureDate)::date IS NULL OR t.start_date_time::date = :departureDate) " +
        
        // Filtro de ruta obligatorio
        "AND EXISTS (SELECT 1 FROM trip_stop ts1, trip_stop ts2 " +
        "           WHERE ts1.city_id = :originCityId " + 
        "           AND ts2.city_id = :destinationCityId " + 
        "           AND ts1.stop_order < ts2.stop_order " +
        "           AND t.id = ts1.trip_id " +
        "           AND t.id = ts2.trip_id) " +
        // Filtro opcional de calificacion del driver
        "AND (:driverRating IS NULL OR d.rating >= :driverRating) " +
        // Filtros opcionales de precio 
        "AND (:minPrice IS NULL OR t.seat_price >= :minPrice) " + 
        "AND (:maxPrice IS NULL OR t.seat_price <= :maxPrice) " + 

        //Ordenar por calificacion del driver opcionalmente o por fecha de salida
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
        @Param("driverRating") Double driverRating,
        @Param("userId") Long userId,
        @Param("orderByRating") Boolean orderByRating 
    );
}