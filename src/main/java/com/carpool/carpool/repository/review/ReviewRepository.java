package com.carpool.carpool.repository.review;

import java.util.List;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carpool.carpool.model.review.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Query("""
      SELECT r
      FROM Review r
      WHERE r.targetUser.id = :userId
        AND r.deletedAt IS NULL
  """)
  List<Review> findReviewsByTargetUser(
      @Param("userId") Long userId,
      Pageable pageable
  );
}
