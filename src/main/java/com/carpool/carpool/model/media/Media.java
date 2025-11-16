package com.carpool.carpool.model.media;

import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "media")
public class Media {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("A quien corresponde la imagen")
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Comment("Bucket de Cloudflare")
    @Size(max = 50, message = "El nombre del bucket debe tener como máximo 50 caracteres")
    @Column(name = "bucket", nullable = false)
    private String bucket;

    @Comment("Categoria de la imagen, PROFILE para el perfil, VEHICLE para vehiculos, etc")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CategoryMediaEnum category;

    @Comment("Identificador de la imagen en el bucket")
    @Column(name = "object_key", nullable = false, columnDefinition="text")
    private String objectKey;

    @Comment("Nombre del archivo")
    @Column(name = "filename", nullable = false, columnDefinition="text")
    private String fileName;

    @Comment("Tipo de contenido de la imagen")
    @Size(max = 64, message = "El tipo de contenido debe tener como máximo 64 caracteres")
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Comment("Tamanio de la imagen")
    @Column(name = "byte_size", nullable = false)
    private Long byteSize;

    @Comment("Fecha de creacion")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Comment("Fecha de actualizacion")
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
