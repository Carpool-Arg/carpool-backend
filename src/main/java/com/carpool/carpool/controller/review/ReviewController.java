package com.carpool.carpool.controller.review;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
import com.carpool.carpool.dto.review.ReviewRequestDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.review.IReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name="Review", description ="Operaciones relacionadas con las reseñas")
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

  private final IReviewService reviewService;

  @Operation(
    summary = "Obtener las reservas paginadas de un chofer"
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Reseñas obtenidas con éxito", content = @Content),
  })
  @GetMapping("/driver")
  public ResponseEntity<Response<List<DriverReviewResponseDTO>>> getDriverReviews(
    @RequestParam(required = true) Long driverId,
    @RequestParam(required = false, defaultValue = "0") int skip,
    @RequestParam(required = false, defaultValue = "RECENT") String orderBy
  ){
    return new ResponseEntity<>(reviewService.getDriverReviews(driverId, skip, orderBy), HttpStatus.OK);
  }

  @Operation(
        summary = "Verificar si se puede reseñar un viaje",
        description = "Valida si el usuario actual puede reseñar al conductor de un viaje específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validación completada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Viaje no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado para acceder a este recurso")
    })
    @GetMapping("/can-review/{tripId}")
    public ResponseEntity<Response<Boolean>> canReview(@PathVariable("tripId") Long tripId) {
        return ResponseEntity.ok(reviewService.canUserReviewTrip(tripId));
    }
    
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
    @Operation(summary = "Eliminar una reseña", description = "Elimina físicamente una reseña y actualiza el promedio del chofer calificado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reseña eliminada con éxito"),
        @ApiResponse(responseCode = "404", description = "Reseña no encontrada"),
        @ApiResponse(responseCode = "403", description = "No autorizado para eliminar esta reseña")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteReview(@PathVariable Long id) {
        return new ResponseEntity<>(reviewService.deleteReview(id), HttpStatus.OK);
    }
}
