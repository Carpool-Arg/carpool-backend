package com.carpool.carpool.model.trip.tripStop;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.trip.Trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@Entity
@Table(name="trip_stop")
public class TripStop implements Serializable{
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private boolean isStart;

    private boolean isDestination;
    
    @Column(nullable = false)
    private String observation;

    @Column(nullable = false)
    private int stopOrder;

    @Column(nullable = false)
    private double distanceFromPrevious;

    @Column(nullable = false)
    private LocalDateTime estimatedArrivalDateTime;

    @OneToMany(mappedBy = "startCity")
    private List<Reservation> startsReservations;

    @OneToMany(mappedBy = "destinationCity")
    private List<Reservation> destinationReservations;

    @Column
    private LocalDateTime arrivalDateTime;

    @ManyToOne
    @JoinColumn(name="city_id",nullable = false)
    private City city;

    @ManyToOne
    @JoinColumn(name="trip_id", referencedColumnName = "id",nullable = false)
    private Trip trip;
}
