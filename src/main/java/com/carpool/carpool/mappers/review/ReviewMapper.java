package com.carpool.carpool.mappers.review;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.dto.review.UserReviewDTO;
import com.carpool.carpool.model.review.Review;
import com.carpool.carpool.service.media.IMediaService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
  private final IMediaService mediaService;


  public DriverReviewResponseDTO convertReviewToDriverReviewResponseDTO(Review review){
    return DriverReviewResponseDTO.builder()
      .reviewId(review.getId())
      .stars(review.getStars())
      .createdAt(review.getCreatedAt())
      .description(review.getDescription())
    .build();

  }

  /**
     * Convierte un ReviewRequestDTO en una entidad Review. Si se proporciona un objeto Review existente, se actualizan sus campos; de lo contrario, se crea uno nuevo.
     * @param review entidad Review existente (puede ser null para creación)
     * @param reviewResponseDTO DTO con los datos de la reseña a convertir
     * @return Review entidad lista para persistir o actualizar
     */
   public Review convertReviewRequestDtoToReview(Review review, ReviewResponseDTO reviewResponseDTO) {
        return Review.builder()
            .stars(reviewResponseDTO.getStars())
            .description(reviewResponseDTO.getDescription())
            .build();
    }

    /**
     * Convierte una entidad Review en un ReviewResponseDTO para ser enviado al cliente. Se incluyen los nombres completos del reseñador y el usuario objetivo.
     * @param review entidad Review a convertir
     * @return ReviewResponseDTO con los datos de la reseña formateados para la respuesta
     */
    public ReviewResponseDTO convertReviewToReviewResponseDTO(Review review) {
        ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO();
            reviewResponseDTO.setId(review.getId());
            reviewResponseDTO.setStars(review.getStars());
            reviewResponseDTO.setDescription(review.getDescription());
            reviewResponseDTO.setReviewerName(review.getReviewerUser().getName() + " " + review.getReviewerUser().getLastname());
            reviewResponseDTO.setTargetName(review.getTargetUser().getName() + " " + review.getTargetUser().getLastname());
        return reviewResponseDTO;

    }

    /**
     * Convierte una entidad Review en UserReviewDTO mostrando los datos del usuario revisor.
     * Usado cuando el usuario autenticado quiere ver las reseñas que recibió.
     */
    public UserReviewDTO convertReviewToReviewToMeDTO(Review review) {
      String profilePictureUrl = mediaService.getProfilePictureUrlByUserId(review.getReviewerUser().getId());
      String completeName = review.getReviewerUser().getName() + " " + review.getReviewerUser().getLastname();

      return UserReviewDTO.builder()
          .id(review.getId())
          .stars(review.getStars())
          .createdAt(review.getCreatedAt())
          .tripDate(review.getTrip().getStartTripDateTime())
          .description(review.getDescription())
          .completeName(completeName)
          .profilePhotoUrl(profilePictureUrl)
          .tripId(review.getTrip().getId())
          .build();
    }
    /**
     * Convierte una entidad Review en UserReviewDTO mostrando los datos del usuario target.
     * Usado cuando el usuario autenticado quiere ver las reseñas que él realizó.
     */
    public UserReviewDTO convertReviewToMyMadeReviewDTO(Review review) {
      String profilePictureUrl = mediaService.getProfilePictureUrlByUserId(review.getTargetUser().getId());
      String completeName = review.getTargetUser().getName() + " " + review.getTargetUser().getLastname();

      return UserReviewDTO.builder()
          .id(review.getId())
          .stars(review.getStars())
          .createdAt(review.getCreatedAt())
          .tripDate(review.getTrip().getStartTripDateTime())
          .description(review.getDescription())
          .completeName(completeName)
          .profilePhotoUrl(profilePictureUrl)
          .tripId(review.getTrip().getId())
          .build();
    }   
}
