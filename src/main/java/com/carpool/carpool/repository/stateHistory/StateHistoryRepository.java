package com.carpool.carpool.repository.stateHistory;

import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
    Optional<StateHistory> findByTripAndFinishDatetimeIsNull(Trip trip);

    Optional<StateHistory> findTopByTripAndFinishDatetimeIsNotNullOrderByFinishDatetimeDesc(Trip trip);
}
