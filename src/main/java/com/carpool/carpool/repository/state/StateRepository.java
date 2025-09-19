package com.carpool.carpool.repository.state;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.state.State;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByNameAndScope(String name, ScopeEnum scope);
}
