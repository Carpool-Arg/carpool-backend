package com.carpool.carpool.repository.trip.stop;

import com.carpool.carpool.model.trip.tripStop.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {
    Optional<TripStop> findByTripIdAndCityId(Long tripId, Long cityId);
}
