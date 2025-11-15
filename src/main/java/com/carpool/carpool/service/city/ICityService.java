package com.carpool.carpool.service.city;

import java.util.List;

import com.carpool.carpool.dto.city.CityResponseDTO;
import com.carpool.carpool.response.Response;

public interface ICityService {
    /**
     * Metodo para obtener una localidad por su ID.
     * @return Response<List<CityResponseDTO>> devolviendo la lista de todas las localidades
     */
    Response<CityResponseDTO> getCityById(Long id);

    /**
     * Metodo para obtener una localidad por su nombre.
     * @param cityNameRequestDTO una request con el nombre de la ciudad 
     * @return  la ciudad convertida a un objeto DTO
     */
    Response<CityResponseDTO> getCityByName(String name);

    /**
     * Metodo para obtener localidades para autocompletar por nombre.
     * @param name nombre de la localidad a buscar (mínimo 2 caracteres)
     * @param limit cantidad máxima de resultados a devolver
     * @return Response<CityResponseDTO> devolviendo la localidad buscada por nombre
     */
     Response<List<CityResponseDTO>> getCitiesForAutocomplete(String name, int limit);

    /**
     * Obtiene una localidad a partir de sus coordenadas geográficas.
     *
     * @param latitude  latitud de la ciudad
     * @param longitude longitud de la ciudad
     * @return un objeto Response que envuelve el CityResponseDTO correspondiente a la ciudad encontrada
     */
    Response<CityResponseDTO> getCityByCoordinates(String latitude, String longitude);
}
