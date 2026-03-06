package com.carpool.carpool.mappers.review;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.review.DriverReviewResponseDTO;
import com.carpool.carpool.dto.review.MyMadeReviewDTO;
import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.dto.review.ReviewToMeDTO;
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

    public ReviewToMeDTO convertReviewToReviewToMeDTO(Review review){
      String profilePictureUrl = mediaService.getProfilePictureUrlByUserId(review.getReviewerUser().getId());
      String userCompleteName = review.getReviewerUser().getName() + " " + review.getReviewerUser().getLastname();

      return ReviewToMeDTO.builder()
        .completeName(userCompleteName)
        .stars(review.getStars())
        .reviewDate(review.getCreatedAt())
        .tripDate(review.getTrip().getStartTripDateTime())
        .description(review.getDescription())
        .profilePhotoUrl(profilePictureUrl)
      .build();
    }

    /**
     * 
     * @param review
     * @return
     */
    public MyMadeReviewDTO convertReviewToMyMadeReviewDTO(Review review) {
        String targetProfilePictureUrl = mediaService.getProfilePictureUrlByUserId(review.getTargetUser().getId());
        String targetCompleteName = review.getTargetUser().getName() + " " + review.getTargetUser().getLastname();

        return MyMadeReviewDTO.builder()
            .id(review.getId()) 
            .stars(review.getStars())
            .createdAt(review.getCreatedAt())
            .tripDate(review.getTrip().getStartTripDateTime())
            .description(applyEllipsis(review.getDescription(), 100)) 
            .targetFullName(targetCompleteName)
            .targetPhoto(targetProfilePictureUrl)
            .tripId(review.getTrip().getId())
            .build();
    }

    /**
     * Aplica puntos suspensivos si el texto supera el límite.
     */
    private String applyEllipsis(String text, int limit) {
        if (text == null || text.length() <= limit) {
            return text;
        }
        return text.substring(0, limit - 3) + "...";
    }
}
