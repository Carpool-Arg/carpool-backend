package com.carpool.carpool.service.town;

import java.util.List;
import com.carpool.carpool.dto.town.TownResponseDTO;
import com.carpool.carpool.response.Response;

public interface ITownService {
    /**
     * Metodo para obtener una localidad por su ID.
     * @return Response<List<TownResponseDTO>> devolviendo la lista de todas las localidades
     */
    Response<TownResponseDTO> getTownById(Long id);

    /**
     * Metodo para obtener localidades para autocompletar por nombre.
     * @param name nombre de la localidad a buscar (mínimo 2 caracteres)
     * @param limit cantidad máxima de resultados a devolver
     * @return Response<TownResponseDTO> devolviendo la localidad buscada por nombre
     */
     Response<List<TownResponseDTO>> getTownsForAutocomplete(String name, int limit);
}
