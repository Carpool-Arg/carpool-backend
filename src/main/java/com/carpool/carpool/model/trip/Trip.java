package com.carpool.carpool.model.trip;

import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.model.review.Review;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.vehicle.Vehicle;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="trip")
@AllArgsConstructor
@NoArgsConstructor  
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_date_time", nullable = false)
    private LocalDateTime startTripDateTime;

    @Column(name="available_seat", nullable = false)
    private int availableSeat;

    @Column(name="current_available_seats",nullable = false)
    private int currentAvailableSeats;

    @Enumerated(EnumType.STRING)
    @Column(name="available_baggage", nullable = false)
    private BaggageEnum availableBaggage;

    @Column(name="seat_price", nullable = false)
    private double seatPrice;

    @Column(name="published_seat_price", nullable = false)
    private double publishedSeatPrice; 

    @Column(name="driver_price_discount", nullable = false)
    private double driverPriceDiscount;

    @Column(name = "cancellation_reason", length = 250)
    private String cancellationReason;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StateHistory> stateHistory;

    @OneToMany(mappedBy = "trip",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<TripStop> tripStops;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deleted_by;

    @Column(name="kilometer_price", nullable = false)
    private double kilometerPrice;

    @OneToMany(mappedBy = "trip",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Review> reviews;
    
    @ManyToOne
    @JoinColumn(
            name = "vehicle_id",
            referencedColumnName = "id",
            nullable = false
    )
    private Vehicle vehicle;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
