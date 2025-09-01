package com.carpool.carpool.controller.vehicle;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.vehicle.VehicleOnlyResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleRequestDTO;
import com.carpool.carpool.dto.vehicle.VehicleUpdateRequestDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.vehicle.IVehicleService;

import java.util.List; 
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Vehiculos", description = "Operaciones relacionadas con el vehiculo")
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final IVehicleService vehicleService;

    @Operation(
            summary = "Obtener un vehículo por ID",
            description = "Obtiene los detalles de un vehículo específico por su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Response<VehicleOnlyResponseDTO>> getVehicleById(@PathVariable Long id) {
        Response<VehicleOnlyResponseDTO> serviceResponse = vehicleService.getVehicleById(id);
        return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener vehículos del chofer autenticado",
            description = "Obtiene una lista de vehículos asociados al chofer autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehiculos obtenidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron vehiculos para el chofer autenticado")
    })
    @GetMapping("/my-vehicles")
    public ResponseEntity<Response<List<VehicleResponseDTO>>> getMyVehicles(){
        Response<List<VehicleResponseDTO>> serviceResponse = vehicleService.getVehiclesByAuthenticatedDriver();
        return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
    }

    @Operation(
        summary = "Registrar un nuevo vehículo",
        description = "Registra un nuevo vehículo para un chofer específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vehiculo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación en los campos de entrada",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<Response<Void>> saveVehicle(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request para crear un vehiculo de un chofer especifico.", required = true)
                @Valid 
                @RequestBody VehicleRequestDTO vehicleRequestDTO) {
        
        Response<Void> serviceResponse = vehicleService.saveVehicle(vehicleRequestDTO);
        return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Actualizar un vehículo",
        description = "Actualiza los detalles de un vehículo existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehiculo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación en los campos de entrada",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado")
    })
    @PutMapping("/{id}") 
    public ResponseEntity<Response<Void>> updateVehicle(
            @PathVariable Long id, 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Request para actualizar un vehiculo existente.", required = true)
            @Valid
            @RequestBody VehicleUpdateRequestDTO vehicleUpdateRequestDTO) {

        Response<Void> serviceResponse = vehicleService.updateVehicle(id, vehicleUpdateRequestDTO);
        return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
    } 

    @Operation(
        summary = "Eliminar un vehículo",
        description = "Elimina un vehículo existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehiculo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Vehiculo no encontrado")
    })
    @DeleteMapping("/{id}") 
    public ResponseEntity<Response<Void>> deleteVehicle(@PathVariable Long id){
        Response<Void> serviceResponse = vehicleService.deleteVehicle(id);
        return new ResponseEntity<>(serviceResponse, HttpStatus.OK); 
    }
}

