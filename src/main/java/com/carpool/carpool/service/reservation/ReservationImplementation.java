package com.carpool.carpool.service.reservation;

import com.carpool.carpool.dto.reservation.ReservationRequestDTO;
import com.carpool.carpool.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationImplementation implements  IReservationService{

    @Override
    public Response<Void> createReservation(ReservationRequestDTO reservationRequestDTO) {
        return null;
    }

    /*
    * VALIDACIONES:
    * Que exista el viaje, que no esté lleno, que no esté cerrado, cancelado o finalizado.
    * El pasajero no puede enviar mas de una solicitud para el mismo viaje
    * La localidad origen y destino no pueden ser iguales
    * La localidad origen y destino deben existir en la tabla TripStop, en base al trip que se envia por la request
    * Verificar que la localidad origen y destino que se pasan, respeten el orden establecido en TripStop
    * */
}

