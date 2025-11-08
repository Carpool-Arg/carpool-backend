package com.carpool.carpool.model.reservation;

import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.model.trip.tripStop.TripStop;
import com.carpool.carpool.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            nullable = false
    )
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    @ManyToOne
    @JoinColumn(name = "state_id", referencedColumnName = "id", nullable = false)
    private State state;
}
