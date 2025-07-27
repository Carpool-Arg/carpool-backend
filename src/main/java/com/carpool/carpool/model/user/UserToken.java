package com.carpool.carpool.model.user;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_token")
public class UserToken {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede quedar en blanco.")
    @Size(min = 32, max = 64, message = "El token debe tener entre 32 y 64 caracter.")
    @Column(unique = true, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TokenTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private TokenStateEnum state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "used_at", updatable = false)
    private LocalDateTime usedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(48);
    }
}
