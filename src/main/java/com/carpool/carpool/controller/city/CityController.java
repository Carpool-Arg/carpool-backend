package com.carpool.carpool.controller.city;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.city.CityResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.city.ICityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "City", description = "Operaciones relacionadas con las localidades")
@RequestMapping("/city")
@RequiredArgsConstructor
public class CityController {
    
    private final ICityService cityService;

    @Operation(
        summary = "Obtener una localidad por id"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localidad obtenida con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<Response<CityResponseDTO>> getCityById(@PathVariable Long id) {
        return new ResponseEntity<>(cityService.getCityById(id), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener una ciudad por su nombre"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200",description = "Localidad obtenida con éxito"),
        @ApiResponse(responseCode = "400", description = "No existe una localidad con el nombre ingresado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<Response<CityResponseDTO>> getCityByName(@PathVariable String name){
        return new ResponseEntity<>(cityService.getCityByName(name), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener localidades para autocompletar"
    
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localidades obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @GetMapping("/autocomplete")
    public ResponseEntity<Response<List<CityResponseDTO>>> getCitiesForAutocomplete(
            @RequestParam(required = false) 
            @Parameter(description = "Nombre de la localidad a buscar (mínimo 2 caracteres)", example = "Buenos")
            String name) {
        
        Response<List<CityResponseDTO>> response = cityService.getCitiesForAutocomplete(name, 10);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener una localidad por coordenadas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Localidad obtenida con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @GetMapping("/coordinates")
    public ResponseEntity<Response<CityResponseDTO>> getCityByCoordinates( @RequestParam String lat,  @RequestParam String lng) {
        return new ResponseEntity<>(cityService.getCityByCoordinates(lat, lng), HttpStatus.OK);
    }
}
