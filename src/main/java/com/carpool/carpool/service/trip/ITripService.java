package com.carpool.carpool.service.trip;

import java.time.LocalDateTime;
import java.util.List;

import com.carpool.carpool.dto.trip.*;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.response.Response;

public interface ITripService {

    /**
     * Metodo para crear y publicar un nuevo viaje.
     * @return Response<Void> devolviendo el mensaje si el viaje fue creado
     * @throws ConflictException si alguna de las validaciones falla
     * @throws ResourceNotFoundException si el vehiculo o el estado no existen 
     */
    Response<Void> createTrip(TripRequestDTO tripRequestDTO);

    /**
     * Metodo para obtener los detalles de un viaje específico por su ID.
     * @param id identificador del viaje a solicitar
     * @return Response<TripResponseDTO> devolviendo el viaje solicitado
     * @throws ResourceNotFoundException si el viaje no existe
     */
    Response<TripResponseDTO> getTripDetails(Long id);

    /**
     * Metodo para verificar la disponibilidad de un viaje.
     * @param startDateTime La fecha y hora a partir de la cual verificar.
     * @return Response<Void> devolviendo el mensaje si el viaje es posible o no.
     * @throws ConflictException si el chofer ya tiene un viaje planificado en la fecha y hora dadas.
     */
    Response<Void> checkTripAvailability(LocalDateTime startDateTime);

    /**
     * Metodo para obtener los viajes que creó un chofer que se encuentra en la sesión
     * @return Response<TripDriverResponseDTO> con el listado de viajes en caso exitoso o una lista vacia caso contrario
     */
    Response<TripDriverResponseDTO> getTrips(String tripState);

    /**
     *  Metodo para obtener el feed inicial de viajes.
     *  Este feed muestra viajes con asientos disponibles que pasen por la localidad del usuario
     * @param userCityId El ID de la ciudad del usuario
     * @param limit El numero maximo de resultados a devolver
     * @return Response<List<TripSearchResponseDTO>> devolviendo la lista de viajes encontrados
     */
    Response<List<TripSearchResponseDTO>> getInitialFeed(Long userCityId, int limit);
    
    /**
     * Metodo para buscar viajes con filtros aplicados. 
     * @param request Objeto que contiene los filtros de busqueda
     * @param userId El ID del usuario que realiza la busqueda
     * @param limit El numero maximo de resultados a devolver
     * @return Response<List<TripSearchResponseDTO>> devolviendo la lista de viajes encontrados
     * @throws ConflictException si no se completan los campos de origen y destino en la busqueda de viajes
     */
    Response<List<TripSearchResponseDTO>> searchTrips(TripSearchRequestDTO request, int limit);

    /**
     * Metodo para determinar si un chofer es dueño de ese viaje o no 
     * @param tripId Id del viaje que se quiere comprobar si el usuarios logeado es el dueño 
     * @return Response<Boolean> devuelve un true o false
     * @throws ResourceNotFoundException El viaje creado no existe
     */
    Response<Boolean> isTripCreator(Long tripId); 

    /**
     * Metodo para calcular el precio de publicacion de un asiento de un viaje
     * @param publishedPrice El precio publicado total del viaje
     * @param availableSeats La cantidad de asientos disponibles en el viaje
     * @return Response<Double> devolviendo el precio base minimo por asiento
     * @throws ConflictException si la cantidad de asientos es menor o igual a 0 o si el precio publicado es negativo
     */
    Response<TripPriceCalculationResponseDTO> calculatePublishSeatPrice(Double publishedPrice, Integer availableCurrentSeats);
}