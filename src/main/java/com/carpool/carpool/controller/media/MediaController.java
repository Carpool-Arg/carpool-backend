package com.carpool.carpool.controller.media;

import com.carpool.carpool.dto.media.MediaDeleteRequestDTO;
import com.carpool.carpool.dto.media.MediaIdRequestDTO;
import com.carpool.carpool.dto.media.MediaUploadRequestDTO;
import com.carpool.carpool.dto.media.MediaUsersRequestDTO;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.media.IMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@Tag(name = "Media", description = "Operaciones relacionadas con imagenes")
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final IMediaService mediaService;

    @Operation(
            summary = "Subir y almacenar un archivo en R2 y en la base de datos"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo almacenado con exito"),
            @ApiResponse(responseCode = "409", description = "Errores de validaciones", content = @Content)
    })
    @PostMapping()
    public ResponseEntity<Response<Media>> upload(@RequestBody MediaUploadRequestDTO mediaUploadRequestDTO, @RequestParam("file") MultipartFile file){
        return new ResponseEntity<>(mediaService.uploadAndSaveFile(file, mediaUploadRequestDTO.getDni()), HttpStatus.OK);
    }

    @Operation(summary = "Eliminar un archivo en R2 y en la base de datos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo eliminado con exito"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content)
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Response<Void>> deleteFile(@RequestBody MediaDeleteRequestDTO mediaDeleteRequestDTO) {
        return new ResponseEntity<>(mediaService.deleteFile(mediaDeleteRequestDTO.getMediaId()), HttpStatus.OK);
    }

    @Operation(summary = "Obtener las imagenes de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagenes obtenidas con exito"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content)
    })
    @GetMapping("/user")
    public ResponseEntity<Response<List<Media>>> getFilesByOwner(@RequestBody MediaUsersRequestDTO mediaUsersRequestDTO) {
        return new ResponseEntity<>(mediaService.getFilesByOwner(mediaUsersRequestDTO.getDni()), HttpStatus.OK);
    }

    @Operation(summary = "Obtener una imagen en particular")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen obtenida con exito"),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content)
    })
    @GetMapping("/{mediaId}")
    public ResponseEntity<Response<Media>> getFileById(@RequestBody MediaIdRequestDTO mediaIdRequestDTO) {
        return new ResponseEntity<>(mediaService.getFileById(mediaIdRequestDTO.getMediaId()), HttpStatus.OK);
    }
}
