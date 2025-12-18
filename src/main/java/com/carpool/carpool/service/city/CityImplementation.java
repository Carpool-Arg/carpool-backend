package com.carpool.carpool.service.city;

import java.text.Normalizer;
import java.util.List;

import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.service.setting.ISettingService;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.city.CityResponseDTO;
import com.carpool.carpool.exception.BadRequestException;
import com.carpool.carpool.exception.NoContentException;
import com.carpool.carpool.mappers.city.CityMapper;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;
import static com.carpool.carpool.utils.TextUtils.normalize;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
public class CityImplementation implements ICityService {
    
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;
    private final ISettingService settingService;

    @Override
    public Response<CityResponseDTO> getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La localidad no existe."));
        
        CityResponseDTO cityResponseDTO = cityMapper.convertCityToCityResponseDTO(city);

        return ResponseUtils.buildOKResponse(List.of("Localidad obtenida con éxito."), cityResponseDTO);
    }

    @Override
    public Response<CityResponseDTO> getCityByName(String name) {
        City city = cityRepository.findByName(normalize(name))
            .orElseThrow(()-> new ResourceNotFoundException("No existe una localidad con el nombre ingresado"));
        CityResponseDTO cityResponseDTO = cityMapper.convertCityToCityResponseDTO(city);
        return ResponseUtils.buildOKResponse(List.of("Localidad obtenida con éxito."), cityResponseDTO);
    }  

    @Override
    public Response<List<CityResponseDTO>> getCitiesForAutocomplete(String name, int limit) {

      if (name.isBlank() || name.trim().length() < 2) {
            throw new BadRequestException("Se requieren al menos 2 caracteres para la búsqueda.");
        }

        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        String nameNormalized = normalized.replaceAll("\\p{M}", "").toLowerCase();

        // Dividir el nombre en palabras y crear un patrón de búsqueda
        String[] searchWords = nameNormalized.trim().toLowerCase().split("\\s+");
        String pattern = "%" + String.join("%", searchWords) + "%";
        
        List<City> cities = cityRepository.findCitiesByPattern(pattern);

        if(cities.size() > limit){
            cities = cities.subList(0, limit);
        }

        List<CityResponseDTO> cityResponseDTO = cities.stream()
                .map(cityMapper::convertCityToCityResponseDTO)
                .toList();
            
        if(cityResponseDTO.isEmpty()){
            return ResponseUtils.buildOKResponse(List.of("No se encontraron localidades que coincidan con la búsqueda."), cityResponseDTO);
        }
            
        return ResponseUtils.buildOKResponse(List.of("Localidades obtenidas con éxito."), cityResponseDTO);
    }

    @Override
    public Response<CityResponseDTO> getCityByCoordinates(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);

        int minimumCityDistance = settingService.getMinimumCityDistance();

        City city = cityRepository.findCityByCoordinates(lat, lng, minimumCityDistance)
                .orElseThrow(() -> new ResourceNotFoundException("La localidad no existe."));

        CityResponseDTO cityResponseDTO = cityMapper.convertCityToCityResponseDTO(city);

        return ResponseUtils.buildOKResponse(List.of("Localidad obtenida con éxito."), cityResponseDTO);
    }
}
