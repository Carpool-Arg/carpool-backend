package com.carpool.carpool.service.review;

import java.util.List;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
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
}
