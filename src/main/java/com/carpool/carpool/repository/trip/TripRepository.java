package com.carpool.carpool.repository.trip;

import com.carpool.carpool.model.trip.Trip;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t " +
           "JOIN FETCH t.vehicle v " +
           "JOIN FETCH v.driver d " +
           "JOIN FETCH d.user u " +
           "JOIN FETCH t.originCity " +
           "JOIN FETCH t.destinationCity " +
           "WHERE t.id = :id")
    Optional<Trip> findTripWithAllDetails(@Param("id") Long id);
}
