package com.carpool.carpool.controller.town;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.town.TownResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.town.ITownService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Town", description = "Operaciones relacionadas con las localidades")
@RequestMapping("/town")
@RequiredArgsConstructor
public class TownController {
    
    private final ITownService townService;

    @Operation(
        summary = "Obtener una localidad por id"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localidad obtenida con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<Response<TownResponseDTO>> getTownById(@PathVariable Long id) {
        return new ResponseEntity<>(townService.getTownById(id), HttpStatus.OK);
    }


    @Operation(
        summary = "Obtener localidades para autocompletar"
    
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Localidades obtenidas con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
    })
    @GetMapping("/autocomplete")
    public ResponseEntity<Response<List<TownResponseDTO>>> getTownsForAutocomplete(
            @RequestParam(required = false) 
            @Parameter(description = "Nombre de la localidad a buscar (mínimo 2 caracteres)", example = "Buenos")
            String name) {
        
        Response<List<TownResponseDTO>> response = townService.getTownsForAutocomplete(name, 10);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}
