package com.carpool.carpool.service.state;

import java.util.Optional;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.state.State;
import com.carpool.carpool.model.stateHistory.StateHistory;

public interface StateHistoryFinder<T> {
    ScopeEnum getScope();
    Optional<StateHistory> findCurrent(T entity);
    StateHistory buildNew(State state, T entity);
}

