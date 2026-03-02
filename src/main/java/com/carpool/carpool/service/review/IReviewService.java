package com.carpool.carpool.service.review;

import java.time.LocalDate;
import java.util.List;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
import com.carpool.carpool.dto.review.ReviewRequestDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.dto.review.ReviewsToMeResponseDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.response.Response;


public interface IReviewService {

  /**
   * Metodo para obtener las reservas que le realizaron a un chofer, de manera paginada
   * @param driverId id del chofer
   * @param skip cantidad de registros que se van a saltear para el paginado
   * @param orderBy segun que atributo de la review vamos a ordenar
   * @return
   */
  Response<List<DriverReviewResponseDTO>> getDriverReviews(Long driverId, int skip, String orderBy);

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
   * Metodo que permite verificar si un usuario puede dejar una reseña para un viaje específico.
   * @param tripId ID del viaje a verificar.
   * @return Response que contiene un booleano indicando si el usuario puede dejar una reseña para el viaje, o un error si la verificación falla.
   * @throws ResourceNotFoundException si el viaje con {@code tripId} no existe.
   */
  Response<Boolean> canUserReviewTrip(Long tripId);

  /**
   * Metodo para obtener las reseñas que he recibido pudiendo filtrar y obtenerlas de manera paginada
   * @return
   */
  Response<ReviewsToMeResponseDTO> getReviewsToMe(LocalDate dateFrom, LocalDate dateTo,String role, int skip,  String orderBy);
}
