package com.carpool.carpool.repository.stateHistory;

import com.carpool.carpool.enums.state.ScopeEnum;
import com.carpool.carpool.dto.user.UserDebtResponseDTO;
import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
    Optional<StateHistory> findByTripAndFinishDateTimeIsNullAndReservationIdIsNull(Trip trip);

    Optional<StateHistory> findTopByTripAndFinishDateTimeIsNotNullOrderByFinishDateTimeDesc(Trip trip);

    Optional<StateHistory> findByReservationIdAndFinishDateTimeIsNull(Long idReservation);

    Optional<StateHistory> findByTripIdAndFinishDateTimeIsNull(Long idTrip);

    @Query("""
    SELECT new com.carpool.carpool.dto.user.UserDebtResponseDTO(
        r.id,
        r.total,
        true,
        (s.name = 'EXPIRED')
    )
    FROM StateHistory sh
    JOIN sh.reservation r
    JOIN sh.state s
    WHERE r.user.id = :userId
      AND sh.finishDateTime IS NULL
      AND s.name IN ('UNPAID', 'EXPIRED')
""")
    Optional<UserDebtResponseDTO> findActiveDebtByUserId(@Param("userId") Long userId);

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
