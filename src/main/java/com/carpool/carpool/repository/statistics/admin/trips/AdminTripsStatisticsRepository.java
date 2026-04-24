package com.carpool.carpool.repository.statistics.admin.trips;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpool.carpool.model.trip.Trip;

public interface AdminTripsStatisticsRepository extends JpaRepository<Trip,Long>{

}
