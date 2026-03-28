package com.carpool.carpool.model.reservation;

import java.time.LocalDateTime;

import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="reservation")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private double total;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false
    )
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancellation_reason", length = 250)
    private String cancellationReason;

    @ManyToOne
    @JoinColumn(
            name = "trip_id",
            referencedColumnName = "id",
            nullable = false
    )
    private Trip trip;

    @ManyToOne
    @JoinColumn(
            name = "start_city_id",
            referencedColumnName = "id",
            nullable = false
    )
    private TripStop startCity;

    @ManyToOne
    @JoinColumn(
            name = "destination_city_id",
            referencedColumnName = "id",
            nullable = false
    )
    private TripStop destinationCity;

    @Column(name="baggage", nullable = false)
    private boolean baggage;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
