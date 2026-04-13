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
     *
     * @return Response<Void> devolviendo el mensaje si el viaje fue creado
     * @throws ConflictException         si alguna de las validaciones falla
     * @throws ResourceNotFoundException si el vehiculo o el estado no existen
     */
    Response<Void> createTrip(TripRequestDTO tripRequestDTO);

    /**
     * Metodo para obtener los detalles de un viaje específico por su ID.
     *
     * @param id identificador del viaje a solicitar
     * @return Response<TripResponseDTO> devolviendo el viaje solicitado
     * @throws ResourceNotFoundException si el viaje no existe
     */
    Response<TripResponseDTO> getTripDetails(Long id);

    /**
     * Metodo para que un chofer pueda obtener los detalles de su viaje
     * Realiza control de permisos para validar que el viaje sea del chofer que esta en 
     * sesion
     *
     * @param id identificador del viaje a solicitar
     * @return Response<TripResponseDTO> devolviendo el viaje solicitado
     * @throws ResourceNotFoundException si el viaje no existe
     */
    Response<TripResponseDTO> getMyTripDetails(Long id);

    /**
     * Metodo para obtener los detalles de un viaje específico por su ID, para su EDICION.
     *
     * @param id identificador del viaje a solicitar
     * @return Response<TripResponseDTO> devolviendo el viaje solicitado
     * @throws ResourceNotFoundException si el viaje no existe, o no se puede editar
     */
    Response<TripResponseDTO> getTripDetailsForEdit(Long id);

    /**
     * Metodo para obtener el historial de viajes de un pasajero
     * @param namesStateTrip	Lista con nombre de estados de un viaje
     * @param skip				Numero de pagina
     * @return Response {@link TripHistoryUserResponseDTO} que contiene el historial de viajes del pasajero
     */
    Response<TripHistoryUserResponseDTO> getHistoryTripUser(List<String> namesStateTrip, int skip);

    /**
     * Metodo para verificar la disponibilidad de un viaje.
     *
     * @param startDateTime La fecha y hora a partir de la cual verificar.
     * @param idTrip id del viaje que NO queres tener en cuenta en la comprobacion
     * @return Response<Void> devolviendo el mensaje si el viaje es posible o no.
     * @throws ConflictException si el chofer ya tiene un viaje planificado en la
     *                           fecha y hora dadas.
     */
    Response<Void> checkTripAvailability(LocalDateTime startDateTime, Long idTrip, Long idOriginCity, Long idDestinationCity);

    /**
     * Metodo para obtener los viajes que creó un chofer que se encuentra en la
     * sesión
     *
     * @return Response<TripDriverResponseDTO> con el listado de viajes en caso
     *         exitoso o una lista vacia caso contrario
     */
    Response<TripDriverResponseDTO> getTrips(List<String> tripState);

    /**
     * Metodo para obtener el feed inicial de viajes.
     * Este feed muestra viajes con asientos disponibles que pasen por la localidad
     * del usuario
     *
     * @param userCityId El ID de la ciudad del usuario
     * @param limit      El numero maximo de resultados a devolver
     * @return Response<List<TripSearchResponseDTO>> devolviendo la lista de viajes
     *         encontrados
     */
    Response<List<TripSearchResponseDTO>> getInitialFeed(Long userCityId, int limit);

    /**
     * Metodo para buscar viajes con filtros aplicados.
     *
     * @param request Objeto que contiene los filtros de busqueda
     * @param userId  El ID del usuario que realiza la busqueda
     * @param limit   El numero maximo de resultados a devolver
     * @return Response<List<TripSearchResponseDTO>> devolviendo la lista de viajes
     *         encontrados
     * @throws ConflictException si no se completan los campos de origen y destino
     *                           en la busqueda de viajes
     */
    Response<List<TripSearchResponseDTO>> searchTrips(TripSearchRequestDTO request, int limit);

    /**
     * Metodo para determinar si un chofer es dueño de ese viaje o no
     *
     * @param tripId Id del viaje que se quiere comprobar si el usuarios logeado es
     *               el dueño
     * @return Response<Boolean> devuelve un true o false
     * @throws ResourceNotFoundException El viaje creado no existe
     */
    Response<Boolean> isTripCreator(Long tripId);

    /**
     * Metodo para calcular el precio de publicacion de un asiento de un viaje
     *
     * @param publishedPrice El precio publicado total del viaje
     * @param availableSeats La cantidad de asientos disponibles en el viaje
     * @return Response<Double> devolviendo el precio base minimo por asiento
     * @throws ConflictException si la cantidad de asientos es menor o igual a 0 o
     *                           si el precio publicado es negativo
     */
    Response<TripPriceCalculationResponseDTO> calculatePublishSeatPrice(Double publishedPrice,
            Integer availableCurrentSeats);

    /**
     * Metodo para iniciar un viaje, cambiando el estado del mismo
     * y de las reservas del mismo viaje.
     *
     * @param tripId Id del viaje a iniciar
     * @return Response<Void> 
     * @throws ResourceNotFoundException
     * @throws ConflictException
     */
    Response<Void> startTrip(Long tripId);

    /**
     * Metodo para cancelar un viaje, cambiando el estado del mismo
     * y de las reservas del mismo viaje.
     *
     * @param TripCancellRequestDTO ID y motivo de la cancelacion si corresponde
     * @return Response<Void>
     * @throws ResourceNotFoundException
     * @throws ConflictException
     */
    Response<Void> cancelTrip(TripCancellRequestDTO tripCancellRequestDTO);

    /**
     * Metodo para devolver el viaje en progreso de un chofer
     * 
     * @return CurrentTripResponseDTO que contiene todos los datos necesarios del
     *         viaje y sus paradas intermedias
     */
    Response<CurrentTripResponseDTO> getCurrentTrip();

    /**
     * Metodo para que el chofer indique que llego a una determinada parada
     * intermedia de su viaje actual,
     * pasando todas las reservas que finalicen en esa parada a estado UNPAID
     * 
     * @param tripArriveRequestDTO contiene el id de la parda intermedia que desea
     *                             ser cerrada
     * @return una respuesta vacia con mensajes que indican el resultado de la
     *         peticion
     */
    Response<Void> arriveTripStop(TripArriveRequestDTO tripArriveRequestDTO);
    
    Response<Void> updateTrip(TripUpdateRequestDTO tripUpdateRequestDTO);
    
    /**
     * Metodo para obtener los pasajeros que se participaron d eun viaje y tienen sus reservas en estados especificos
     * @param idTrip
     * @return
     */
    Response<TripPassengersResponseDTO> getTripPassengers(Long idTrip);
    
}