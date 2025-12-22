package com.carpool.carpool.model.trip.tripStop;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.trip.Trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
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

    @ManyToOne
    @JoinColumn(name="city_id",nullable = false)
    private City city;

    @ManyToOne
    @JoinColumn(name="trip_id", referencedColumnName = "id",nullable = false)
    private Trip trip;
}
