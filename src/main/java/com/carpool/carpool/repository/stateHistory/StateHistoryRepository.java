package com.carpool.carpool.repository.stateHistory;

import com.carpool.carpool.model.stateHistory.StateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
}
