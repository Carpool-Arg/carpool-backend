package com.carpool.carpool.controller.driver;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.dto.driver.DriverResponseDTO;
import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.driver.IDriverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Conductores", description = "Operaciones relacionadas con el conductor")
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    
    private final IDriverService driverService;

    @Operation(
        summary =  "Obtener el perfil del conductor autenticado",
        description = "Permite al conductor autenticado obtener su perfil"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil obtenido con éxito"),
        @ApiResponse(responseCode = "404", description = "No se encontró el perfil"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping()
    public ResponseEntity<Response<DriverResponseDTO>> getMyDriverProfile() {
        return ResponseEntity.ok(driverService.getMyDriverProfile());
    }

    @Operation(
        summary = "Registrar un nuevo conductor",
        description = "Registra un nuevo conductor con campos de entrada validados, junto a su carnet de conducir y devuelve los tokens JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Conductor creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Conflicto: el usuario ya es conductor o ocurrió otro error")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<TokenResponseDTO>> createDriverProfile(
            @RequestPart("driverRequestDTO") @Valid DriverRequestDTO driverRequestDTO,
            @RequestPart("frontLicensePhoto") MultipartFile frontLicensePhoto,
            @RequestPart("backLicensePhoto") MultipartFile backLicensePhoto) {
        
        Response<TokenResponseDTO> serviceResponse = driverService.saveDriver(driverRequestDTO, frontLicensePhoto, backLicensePhoto);
        return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
    }
}
