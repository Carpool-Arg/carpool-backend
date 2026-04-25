package com.carpool.carpool.controller.media;

import com.carpool.carpool.dto.driver.LicenseUrlsResponse;
import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.media.IMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Media", description = "Operaciones relacionadas con archivos en R2")
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final IMediaService mediaService;

    @Operation(
        summary = "Obtener URL de foto de perfil"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URL obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no tiene foto de perfil"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/profile")
    public ResponseEntity<Response<String>> getProfilePictureUrl() {
        return new ResponseEntity<>(mediaService.getProfilePictureUrl(), HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener URLs de fotos del carnet (frente y dorso)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URLs obtenidas exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no tiene fotos de carnet"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/license")
    public ResponseEntity<Response<LicenseUrlsResponse>> getLicensePhotoUrls() {
        return new ResponseEntity<>(mediaService.getLicensePhotoUrls(), HttpStatus.OK);
    }

    @Operation(
        summary = "Upload o actualizar foto de perfil"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Foto subida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido (tamaño, formato, etc)"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/profile")
    public ResponseEntity<Response<Void>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(mediaService.uploadMedia(file, null, CategoryMediaEnum.PROFILE), HttpStatus.OK);
    }

    @Operation(
        summary = "Upload o actualizar fotos del carnet (frente y/o trasera)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fotos subidas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o ambos archivos vacíos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/license")
    public ResponseEntity<Response<Void>> uploadLicensePhotos(
            @RequestParam(value = "front", required = false) MultipartFile frontFile,
            @RequestParam(value = "back", required = false) MultipartFile backFile) {
        return new ResponseEntity<>(mediaService.uploadMedia(frontFile, backFile, CategoryMediaEnum.LICENSE_FRONT), HttpStatus.OK);
    }

    @Operation(
        summary = "Eliminar foto de perfil y restaurar default"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Foto eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no tiene foto de perfil"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/profile")
    public ResponseEntity<Response<Void>> deleteProfilePicture() {
        return new ResponseEntity<>(mediaService.deleteMedia(CategoryMediaEnum.PROFILE), HttpStatus.OK);
    }

    @Operation(
        summary = "Eliminar fotos del carnet (frente y dorso)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fotos eliminadas exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existen fotos de carnet para eliminar"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/license")
    public ResponseEntity<Response<Void>> deleteLicensePhotos() {
        return new ResponseEntity<>(mediaService.deleteMedia(CategoryMediaEnum.LICENSE_FRONT), HttpStatus.OK);
    }
}
