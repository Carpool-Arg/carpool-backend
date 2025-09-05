package com.carpool.carpool.service.town;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.town.TownResponseDTO;
import com.carpool.carpool.mappers.town.TownMapper;
import com.carpool.carpool.model.province.town.Town;
import com.carpool.carpool.repository.town.TownRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TownImplementation implements ITownService {
    
    private final TownRepository townRepository;
    private final TownMapper townMapper;

   

    @Override
    public Response<TownResponseDTO> getTownById(Long id) {
        Town townn = townRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La localidad no existe."));
        
        TownResponseDTO townResponseDTO = townMapper.convertTownToTownResponseDTO(townn);

        return ResponseUtils.buildOKResponse(List.of("Localidad obtenida con éxito."), townResponseDTO);
    }


    @Override
    public Response<List<TownResponseDTO>> getTownsForAutocomplete(String name, int limit) {

        if(name.isBlank() || name.trim().length() < 2){
            return ResponseUtils.buildOKResponse(List.of("Se requieren al menos 2 caracteres para la búsqueda."), new ArrayList<>());
        }
        
        List<Town> towns = townRepository.findByNameStartingWithIgnoreCase(name.trim());

        if(towns.size() > limit){
            towns = towns.subList(0, limit);
        }

        List<TownResponseDTO> townResponseDTO = towns.stream()
                .map(townMapper::convertTownToTownResponseDTO)
                .toList();
            
         if (townResponseDTO.isEmpty()) {
            return ResponseUtils.buildOKResponse(List.of("No se encontraron localidades que coincidan con la búsqueda."), townResponseDTO);
        }
            
        return ResponseUtils.buildOKResponse(List.of("Localidades obtenidas con éxito."), townResponseDTO);
    }



    

    

    
        
}
