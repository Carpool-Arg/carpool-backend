package com.carpool.carpool.controller.licenseClass;

import com.carpool.carpool.dto.licenseClass.LicenseClassResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.licenseClass.ILicenseClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name ="Tipos de licencias de conducir", description = "Operaciones relacionadas con las licencoias")
@RequestMapping("/license-class")
@RequiredArgsConstructor
public class LicenseClassController {
    private final ILicenseClassService licenseClassService;

    @Operation(
            summary = "Obtener todos los tipos de licencias, icluyendo Id y Nombre.",
            description = "Obteniene una lista con todos los tipos de licencias que se incluyen en la republica argentina."
    )
    @ApiResponses(value =  {
            @ApiResponse(responseCode = "200", description = "Listados de licencias obtenida con exito."),
            @ApiResponse(responseCode =  "401", description =  "Debes estar autenticado para realiza estas acciones"),
            @ApiResponse(responseCode =  "500", description = "Error interno")

    })
    @GetMapping
    public ResponseEntity<Response<List<LicenseClassResponseDTO>>> getAllLicenseClass(){
        return new ResponseEntity<>(licenseClassService.getAllLicenseClasses(), HttpStatus.OK);
    }
}
