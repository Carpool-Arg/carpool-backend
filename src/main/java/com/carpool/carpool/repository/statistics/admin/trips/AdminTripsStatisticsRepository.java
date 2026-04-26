package com.carpool.carpool.repository.statistics.admin.trips;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.carpool.carpool.model.trip.Trip;

import io.lettuce.core.dynamic.annotation.Param;

public interface AdminTripsStatisticsRepository extends JpaRepository<Trip,Long>{

    @Query(value = """
        SELECT c.name AS cityName, COUNT(r.id) AS reservationCount
        FROM reservation r
        JOIN trip_stop ts           ON (:type = 'ORIGIN'      AND r.start_city_id = ts.id)
                                    OR (:type = 'DESTINATION' AND r.destination_city_id = ts.id)
        JOIN city c                 ON ts.city_id = c.id
        JOIN state_history sh_res   ON sh_res.reservation_id = r.id
        JOIN state s_res            ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip  ON sh_trip.trip_id = r.trip_id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND ts.deleted_at IS NULL
        GROUP BY c.id, c.name
        ORDER BY reservationCount DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopCitiesByType(@Param("type") String type, @Param("limit") int limit);


    @Query(value = """
        SELECT
            SUM(t.available_seat - t.current_available_seats) AS taken,
            SUM(t.current_available_seats)                    AS untaken
        FROM trip t
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Object[] getSeatStatsHistorical();

    @Query(value = """
        SELECT
            SUM(t.available_seat - t.current_available_seats) AS taken,
            SUM(t.current_available_seats)                    AS untaken
        FROM trip t
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
    """, nativeQuery = true)
    Object[] getSeatStatsFiltered(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
