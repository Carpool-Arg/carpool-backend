package com.carpool.carpool.model.stateHistory;

import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.trip.Trip;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
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
            name = "state",
            referencedColumnName = "id",
            nullable = false
    )
    private State state;

    @ManyToOne
    @JoinColumn(
            name = "trip",
            referencedColumnName = "id",
            nullable = false
    )
    private Trip tripState;

    //TODO: agregar reportState reservationState

    @PrePersist
    protected void onCreate() {
        this.startDateTime = LocalDateTime.now();
    }
}
