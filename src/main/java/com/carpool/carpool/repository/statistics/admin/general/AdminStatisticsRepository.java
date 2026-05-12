package com.carpool.carpool.repository.statistics.admin.general;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carpool.carpool.model.reservation.Reservation;
 

public interface AdminStatisticsRepository extends JpaRepository<Reservation, Long> {
    
    /**
     * Calcula la suma total de las ganancias de la app de todos los viajes finalizados
     * @return
     */
    @Query(value = """
        SELECT COALESCE(SUM(t.driver_price_discount * 2), 0)
        FROM trip t
        JOIN reservation r          ON r.trip_id = t.id
        JOIN state_history sh_res   ON sh_res.reservation_id = r.id
        JOIN state s_res            ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Double sumAppEarningsHistorical();

    /**
     * Calcula la suma de las comisiones de los viajes finalizados dentro de un rango de fechas específico.
     */
    @Query(value = """
        SELECT COALESCE(SUM(t.driver_price_discount * 2), 0)
        FROM trip t
        JOIN reservation r          ON r.trip_id = t.id
        JOIN state_history sh_res   ON sh_res.reservation_id = r.id
        JOIN state s_res            ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip  ON sh_trip.trip_id = t.id
        JOIN state s_trip           ON s_trip.id = sh_trip.state_id
        WHERE s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
    """, nativeQuery = true)
    Double sumAppEarningsByDateRange(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    @Query(value = """
        SELECT COALESCE(SUM(r.total), 0)
        FROM reservation r
        JOIN state_history sh_res ON sh_res.reservation_id = r.id
        JOIN state s_res          ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip ON sh_trip.trip_id = r.trip_id
        JOIN state s_trip          ON s_trip.id = sh_trip.state_id
        WHERE s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Double sumTotalTransactedHistorical();

    @Query(value = """
        SELECT COALESCE(SUM(r.total), 0)
        FROM reservation r
        JOIN state_history sh_res  ON sh_res.reservation_id = r.id
        JOIN state s_res           ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip ON sh_trip.trip_id = r.trip_id
        JOIN state s_trip          ON s_trip.id = sh_trip.state_id
        WHERE s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR r.created_at >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR r.created_at <= :toDate)
    """, nativeQuery = true)
    Double sumTotalTransactedByDateRange(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    /**
     * Cuenta la cantidad total histórica de viajes que han sido completados 
     */
    @Query(value = """
        SELECT COUNT(t.id)
        FROM trip t
        JOIN state_history sh_trip ON sh_trip.trip_id = t.id
        JOIN state s_trip          ON s_trip.id = sh_trip.state_id
        WHERE s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Long countFinishedTripsHistorical();
    
    /**
     * Cantidad de viajes finalizados dentro de un rango de fechas especificas- 
     */
    @Query(value = """
        SELECT COUNT(t.id)
        FROM trip t
        JOIN state_history sh_trip ON sh_trip.trip_id = t.id
        JOIN state s_trip          ON s_trip.id = sh_trip.state_id
        WHERE s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR t.start_date_time >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR t.start_date_time <= :toDate)
    """, nativeQuery = true)
    Long countFinishedTripsByDateRange(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    /**
     * Calcula el total histórico de CO2 ahorrado multiplicando los kilómetros recorridos por
     * todos los pasajeros en viajes finalizados
     */
    @Query(value = """
        SELECT COALESCE(SUM(
            (SELECT COALESCE(SUM(ts2.distance_from_previous), 0)
             FROM trip_stop ts2
             WHERE ts2.trip_id = r.trip_id
               AND ts2.stop_order > ts_start.stop_order
               AND ts2.stop_order <= ts_end.stop_order
               AND ts2.deleted_at IS NULL)
        ) * :co2PerKm, 0)
        FROM reservation r
        JOIN trip_stop ts_start    ON r.start_city_id = ts_start.id
        JOIN trip_stop ts_end      ON r.destination_city_id = ts_end.id
        JOIN state_history sh_res  ON sh_res.reservation_id = r.id
        JOIN state s_res           ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip ON sh_trip.trip_id = r.trip_id
        JOIN state s_trip          ON s_trip.id = sh_trip.state_id
        WHERE s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
    """, nativeQuery = true)
    Double calculateCo2SavedHistorical(@Param("co2PerKm") double co2PerKm);

    /**
     * Calcula el CO2 ahorrado en un rango de fechas específico
     */
    @Query(value = """
        SELECT COALESCE(SUM(
            (SELECT COALESCE(SUM(ts2.distance_from_previous), 0)
             FROM trip_stop ts2
             WHERE ts2.trip_id = r.trip_id
               AND ts2.stop_order > ts_start.stop_order
               AND ts2.stop_order <= ts_end.stop_order
               AND ts2.deleted_at IS NULL)
        ) * :co2PerKm, 0)
        FROM reservation r
        JOIN trip_stop ts_start    ON r.start_city_id = ts_start.id
        JOIN trip_stop ts_end      ON r.destination_city_id = ts_end.id
        JOIN state_history sh_res  ON sh_res.reservation_id = r.id
        JOIN state s_res           ON s_res.id = sh_res.state_id
        JOIN state_history sh_trip ON sh_trip.trip_id = r.trip_id
        JOIN state s_trip          ON s_trip.id = sh_trip.state_id
        WHERE s_res.name = 'COMPLETED'
        AND sh_res.finish_datetime IS NULL
        AND s_trip.name = 'FINISHED'
        AND sh_trip.finish_datetime IS NULL
        AND sh_trip.reservation_id IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR r.created_at >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR r.created_at <= :toDate)
    """, nativeQuery = true)
    Double calculateCo2SavedByDateRange(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("co2PerKm") double co2PerKm
    );

    /**
     * 
     * Cuenta el total histórico de viajes publicados con filtro de fechas.
     */
    @Query(value = """
        SELECT COUNT(t.id)
        FROM trip t
        WHERE t.created_at >= :fromDate
        AND t.created_at < :toDate
        AND t.deleted_at IS NULL
    """, nativeQuery = true)
    Long countTripsByMonth(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

    /**
     * Cuenta el total histórico de viajes publicados sin filtro de fechas.
     */
    @Query(value = """
        SELECT COUNT(t.id)
        FROM trip t
        WHERE t.deleted_at IS NULL
    """, nativeQuery = true)
    Long countTripsHistorical();
    

}
