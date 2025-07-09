package com.carpool.carpool.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.carpool.carpool.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted_at IS NULL")
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    
    Optional<User> findByEmail(String email);
    Optional<User> findByDni(String dni);
}
