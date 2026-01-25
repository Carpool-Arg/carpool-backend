package com.carpool.carpool.repository.stateHistory;

import com.carpool.carpool.model.stateHistory.StateHistory;
import com.carpool.carpool.model.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
    Optional<StateHistory> findByTripAndFinishDateTimeIsNullAndReservationIdIsNull(Trip trip);

    Optional<StateHistory> findTopByTripAndFinishDateTimeIsNotNullOrderByFinishDateTimeDesc(Trip trip);

    Optional<StateHistory> findByReservationIdAndFinishDateTimeIsNull(Long idReservation);

    Optional<StateHistory> findByTripIdAndFinishDateTimeIsNull(Long idTrip);

    @Query("""
        SELECT COUNT(sh) > 0
        FROM StateHistory sh
        JOIN sh.reservation r
        JOIN sh.state s
        WHERE r.user.id = :userId
          AND sh.finishDateTime IS NULL
          AND s.name IN ('UNPAID', 'EXPIRED')
    """)
    boolean existsActiveDebtByUserId(@Param("userId") Long userId);
}
