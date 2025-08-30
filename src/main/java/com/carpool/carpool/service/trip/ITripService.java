package com.carpool.carpool.service.trip;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.response.Response;

public interface ITripService {

    /**
     *
     * @return Response<Void> devolviendo el mensaje si el viaje fue creado
     */
    Response<Void> createTrip(TripRequestDTO tripRequestDTO);
}
