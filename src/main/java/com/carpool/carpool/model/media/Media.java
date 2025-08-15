package com.carpool.carpool.model.media;

import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.model.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@RequiredArgsConstructor
@Table(name = "media")
public class Media {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "bucket", nullable = false)
    private String bucket;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CategoryMediaEnum category;

    @Column(name = "object_key", nullable = false, columnDefinition="text")
    private String objectKey;

    @Column(name = "filename", nullable = false, columnDefinition="text")
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "byte_size", nullable = false)
    private Long byteSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
