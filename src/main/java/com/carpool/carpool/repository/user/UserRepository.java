package com.carpool.carpool.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpool.carpool.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByDniAndDeletedAtIsNull(String dni);
    Optional<User> findByUsername(String username);
    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);
    Optional<User> findByIdAndDeletedAtIsNull(Long userId);
}
