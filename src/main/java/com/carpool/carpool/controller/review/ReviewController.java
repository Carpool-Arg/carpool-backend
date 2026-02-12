package com.carpool.carpool.controller.review;

import com.carpool.carpool.dto.review.ReviewRequestDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.review.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Reseñas", description = "Operaciones relacionadas con las reseñas entre usuarios")
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @Operation(
        summary = "Crear una nueva reseña",
        description = "Permite a un pasajero reseñar a un chofer tras finalizar un viaje abonado. Valida estados de viaje y evita duplicados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reseña creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación en los campos"),
        @ApiResponse(responseCode = "404", description = "Viaje o Usuario no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto: El viaje no terminó, no está pago o ya fue reseñado")
    })
    @PostMapping
    public ResponseEntity<Response<ReviewResponseDTO>> createReview(
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO) {
        
        Response<ReviewResponseDTO> serviceResponse = reviewService.createReview(reviewRequestDTO);
        return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Obtener reseñas de un usuario",
        description = "Devuelve la lista de reseñas recibidas por un usuario específico (chofer)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de reseñas obtenida correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/target/{targetUserId}")
    public ResponseEntity<Response<List<ReviewResponseDTO>>> getReviewsByTargetUser(
            @PathVariable Long targetUserId) {
        
        Response<List<ReviewResponseDTO>> serviceResponse = reviewService.getReviewsByTargetUser(targetUserId);
        return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
    }
}