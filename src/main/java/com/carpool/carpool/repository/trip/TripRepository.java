package com.carpool.carpool.repository.trip;

import com.carpool.carpool.model.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
