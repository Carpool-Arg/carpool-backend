package com.carpool.carpool.repository.user.token;

import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.user.token.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken,Long> {
    Optional<UserToken> findByToken(String token);
    List<UserToken> findByUserAndTypeAndState(User user, TokenTypeEnum type, TokenStateEnum state);

    @Transactional
    @Modifying
    void deleteByUserIdAndType(Long userId, TokenTypeEnum type);
}
