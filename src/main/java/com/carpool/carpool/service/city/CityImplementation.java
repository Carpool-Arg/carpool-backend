package com.carpool.carpool.service.city;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.city.CityResponseDTO;
import com.carpool.carpool.mappers.city.CityMapper;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CityImplementation implements ICityService {
    
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

   

    @Override
    public Response<CityResponseDTO> getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La localidad no existe."));
        
        CityResponseDTO cityResponseDTO = cityMapper.convertCityToCityResponseDTO(city);

        return ResponseUtils.buildOKResponse(List.of("Localidad obtenida con éxito."), cityResponseDTO);
    }


    @Override
    public Response<List<CityResponseDTO>> getCitiesForAutocomplete(String name, int limit) {

        if(name.isBlank() || name.trim().length() < 2){
            return ResponseUtils.buildOKResponse(List.of("Se requieren al menos 2 caracteres para la búsqueda."), new ArrayList<>());
        }

        // Dividir el nombre en palabras y crear un patrón de búsqueda
        String[] searchWords = name.trim().toLowerCase().split("\\s+");
        String pattern = "%" + String.join("%", searchWords) + "%";
        
        List<City> cities = cityRepository.findCitiesByPattern(pattern);

        if(cities.size() > limit){
            cities = cities.subList(0, limit);
        }

        List<CityResponseDTO> cityResponseDTO = cities.stream()
                .map(cityMapper::convertCityToCityResponseDTO)
                .toList();
            
        if (cityResponseDTO.isEmpty()) {
            return ResponseUtils.buildOKResponse(List.of("No se encontraron localidades que coincidan con la búsqueda."), cityResponseDTO);
        }
            
        return ResponseUtils.buildOKResponse(List.of("Localidades obtenidas con éxito."), cityResponseDTO);
    }  
        
}
