package com.carpool.carpool.repository.statistics.admin.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carpool.carpool.model.user.User;

public interface AdminUserStatisticsRepository  extends JpaRepository<User, Long>{

    @Query(value = """
        SELECT
            CASE
                WHEN COUNT(u.id) = 0 THEN 0
                ELSE ROUND(
                    COUNT(d.id) * 100.0 / COUNT(u.id),
                    2
                )
            END
        FROM users u
        LEFT JOIN driver d ON d.user_id = u.id
        WHERE u.status = 'ACTIVE'
        AND u.deleted_at IS NULL
    """, nativeQuery = true)
    Double calculateDriverPercentage();

    // Total de choferes
    @Query(value = """
        SELECT COUNT(d.id)
        FROM driver d
    """, nativeQuery = true)
    Long countTotalDrivers();

    // Total de choferes habilitados
    @Query(value = """
        SELECT COUNT(d.id)
        FROM driver d
        WHERE d.license_status = 'APPROVED'
    """, nativeQuery = true)
    Long countTotalActiveDriversDrivers();

    
    // Total histórico de usuarios verificados
    @Query(value = """
        SELECT COUNT(u.id)
        FROM users u
        WHERE u.status = 'ACTIVE'
        AND u.deleted_at IS NULL
    """, nativeQuery = true)
    Long countVerifiedUsersHistorical();

    // Nuevos usuarios por período agrupados
    @Query(value = """
        SELECT label, COUNT(id) AS value
        FROM (
            SELECT
                CASE CAST(:groupBy AS text)
                    WHEN 'DAY'   THEN TO_CHAR(u.created_at, 'DD/MM/YYYY')
                    WHEN 'WEEK'  THEN TO_CHAR(u.created_at, 'DD/MM/YYYY')
                    WHEN 'MONTH' THEN TO_CHAR(u.created_at, 'MM/YYYY')
                    WHEN 'YEAR'  THEN TO_CHAR(u.created_at, 'YYYY')
                END AS label,
                u.created_at AS created_at,
                u.id AS id
            FROM users u
            WHERE u.deleted_at IS NULL
            AND (CAST(:fromDate AS timestamp) IS NULL OR u.created_at >= :fromDate)
            AND (CAST(:toDate AS timestamp) IS NULL OR u.created_at <= :toDate)
        ) sub
        GROUP BY label
        ORDER BY MIN(created_at)
    """, nativeQuery = true)
    List<Object[]> findNewUsersByGrouping(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("groupBy") String groupBy
    );

    // Total de nuevos usuarios filtrado por fechas
    @Query(value = """
        SELECT COUNT(u.id)
        FROM users u
        WHERE u.deleted_at IS NULL
        AND (CAST(:fromDate AS timestamp) IS NULL OR u.created_at >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR u.created_at <= :toDate)
    """, nativeQuery = true)
    Long countNewUsersByDateRange(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );

}
