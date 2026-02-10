package com.carpool.carpool.mappers.review;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.review.ReviewResponseDTO;
import com.carpool.carpool.model.review.Review;

@Component
public class ReviewMapper {
    
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

}