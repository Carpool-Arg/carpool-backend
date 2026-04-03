package com.carpool.carpool.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.driver.DriverLicenseVerifyRequestDTO;
import com.carpool.carpool.dto.driver.DriverPendingPageResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.driver.IDriverService;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Admin", description = "Operaciones del administrador")
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IDriverService driverService;

    @Operation(
        summary = "Obtener choferes con carnet pendiente de verificación",
        description = "Permite obtener una lista paginada de choferes con licencias pendientes"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de choferes pendientes obtenido con éxito"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/drivers/pending")
    public ResponseEntity<Response<DriverPendingPageResponseDTO>> getPendingLicenses(
        @RequestParam(required = false, defaultValue = "0") int skip,
        @RequestParam(required = false, defaultValue = "RECENT") String orderBy
    ) {
        return ResponseEntity.ok(driverService.getPendingLicenses(skip, orderBy));
    }

    @Operation(summary = "Aprobar o rechazar el carnet de un chofer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carnet procesado con éxito"),
            @ApiResponse(responseCode = "400", description = "Error de validación en los campos"),
            @ApiResponse(responseCode = "404", description = "Chofer no encontrado"),
            @ApiResponse(responseCode = "409", description = "El carnet ya fue procesado anteriormente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/drivers/{driverId}/verify")
    public ResponseEntity<Response<Void>> verifyLicense(
            @PathVariable Long driverId,
            @Valid @org.springframework.web.bind.annotation.RequestBody DriverLicenseVerifyRequestDTO dto) {
        return new ResponseEntity<>(driverService.verifyLicense(driverId, dto), HttpStatus.OK);
    }
}