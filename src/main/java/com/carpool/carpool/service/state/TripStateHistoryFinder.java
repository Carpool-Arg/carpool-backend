package com.carpool.carpool.service.state;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import com.carpool.carpool.repository.stateHistory.StateHistoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TripStateHistoryFinder implements StateHistoryFinder<Trip> {

    private final StateHistoryRepository stateHistoryRepository;

    @Override
    public ScopeEnum getScope() {
        return ScopeEnum.TRIP;
    }

    @Override
    public Optional<StateHistory> findCurrent(Trip trip) {
        return stateHistoryRepository
                .findByTripIdAndFinishDateTimeIsNull(trip.getId());
    }

    @Override
    public StateHistory buildNew(State state, Trip trip) {
        return StateHistory.builder()
                .state(state)
                .trip(trip)
                .build();
    }
}

