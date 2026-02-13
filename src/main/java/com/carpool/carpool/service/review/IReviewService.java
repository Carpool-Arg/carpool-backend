package com.carpool.carpool.service.review;

import java.util.List;

import com.carpool.carpool.dto.review.ReviewRequestDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.response.Response;


public interface IReviewService {
    /**
     * Crea una nueva reseña para un viaje específico. El usuario autenticado es el que realiza la reseña (pasajero) y el destinatario es el chofer del viaje.
     * 
     * @param reviewRequestDTO DTO que contiene la información de la reseña a crear, incluyendo la calificación, descripción y el ID del viaje.
     * @return Response que contiene el DTO de la reseña creada, o un error si la creación falla por validaciones o conflictos.
     * @throws ResourceNotFoundException si el viaje o el usuario no existen.
     * @throws ConflictException si el viaje no ha finalizado, no está pagado o el usuario ya ha dejado una reseña para ese viaje.
     */
    Response<ReviewResponseDTO> createReview(ReviewRequestDTO reviewRequestDTO);

    /**
     * Obtiene la lista de reseñas recibidas por un usuario específico (chofer). Solo se devuelven las reseñas que no han sido marcadas como eliminadas.
     * @param targetUserId ID del usuario target (chofer) para el cual se desean obtener las reseñas
     * @return Response que contiene la lista de ReviewResponseDTO con las reseñas recibidas por el usuario target, o un error si el usuario no existe.
     * @throws ResourceNotFoundException si el usuario target no existe.
     */
    Response<List<ReviewResponseDTO>> getReviewsByTargetUser(Long targetUserId);

    /**
     * Verifica si un usuario autenticado puede dejar una reseña para un viaje específico.
     * @param tripId Verifica si un usuario autenticado puede dejar una reseña para un viaje específico.
     * @return Response que contiene un booleano indicando si el usuario puede dejar una reseña para el viaje, o un error si la verificación falla.
     * @throws ResourceNotFoundException si el viaje con {@code tripId} no existe.
     */
    Response<Boolean> canUserReviewTrip(Long tripId);
}
