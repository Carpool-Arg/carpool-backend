package com.carpool.carpool.repository.stateHistory;

import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
    Optional<StateHistory> findByTripAndFinishDateTimeIsNullAndReservationIdIsNull(Trip trip);

    Optional<StateHistory> findTopByTripAndFinishDateTimeIsNotNullOrderByFinishDateTimeDesc(Trip trip);

    StateHistory findTopByReservationIdOrderByStartDateTimeDesc(Long idReservation);

    Optional<StateHistory> findByReservationIdAndFinishDateTimeIsNull(Long idReservation);
}
