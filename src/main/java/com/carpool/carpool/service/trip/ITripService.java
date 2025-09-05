package com.carpool.carpool.service.trip;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.response.Response;

public interface ITripService {

    /**
     * Metodo para crear y publicar un nuevo viaje.
     * @return Response<Void> devolviendo el mensaje si el viaje fue creado
     */
    Response<Void> createTrip(TripRequestDTO tripRequestDTO);

    /**
     * Metodo para obtener los detalles de un viaje específico por su ID.
     * @param id identificador del viaje a solicitar
     * @return Response<TripResponseDTO> devolviendo el viaje solicitado
     */
    Response<TripResponseDTO> getTripDetails(Long id);
}
