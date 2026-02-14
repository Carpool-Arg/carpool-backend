package com.carpool.carpool.service.review;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.carpool.carpool.mappers.review.ReviewMapper;
import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.review.Review;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.review.ReviewRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewImplementation implements IReviewService{
  
  
  private final ReviewRepository reviewRepository;
  private final DriverRepository driverRepository;
  private final ReviewMapper reviewMapper;
  
  @Override
  public Response<List<DriverReviewResponseDTO>> getDriverReviews(Long driverId, int skip, String orderBy) {
    log.info("Iniciando la recuperacion de las reservas del chofer con el id: {}",driverId);
    Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new ResourceNotFoundException("El chofer no existe."));

    List<Review> reviews = reviewRepository.findReviewsByTargetUser(driver.getUser().getId(),getPageable(orderBy, skip));
    
    log.info("Reseñas obtenidas con exito. Cantidad: {}",reviews.size());
    if(reviews.isEmpty()){
      return ResponseUtils.buildOKResponse(List.of("El chofer no tiene reseñas"),null); 
    }

    List<DriverReviewResponseDTO> driverReviews = reviews
      .stream()
      .map(reviewMapper::convertReviewToDriverReviewResponseDTO)
    .toList();

    
    return ResponseUtils.buildOKResponse(List.of("Reseñas recuperadas con éxito"),driverReviews); 
  }

  /**
   * Metodo para obtener el objeto que vamos a usar para el paginado
   * Definmos un tamaño de la pgina fijo 
   * @param type
   * @param skip
   * @return
   */
  private Pageable getPageable(String type, int skip) {
    final int PAGE_SIZE = 10;
    int page = skip / PAGE_SIZE;

    Sort sort = switch (type) {
        case "RATING_DESC" -> Sort.by("stars").descending();
        case "RATING_ASC"  -> Sort.by("stars").ascending();
        case "RECENT"      -> Sort.by("createdAt").descending();
        default            -> Sort.by("createdAt").descending();
    };

    return PageRequest.of(page, PAGE_SIZE, sort);
  }
}
