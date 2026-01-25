package com.carpool.carpool.repository.stateHistory;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
    Optional<StateHistory> findByTripAndFinishDateTimeIsNullAndReservationIdIsNull(Trip trip);

    Optional<StateHistory> findTopByTripAndFinishDateTimeIsNotNullOrderByFinishDateTimeDesc(Trip trip);

    Optional<StateHistory> findByReservationIdAndFinishDateTimeIsNull(Long idReservation);

    Optional<StateHistory> findByTripIdAndFinishDateTimeIsNull(Long idTrip);

    @Query("""
        SELECT COUNT(sh) > 0 FROM StateHistory sh 
        WHERE sh.trip = :trip 
        AND sh.state.name = :name 
        AND sh.state.scope = :scope 
        AND sh.finishDateTime IS NULL
    """)
    boolean isCurrentState(@Param("trip") Trip trip, @Param("name") String name, @Param("scope") ScopeEnum scope);

    @Query("""
        SELECT sh FROM StateHistory sh 
        WHERE sh.trip = :trip 
        AND sh.finishDateTime IS NULL 
        AND sh.reservation IS NULL
    """)
    Optional<StateHistory> findCurrentStateByTrip(@Param("trip") Trip trip);

}
