package com.carpool.carpool.controller.vehicle.type;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.vehicle.type.VehicleTypeResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.vehicle.type.IVehicleTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Tipos de Vehículo", description = "Operaciones relacionadas con los tipos de vehículo")
@RequestMapping("/vehicle-types")
@RequiredArgsConstructor
public class VehicleTypeController {

    private final IVehicleTypeService vehicleTypeService;

   @Operation(
        summary = "Obtener todos los tipos de vehículo",
        description = "Obtiene una lista de todos los tipos de vehículo disponibles."
    
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehiculos obtenidos exitosamente")
    })
    @GetMapping
    public ResponseEntity<Response<List<VehicleTypeResponseDTO>>> getAllVehicleTypes() {
        return new ResponseEntity<>(vehicleTypeService.getAllVehicleTypes(), HttpStatus.OK);
    }
    
}
