package com.carpool.carpool.controller.media;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.media.IMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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

    @Operation(summary = "Obtener URL de imagen de perfil de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Url de la imagen obtenida con exito"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
            @ApiResponse(responseCode = "500", description = "Errores al intentar generar Url para acceder al recurso", content = @Content)
    })
    @GetMapping("{idUser}")
    public ResponseEntity<Response<String>> getUserFile(@PathVariable Long idUser) {
        return new ResponseEntity<>(mediaService.getFileUser(idUser), HttpStatus.OK);
    }

    @Operation(
            summary = "Subir y almacenar un archivo en R2 y en la base de datos"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo almacenado con exito"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones"),
            @ApiResponse(responseCode = "500", description = "Errores al intentar subir el archivo a R2", content = @Content)
    })
    @PostMapping("{idUser}")
    public ResponseEntity<Response<Void>> uploadFileUser(@PathVariable Long idUser, @RequestParam("file") MultipartFile file){
        return new ResponseEntity<>(mediaService.uploadAndSaveFileUser(file, idUser), HttpStatus.OK);
    }

    @Operation(summary = "Eliminar un archivo en R2 y en la base de datos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo eliminado con exito"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content)
    })
    @DeleteMapping("{idUser}")
    public ResponseEntity<Response<Void>> deleteFileUser(@PathVariable Long idUser) {
        return new ResponseEntity<>(mediaService.deleteFileUser(idUser), HttpStatus.OK);
    }
}
