package com.carpool.carpool.repository.statistics.passenger;

import com.carpool.carpool.model.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface PassengerStatisticRepository extends JpaRepository<Reservation,Long>, JpaSpecificationExecutor<Reservation> {
    Optional<Reservation> findByUserId(Long userId);


    @Query(value = """
    SELECT COALESCE(SUM(
            (SELECT COALESCE(SUM(ts2.distance_from_previous), 0)
            FROM trip_stop ts2
            WHERE ts2.trip_id = r.trip_id
            AND ts2.stop_order > ts_start.stop_order
            AND ts2.stop_order <= ts_end.stop_order
            AND ts2.deleted_at IS NULL)
        ), 0)
        FROM reservation r
        JOIN trip_stop ts_start     ON r.start_city_id = ts_start.id
        JOIN trip_stop ts_end       ON r.destination_city_id = ts_end.id
        JOIN state_history sh       ON r.id = sh.reservation_id
        JOIN state s                ON s.id = sh.state_id
        JOIN state_history sh_trip  ON r.trip_id = sh_trip.trip_id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE r.user_id = :userId
        AND s.name = 'COMPLETED'
        AND sh.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Double sumKmHistoricalByUserId(@Param("userId") Long userId);

    @Query(value = """
        SELECT COUNT(r.id)
        FROM reservation r
        JOIN state_history sh       ON r.id = sh.reservation_id
        JOIN state s                ON s.id = sh.state_id
        JOIN state_history sh_trip  ON r.trip_id = sh_trip.trip_id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE r.user_id = :userId
        AND s.name = 'COMPLETED'
        AND sh.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Long countCompletedTripsHistoricalByUserId(@Param("userId") Long userId);

    /*
        Consulta para calcular la suma total de kilómetros recorridos
        por un usuario con rol de pasajero en un rango de fechas específico.
     */
    @Query(value = """
        SELECT COALESCE(SUM(
            (SELECT COALESCE(SUM(ts2.distance_from_previous), 0)
            FROM trip_stop ts2
            WHERE ts2.trip_id = r.trip_id
            AND ts2.stop_order > ts_start.stop_order
            AND ts2.stop_order <= ts_end.stop_order
            AND ts2.deleted_at IS NULL)
        ), 0)
        FROM reservation r
        JOIN trip_stop ts_start     ON r.start_city_id = ts_start.id
        JOIN trip_stop ts_end       ON r.destination_city_id = ts_end.id
        JOIN state_history sh       ON r.id = sh.reservation_id
        JOIN state s                ON s.id = sh.state_id
        JOIN state_history sh_trip  ON r.trip_id = sh_trip.trip_id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE r.user_id = :userId
        AND s.name = 'COMPLETED'
        AND sh.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR r.created_at >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR r.created_at <= :toDate)
    """, nativeQuery = true)
    Double sumKmByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );
    
  
    /*
        Método empleado para contar la cantidad total de viajes completados
        por un pasajero en un rango de fechas específico.
     */
    @Query(value = """
        SELECT COUNT(r.id)
        FROM reservation r
        JOIN state_history sh       ON r.id = sh.reservation_id
        JOIN state s                ON s.id = sh.state_id
        JOIN state_history sh_trip  ON r.trip_id = sh_trip.trip_id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE r.user_id = :userId
        AND s.name = 'COMPLETED'
        AND sh.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR r.created_at >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR r.created_at <= :toDate)
    """, nativeQuery = true)
    Long countCompletedTripsByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    /*
        Obtiene metricas mensuales de viajes completados por un pasajero,
        agrupados por mes y año para un rango de fechas específico.
     */
    @Query(value = """
        SELECT label, COUNT(id) AS value
        FROM (
            SELECT
                CASE :groupBy
                    WHEN 'DAY'   THEN TO_CHAR(r.created_at, 'DD/MM/YYYY')
                    WHEN 'WEEK'  THEN TO_CHAR(r.created_at, 'DD/MM/YYYY')
                    WHEN 'MONTH' THEN TO_CHAR(r.created_at, 'MM/YYYY')
                    WHEN 'YEAR'  THEN TO_CHAR(r.created_at, 'YYYY')
                END AS label,
                r.created_at AS created_at,
                r.id AS id
            FROM reservation r
            JOIN state_history sh       ON r.id = sh.reservation_id
            JOIN state s                ON s.id = sh.state_id
            JOIN state_history sh_trip  ON r.trip_id = sh_trip.trip_id
            JOIN state s_trip           ON s_trip.id = sh_trip.state_id
            WHERE r.user_id = :userId
            AND s.name = 'COMPLETED'
            AND sh.finish_datetime IS NULL
            AND s_trip.name = 'FINISHED'
            AND sh_trip.finish_datetime IS NULL
            AND sh_trip.reservation_id IS NULL
            AND (CAST(:fromDate AS timestamp) IS NULL OR r.created_at >= :fromDate)
            AND (CAST(:toDate AS timestamp) IS NULL OR r.created_at <= :toDate)
        ) sub
        GROUP BY label
        ORDER BY MIN(created_at)
    """, nativeQuery = true)
    List<Object[]> findTripMetricsByGrouping(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("groupBy") String groupBy
    );

    /*
        Obtiene métricas mensuales de kilómetros recorridos por un pasajero,
        agrupados por mes y año para un rango de fechas específico.
     */
    @Query(value = """
        SELECT label, COALESCE(SUM(km), 0) AS value
        FROM (
            SELECT 
                CASE :groupBy
                    WHEN 'DAY'   THEN TO_CHAR(r.created_at, 'DD/MM/YYYY')
                    WHEN 'WEEK'  THEN TO_CHAR(r.created_at, 'DD/MM/YYYY')
                    WHEN 'MONTH' THEN TO_CHAR(r.created_at, 'MM/YYYY')
                    WHEN 'YEAR'  THEN TO_CHAR(r.created_at, 'YYYY')
                END AS label,
                r.created_at AS created_at,
                (SELECT COALESCE(SUM(ts2.distance_from_previous), 0)
                FROM trip_stop ts2
                WHERE ts2.trip_id = r.trip_id
                AND ts2.stop_order > ts_start.stop_order
                AND ts2.stop_order <= ts_end.stop_order
                AND ts2.deleted_at IS NULL) AS km
            FROM reservation r
            JOIN trip_stop ts_start     ON r.start_city_id = ts_start.id
            JOIN trip_stop ts_end       ON r.destination_city_id = ts_end.id
            JOIN state_history sh       ON r.id = sh.reservation_id
            JOIN state s                ON s.id = sh.state_id
            JOIN state_history sh_trip  ON r.trip_id = sh_trip.trip_id
            JOIN state s_trip           ON s_trip.id = sh_trip.state_id
            WHERE r.user_id = :userId
            AND s.name = 'COMPLETED'
            AND sh.finish_datetime IS NULL
            AND s_trip.name = 'FINISHED'
            AND sh_trip.finish_datetime IS NULL
            AND sh_trip.reservation_id IS NULL
            AND (CAST(:fromDate AS timestamp) IS NULL OR r.created_at >= :fromDate)
            AND (CAST(:toDate AS timestamp) IS NULL OR r.created_at <= :toDate)
        ) sub
        GROUP BY label
        ORDER BY MIN(created_at)
    """, nativeQuery = true)
    List<Object[]> findKmMetricsByGrouping(
        @Param("userId") Long userId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("groupBy") String groupBy
    );

    @Query(value = """
    SELECT COALESCE(SUM(
            tramo_km * :co2PerKm * pasajeros_compartidos
        ), 0)
        FROM (
            SELECT 
                (SELECT COALESCE(SUM(ts2.distance_from_previous), 0)
                FROM trip_stop ts2
                WHERE ts2.trip_id = r.trip_id
                AND ts2.stop_order > ts_start.stop_order
                AND ts2.stop_order <= ts_end.stop_order
                AND ts2.deleted_at IS NULL) AS tramo_km,

                (SELECT COUNT(r2.id)
                FROM reservation r2
                JOIN trip_stop ts2_start ON r2.start_city_id = ts2_start.id
                JOIN trip_stop ts2_end   ON r2.destination_city_id = ts2_end.id
                JOIN state_history sh2   ON r2.id = sh2.reservation_id
                JOIN state s2            ON s2.id = sh2.state_id
                WHERE r2.trip_id = r.trip_id
                AND r2.id != r.id
                AND s2.name = 'COMPLETED'
                AND sh2.finish_datetime IS NULL
                AND ts2_start.stop_order < ts_end.stop_order
                AND ts2_end.stop_order > ts_start.stop_order
                ) AS pasajeros_compartidos

            FROM reservation r
            JOIN trip_stop ts_start     ON r.start_city_id = ts_start.id
            JOIN trip_stop ts_end       ON r.destination_city_id = ts_end.id
            JOIN state_history sh       ON r.id = sh.reservation_id
            JOIN state s                ON s.id = sh.state_id
            JOIN state_history sh_trip  ON r.trip_id = sh_trip.trip_id
            JOIN state s_trip           ON s_trip.id = sh_trip.state_id
            WHERE r.user_id = :userId
            AND s.name = 'COMPLETED'
            AND sh.finish_datetime IS NULL
            AND s_trip.name = 'FINISHED'
            AND sh_trip.finish_datetime IS NULL
            AND sh_trip.reservation_id IS NULL
        ) sub
        WHERE pasajeros_compartidos > 0
    """, nativeQuery = true)
    Double calculateCo2SavedByUserId(
        @Param("userId") Long userId,
        @Param("co2PerKm") double co2PerKm
    );
}
