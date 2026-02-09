package com.carpool.carpool.repository.state;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.state.State;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByNameAndScope(String name, ScopeEnum scope);
    
	@Query("""
			SELECT s.name
			FROM State s
			WHERE s.scope = :scope
			  AND s.name IN :names
			""")
	List<String> findExistingStateNames(@Param("scope") ScopeEnum scope, @Param("names") List<String> names);
}
