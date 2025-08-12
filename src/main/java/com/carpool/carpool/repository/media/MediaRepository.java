package com.carpool.carpool.repository.media;

import com.carpool.carpool.model.media.Media;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Optional<List<Media>> findByOwnerOrderByCreatedAtDesc(Long owner);

    @Query("SELECT m FROM Media m WHERE m.owner = :owner AND m.contentType LIKE 'image/%' ORDER BY m.createdAt DESC")
    List<Media> findImagesByOwner(@Param("owner") Long owner);
}
