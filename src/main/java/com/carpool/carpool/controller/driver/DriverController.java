package com.carpool.carpool.controller.driver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.driver.IDriverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Conductores", description = "Operaciones relacionadas con el conductor")
@RequestMapping("/drivers")
public class DriverController {
    
    @Autowired
    private IDriverService driverService;

    @Operation(
        summary = "Registrar un nuevo conductor",
        description = "Registra un nuevo conductor con campos de entrada validados."
    )
    
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Conductor creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación",
            content = @Content(mediaType = "application/json"))
    })

    @PostMapping("/become_driver")    
    ResponseEntity<Response<Void>> createDriverProfile(@Valid @RequestBody DriverRequestDTO driverRequestDTO) {
        Response<Void> serviceResponse = driverService.saveDriver(driverRequestDTO);
        return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
    }
}
