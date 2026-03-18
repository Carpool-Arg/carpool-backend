package com.carpool.carpool.repository.review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carpool.carpool.dto.review.TripPassengerReviewDTO;
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
     * Verifica si un pasajero ya fue reseñado para un viaje específico.
     * @param targetUserId ID del pasajero que se quiere reseñar
     * @param tripId ID del viaje para el cual se dejó la reseña
     * @return true si ya existe una reseña para el usuario para ese viaje, false en caso contrario
     */
    boolean existsByTargetUserIdAndTripId(Long targetUserId, Long tripId);

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

    /**
     * Otiene las estrellas y la descripcion de una reseña a un pasajero especifico para un viaje en especifico, se 
     * usa un octional para manejar el caso de que no exista la reseña
     * @param tripId
     * @param userId el id del usuario del que se quiere obtener la reseña
     * @return
     */
    @Query("""
      SELECT new com.carpool.carpool.dto.review.TripPassengerReviewDTO(
        rev.stars,
        rev.description
      )
      FROM Review rev
      WHERE rev.trip.id = :tripId
      AND rev.targetUser.id = :userId
      AND rev.passengerToDriver = false
    """)
    Optional<TripPassengerReviewDTO> getTripPassengerReview(Long tripId, Long userId);
    

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

    @Query("""
        SELECT r
        FROM Review r
        WHERE r.reviewerUser.id = :reviewerId
          AND r.deletedAt IS NULL
          AND r.passengerToDriver = :toDriver
          AND (CAST(:fromDate AS timestamp) IS NULL OR r.createdAt >= :fromDate)
          AND (CAST(:toDate AS timestamp) IS NULL OR r.createdAt <= :toDate)
    """)
    Page<Review> findReviewsByReviewerWithFiltersPage(
        @Param("reviewerId") Long reviewerId,
        @Param("fromDate") LocalDateTime dateFrom,
        @Param("toDate") LocalDateTime dateTo,
        @Param("toDriver") boolean toDriver,
        Pageable pageable
    );

    /**
     * 
     * @param id
     * @param reviewerUserId
     * @return
     */
    Optional<Review> findByIdAndReviewerUserId(Long id, Long reviewerUserId);
}