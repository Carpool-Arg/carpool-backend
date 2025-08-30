package com.carpool.carpool.model.trip;

import com.carpool.carpool.enums.trip.BaggageEnum;
import com.carpool.carpool.enums.trip.TripEnum;
import com.carpool.carpool.model.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name="trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //TODO: esperar a cambios que realicen los chicos
    private String originTown;
    private String destinationTown;
    private String intermediateTown;

    @Column(name="start_date_time", nullable = false)
    private LocalDateTime startTripDateTime;

    @Column(name="available_seat", nullable = false)
    private int availableSeat;

    @Enumerated(EnumType.STRING)
    @Column(name="available_baggage", nullable = false)
    private BaggageEnum availableBaggage;

    @Column(name="seat_price", nullable = false)
    private double seatPrice;

    @Enumerated(EnumType.STRING)
    @Column(name="state", nullable = false)
    private TripEnum state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deleted_by;

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
        this.state = TripEnum.CREATE;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
