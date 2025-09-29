package com.carpool.carpool.service.reservation;

import com.carpool.carpool.dto.reservation.ReservationRequestDTO;
import com.carpool.carpool.response.Response;

public interface IReservationService {
    /**
     * Metodo para crear una solicitud de reserva
     * @return Response<Void> devolviendo el mensaje si la solicitud de reserva fue creada
     */
    Response<Void> createReservation(ReservationRequestDTO reservationRequestDTO);
}
