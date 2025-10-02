package com.carpool.carpool.service.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.trip.TripRequestDTO;
import com.carpool.carpool.dto.trip.TripResponseDTO;
import com.carpool.carpool.dto.trip.TripSearchRequestDTO;
import com.carpool.carpool.dto.trip.TripSearchResponseDTO;
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

    /**
     * Metodo para verificar la disponibilidad de un viaje.
     * @param startDateTime La fecha y hora a partir de la cual verificar.
     * @return Response<Void> devolviendo el mensaje si el viaje es posible o no.
     * 
     */
    Response<Void> checkTripAvailability(LocalDateTime startDateTime);

    
    /**
     *  Metodo para obtener el feed inicial de viajes.
     *  Este feed muestra viajes con asientos disponibles que pasen por la localidad del usuario
     * @param userCityId
     * @param limit
     * @param offset
     * @return Response<List<TripSearchResponseDTO>> devolviendo la lista de viajes encontrados
     */
    Response<List<TripSearchResponseDTO>> getInitialFeed(Long userCityId, int limit);
    
    /**
     * Metodo para buscar viajes con filtros aplicados. 
     * @param request
     * @param limit
     * @param offset
     * @return Response<List<TripSearchResponseDTO>> devolviendo la lista de viajes encontrados
     */
    Response<List<TripSearchResponseDTO>> searchTrips(TripSearchRequestDTO request, int limit);
}