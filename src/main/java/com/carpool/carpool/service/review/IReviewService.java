package com.carpool.carpool.service.review;

import java.time.LocalDate;
import java.util.List;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
import com.carpool.carpool.dto.review.MyMadeReviewsResponseDTO;
import com.carpool.carpool.dto.review.ReviewDriverRequestDTO;
import com.carpool.carpool.dto.review.ReviewPassengerRequestDTO;
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
   * @param reviewDriverRequestDTO DTO que contiene la información de la reseña a crear, incluyendo la calificación, descripción y el ID del viaje.
   * @return Response que contiene el DTO de la reseña creada, o un error si la creación falla por validaciones o conflictos.
   * @throws ResourceNotFoundException si el viaje o el usuario no existen.
   * @throws ConflictException si el viaje no ha finalizado, no está pagado o el usuario ya ha dejado una reseña para ese viaje.
   */
  Response<ReviewResponseDTO> createDriverReview(ReviewDriverRequestDTO reviewDriverRequestDTO);

  /**
   * Crea una nueva reseña para un pasajero específico. El usuario autenticado es el que realiza la reseña (chofer) y el destinatario es un pasajero del viaje.
   *
   * @param reviewPassengerRequestDTO DTO que contiene la información de la reseña a crear, incluyendo la calificación, descripción y el ID del viaje.
   * @return Response que contiene el DTO de la reseña creada, o un error si la creación falla por validaciones o conflictos.
   * @throws ResourceNotFoundException si el viaje o el usuario no existen.
   * @throws ConflictException si el viaje no ha finalizado,  el usuario ya ha dejado una reseña para ese pasajero.
   */
  Response<ReviewResponseDTO> createPassengerReview(ReviewPassengerRequestDTO reviewPassengerRequestDTO);

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

 
  /**
   * Recupera de forma paginada las reseñas realizadas por el usuario autenticado.
   * Según el rol indicado, resuelve internamente si buscar reseñas hechas a choferes
   * o a pasajeros.
   * Si no se encuentran reseñas para la página solicitada, retorna un mensaje informativo.
   * @param dateFrom fecha desde (opcional). Si se indica sola, trae desde esa fecha hasta hoy.
   * @param dateTo fecha hasta (opcional). Si se indica sola, trae todo hasta esa fecha.
   * @param role rol desde el cual se hicieron las reseñas. "driver" para reseñas a pasajeros, "passenger" para reseñas a choferes.
   * @param skip cantidad de registros a saltear para el paginado.
   * @param orderBy criterio de ordenamiento: RATING_DESC, RATING_ASC o RECENT.
   * @return Response con el total y la lista paginada de reseñas realizadas.
  */
  
  Response<MyMadeReviewsResponseDTO> getMyMadeReviews(LocalDate dateFrom, LocalDate dateTo,  String role, int skip, String orderBy);

  /* 
   * Metodo que permite verificar si un chofer puede dejar una reseña para un pasajero y viaje específico
   * @param tripId ID del viaje a verificar.
   * @param passengerId ID del pasajero a verificar.
   * @return Response que contiene un booleano indicando si el chofer  puede dejar una reseña para el viaje, o un error si la verificación falla.
   * @throws ResourceNotFoundException si el viaje con {@code tripId} o el pasajero con {@code passengerId} no existe.
   */
  Response<Boolean> canDriverReviewTrip(Long tripId, Long passengerId);

  /**
   * 
   * @param reviewId
   * @return
   */
  Response<Void> deleteReview(Long reviewId);
}
