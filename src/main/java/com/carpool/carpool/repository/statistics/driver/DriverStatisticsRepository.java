package com.carpool.carpool.repository.statistics.driver;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carpool.carpool.model.trip.Trip;

public interface DriverStatisticsRepository extends JpaRepository<Trip,Long>{
 
    
    @Query(value = """
        SELECT COALESCE(SUM(ts.distance_from_previous), 0)
        FROM trip t
        JOIN vehicles v              ON t.vehicle_id = v.id
        JOIN driver d               ON v.driver_id = d.id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        JOIN trip_stop ts           ON ts.trip_id = t.id
        WHERE d.user_id = :userId
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND ts.deleted_at IS NULL
    """, nativeQuery = true)
    Double sumKmHistoricalByUserId(@Param("userId") Long userId);

    @Query(value = """
        SELECT COALESCE(SUM(ts.distance_from_previous), 0)
        FROM trip t
        JOIN vehicles v              ON t.vehicle_id = v.id
        JOIN driver d               ON v.driver_id = d.id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        JOIN trip_stop ts           ON ts.trip_id = t.id
        WHERE d.user_id = :userId
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND ts.deleted_at IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
    """, nativeQuery = true)
    Double sumKmByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    @Query(value = """
        SELECT label, COALESCE(SUM(km), 0) AS value
        FROM (
            SELECT
                CASE CAST(:groupBy AS text)
                    WHEN 'DAY'   THEN TO_CHAR(t.start_date_time, 'DD/MM/YYYY')
                    WHEN 'WEEK'  THEN TO_CHAR(DATE_TRUNC('week', t.start_date_time), 'DD/MM/YYYY')
                    WHEN 'MONTH' THEN TO_CHAR(t.start_date_time, 'MM/YYYY')
                    WHEN 'YEAR'  THEN TO_CHAR(t.start_date_time, 'YYYY')
                END AS label,
                t.start_date_time AS start_date_time,
                COALESCE(SUM(ts.distance_from_previous), 0) AS km
            FROM trip t
            JOIN vehicles v              ON t.vehicle_id = v.id
            JOIN driver d               ON v.driver_id = d.id
            JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
            JOIN state s_trip           ON s_trip.id = sh_trip.state_id
            JOIN trip_stop ts           ON ts.trip_id = t.id
            WHERE d.user_id = :userId
            AND s_trip.name = 'FINISHED'
            AND sh_trip.finish_datetime IS NULL
            AND sh_trip.reservation_id IS NULL
            AND ts.deleted_at IS NULL
            AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
            AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
            GROUP BY t.id, t.start_date_time, label
        ) sub
        GROUP BY label
        ORDER BY MIN(start_date_time)
    """, nativeQuery = true)
    List<Object[]> findKmMetricsByGrouping(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("groupBy") String groupBy
    );


    @Query(value = """
        SELECT COALESCE(SUM(t.seat_price - t.driver_price_discount), 0)
        FROM trip t
        JOIN vehicles v             ON t.vehicle_id = v.id
        JOIN driver d               ON v.driver_id = d.id
        JOIN reservation r          ON r.trip_id = t.id
        JOIN state_history sh_res   ON sh_res.reservation_id = r.id
        JOIN state s_res            ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE d.user_id = :userId
        AND s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Double sumEarningsHistoricalByUserId(@Param("userId") Long userId);

    /*
        Suma de ganancias del conductor en un rango de fechas,
        basada en reservas COMPLETED de sus viajes.
     */
    @Query(value = """
        SELECT COALESCE(SUM(t.seat_price - t.driver_price_discount), 0)
        FROM trip t
        JOIN vehicles v             ON t.vehicle_id = v.id
        JOIN driver d               ON v.driver_id = d.id
        JOIN reservation r          ON r.trip_id = t.id
        JOIN state_history sh_res   ON sh_res.reservation_id = r.id
        JOIN state s_res            ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE d.user_id = :userId
        AND s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
    """, nativeQuery = true)
    Double sumEarningsByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    /*
        Métricas de ganancias agrupadas por período.
        groupBy: YEAR → meses, MONTH → semanas, WEEK → días.
     */
    @Query(value = """
        SELECT label, COALESCE(SUM(earnings), 0) AS value
        FROM (
            SELECT
                CASE CAST(:groupBy AS text)
                    WHEN 'DAY'   THEN TO_CHAR(t.start_date_time, 'DD/MM/YYYY')
                    WHEN 'WEEK'  THEN TO_CHAR(DATE_TRUNC('week', t.start_date_time), 'DD/MM/YYYY')
                    WHEN 'MONTH' THEN TO_CHAR(t.start_date_time, 'MM/YYYY')
                    WHEN 'YEAR'  THEN TO_CHAR(t.start_date_time, 'YYYY')
                END AS label,
                t.start_date_time AS start_date_time,
                (t.seat_price - t.driver_price_discount) AS earnings
            FROM trip t
            JOIN vehicles v             ON t.vehicle_id = v.id
            JOIN driver d               ON v.driver_id = d.id
            JOIN reservation r          ON r.trip_id = t.id
            JOIN state_history sh_res   ON sh_res.reservation_id = r.id
            JOIN state s_res            ON s_res.id = sh_res.state_id
            JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
            JOIN state s_trip           ON s_trip.id = sh_trip.state_id
            WHERE d.user_id = :userId
            AND s_res.name = 'COMPLETED'
            AND sh_res.finish_datetime IS NULL
            AND s_trip.name = 'FINISHED'
            AND sh_trip.finish_datetime IS NULL
            AND sh_trip.reservation_id IS NULL
            AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
            AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
        ) sub
        GROUP BY label
        ORDER BY MIN(start_date_time)
    """, nativeQuery = true)
    List<Object[]> findEarningsMetricsByGrouping(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("groupBy") String groupBy
    );

    @Query(value = """
        SELECT COUNT(t.id)
        FROM trip t
        JOIN vehicles v             ON t.vehicle_id = v.id
        JOIN driver d               ON v.driver_id = d.id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE d.user_id = :userId
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Long countTripsHistoricalByUserId(@Param("userId") Long userId);

    @Query(value = """
        SELECT COUNT(t.id)
        FROM trip t
        JOIN vehicles v             ON t.vehicle_id = v.id
        JOIN driver d               ON v.driver_id = d.id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE d.user_id = :userId
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
    """, nativeQuery = true)
    Long countTripsByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    @Query(value = """
        SELECT label, COUNT(id) AS value
        FROM (
            SELECT
                CASE CAST(:groupBy AS text)
                    WHEN 'DAY'   THEN TO_CHAR(t.start_date_time, 'DD/MM/YYYY')
                    WHEN 'WEEK'  THEN TO_CHAR(DATE_TRUNC('week', t.start_date_time), 'DD/MM/YYYY')
                    WHEN 'MONTH' THEN TO_CHAR(t.start_date_time, 'MM/YYYY')
                    WHEN 'YEAR'  THEN TO_CHAR(t.start_date_time, 'YYYY')
                END AS label,
                t.start_date_time AS start_date_time,
                t.id AS id
            FROM trip t
            JOIN vehicles v             ON t.vehicle_id = v.id
            JOIN driver d               ON v.driver_id = d.id
            JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
            JOIN state s_trip           ON s_trip.id = sh_trip.state_id
            WHERE d.user_id = :userId
            AND s_trip.name = 'FINISHED'
            AND sh_trip.finish_datetime IS NULL
            AND sh_trip.reservation_id IS NULL
            AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
            AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
        ) sub
        GROUP BY label
        ORDER BY MIN(start_date_time)
    """, nativeQuery = true)
    List<Object[]> findTripMetricsByGrouping(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("groupBy") String groupBy
    );

    @Query(value = """
        SELECT COALESCE(SUM(tramo_km * :co2PerKm), 0)
        FROM (
            SELECT
                (SELECT COALESCE(SUM(ts2.distance_from_previous), 0)
                FROM trip_stop ts2
                WHERE ts2.trip_id = r.trip_id
                AND ts2.stop_order > ts_start.stop_order
                AND ts2.stop_order <= ts_end.stop_order
                AND ts2.deleted_at IS NULL) AS tramo_km

            FROM trip t
            JOIN vehicles v             ON t.vehicle_id = v.id
            JOIN driver d               ON v.driver_id = d.id
            JOIN reservation r          ON r.trip_id = t.id
            JOIN trip_stop ts_start     ON r.start_city_id = ts_start.id
            JOIN trip_stop ts_end       ON r.destination_city_id = ts_end.id
            JOIN state_history sh_res   ON sh_res.reservation_id = r.id
            JOIN state s_res            ON s_res.id = sh_res.state_id
            JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
            JOIN state s_trip           ON s_trip.id = sh_trip.state_id
            WHERE d.user_id = :userId
            AND s_res.name = 'COMPLETED'
            AND sh_res.finish_datetime IS NULL
            AND s_trip.name = 'FINISHED'
            AND sh_trip.finish_datetime IS NULL
            AND sh_trip.reservation_id IS NULL
        ) sub
        WHERE tramo_km > 0
    """, nativeQuery = true)
    Double calculateCo2SavedByUserId(
        @Param("userId") Long userId,
        @Param("co2PerKm") double co2PerKm
    );
}
