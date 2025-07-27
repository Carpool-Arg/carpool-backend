package com.carpool.carpool.repository.user.token;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.model.user.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken,Long> {
    Optional<UserToken> findByToken(String token);
    List<UserToken> findByTypeAndExpiresAtBeforeAndState(TokenTypeEnum type, LocalDateTime dateTime, TokenStateEnum state);
}
