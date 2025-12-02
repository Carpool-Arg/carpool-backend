package com.carpool.carpool.model.stateHistory;

import com.carpool.carpool.model.reservation.Reservation;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.trip.Trip;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor 
@Table(name="state_history")
public class StateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_datetime", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name="finish_datetime")
    private LocalDateTime finishDateTime;

    @ManyToOne
    @JoinColumn(
        name = "state_id",
        referencedColumnName = "id",
        nullable = false
    )
    private State state;

    @ManyToOne
    @JoinColumn(
        name = "trip_id",
        referencedColumnName = "id"
    )
    private Trip trip;

    @ManyToOne
    @JoinColumn(
            name = "reservation_id",
            referencedColumnName = "id"
    )
    private Reservation reservation;

    //TODO: agregar reportState

    @PrePersist
    protected void onCreate() {
        this.startDateTime = LocalDateTime.now();
    }
}
