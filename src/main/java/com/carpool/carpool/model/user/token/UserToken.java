package com.carpool.carpool.model.user.token;

import java.time.LocalDateTime;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;

import com.carpool.carpool.model.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
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

    @Column(name = "expires_at", updatable = false)
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
        if (this.type == TokenTypeEnum.PASSWORD_CHANGE) {
            this.expiresAt = this.createdAt.plusMinutes(30);
        }else if(this.type == TokenTypeEnum.ACTIVATION || this.type == TokenTypeEnum.EMAIL_CHANGE){
            this.expiresAt = this.createdAt.plusHours(48);
        }
    }

    public boolean isExpired(){
        if (this.expiresAt == null) return false;

        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
