package com.carpool.carpool.controller.review;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
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
}
