package com.carpool.carpool.repository.state;

import com.carpool.carpool.model.state.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
    State findByName(String name);
}
