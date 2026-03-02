package com.carpool.carpool.repository.review;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carpool.carpool.model.review.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Query("""
      SELECT r
      FROM Review r
      WHERE r.targetUser.id = :userId
        AND r.deletedAt IS NULL
  """)
  List<Review> findReviewsByTargetUser(
      @Param("userId") Long userId,
      Pageable pageable
  );

    /**
     * Verifica si un usuario ya ha dejado una reseña para un viaje específico.
     * @param reviewerUserId ID del usuario que deja la reseña
     * @param tripId ID del viaje para el cual se dejó la reseña
     * @return true si ya existe una reseña del usuario para ese viaje, false en caso contrario
     */
    boolean existsByReviewerUserIdAndTripId(Long reviewerUserId, Long tripId);

    /**
     * Calcula el promedio de estrellas (rating) para un usuario específico basado en las reseñas recibidas.
     * @param userId ID del usuario para el cual se desea calcular el promedio de rating
     * @return El promedio de estrellas recibido por el usuario, o null si no tiene reseñas
     */
   @Query("""
        SELECT COUNT(r), SUM(r.stars) 
        FROM Review r 
        WHERE r.targetUser.id = :userId 
        AND r.deletedAt IS NULL
    """)
    Object getReviewStatsByUserId(@Param("userId") Long userId);

    /**
     * Obtiene la lista de reseñas recibidas por un usuario específico (chofer), excluyendo las reseñas marcadas como eliminadas.
     * @param targetUserId ID del usuario target (chofer) para el cual se desean obtener las reseñas
     * @return Lista de reseñas recibidas por el usuario target, sin incluir las reseñas eliminadas
     */
    List<Review> findByTargetUserIdAndDeletedAtIsNull(Long targetUserId);
    

    @Query("""
        SELECT r
        FROM Review r
        WHERE r.targetUser.id = :userId
          AND r.deletedAt IS NULL
          AND r.passengerToDriver = :userToDriver
          AND (CAST(:fromDate AS timestamp) IS NULL OR r.createdAt >= :fromDate)
          AND (CAST(:toDate AS timestamp) IS NULL OR r.createdAt <= :toDate)
    """)
    Page<Review> findReviewsByTargetUserWithFilters(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime dateFrom,
            @Param("toDate") LocalDateTime dateTo,
            @Param("userToDriver") boolean userToDriver,
            Pageable pageable
    );
}