package com.carpool.carpool.repository.media;

import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.model.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<Media> findByUserId(Long idUser);
    boolean existsByObjectKey(String objectKey);
    Optional<Media> findByUserIdAndCategory(Long idUser, CategoryMediaEnum categoryMediaEnum);
}
